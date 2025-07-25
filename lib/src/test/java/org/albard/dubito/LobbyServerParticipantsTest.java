package org.albard.dubito;

import static org.albard.dubito.TestUtilities.addMessageListener;
import static org.albard.dubito.TestUtilities.withLobbyServer;
import static org.albard.dubito.TestUtilities.withNetwork;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.albard.dubito.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.lobby.messages.JoinLobbyFailedMessage;
import org.albard.dubito.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.lobby.messages.LeaveLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.messages.ErrorGameMessageBase;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public final class LobbyServerParticipantsTest {
    @ParameterizedTest
    @ValueSource(strings = { "password" })
    @EmptySource
    void testJoinLobbyOwner(final String password) throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, owner -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, joiner -> {
                    withLobbyServer(network, server -> {
                        final List<LobbyUpdatedMessage> updateReceived = addMessageListener(LobbyUpdatedMessage.class,
                                owner);
                        owner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        joiner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        final LobbyInfo info = new LobbyInfo("Test Lobby", password);
                        owner.sendMessage(new CreateLobbyMessage(owner.getLocalPeerId(), null, info));
                        Thread.sleep(Duration.ofSeconds(1));
                        final Lobby initialCreatedLobby = server.getLobbies().getFirst();

                        joiner.sendMessage(new JoinLobbyMessage(joiner.getLocalPeerId(), null,
                                initialCreatedLobby.getId(), password));
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(2, updateReceived.size());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId()), updateReceived.getFirst().getLobby());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId(), joiner.getLocalPeerId()),
                                updateReceived.getLast().getLobby());
                    });
                });
            });
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "password" })
    @EmptySource
    void testJoinLobbyJoiner(final String password) throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, owner -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, joiner -> {
                    withLobbyServer(network, server -> {
                        final List<LobbyJoinedMessage> joinReceived = addMessageListener(LobbyJoinedMessage.class,
                                joiner);
                        final List<LobbyUpdatedMessage> updateReceived = addMessageListener(LobbyUpdatedMessage.class,
                                joiner);
                        final List<ErrorGameMessageBase> failReceived = addMessageListener(ErrorGameMessageBase.class,
                                joiner);
                        owner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        joiner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        final LobbyInfo info = new LobbyInfo("Test Lobby", password);
                        owner.sendMessage(new CreateLobbyMessage(owner.getLocalPeerId(), null, info));
                        Thread.sleep(Duration.ofSeconds(1));
                        final Lobby initialCreatedLobby = server.getLobbies().getFirst();

                        joiner.sendMessage(new JoinLobbyMessage(joiner.getLocalPeerId(), null,
                                initialCreatedLobby.getId(), password));
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(1, joinReceived.size());
                        Assertions.assertEquals(initialCreatedLobby.getId(), joinReceived.getFirst().getLobbyId());
                        Assertions.assertEquals(1, updateReceived.size());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId(), joiner.getLocalPeerId()),
                                updateReceived.getFirst().getLobby());
                        Assertions.assertEquals(0, failReceived.size());
                    });
                });
            });
        });
    }

    @Test
    void testJoinLobbyFailWithWrongPassword() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, owner -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, joiner -> {
                    withLobbyServer(network, server -> {
                        final List<LobbyJoinedMessage> joinReceived = addMessageListener(LobbyJoinedMessage.class,
                                joiner);
                        final List<LobbyUpdatedMessage> updateReceived = addMessageListener(LobbyUpdatedMessage.class,
                                joiner);
                        final List<ErrorGameMessageBase> failReceived = addMessageListener(ErrorGameMessageBase.class,
                                joiner);
                        owner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        joiner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        final LobbyInfo info = new LobbyInfo("Test Lobby", "password");
                        owner.sendMessage(new CreateLobbyMessage(owner.getLocalPeerId(), null, info));
                        Thread.sleep(Duration.ofSeconds(1));
                        final Lobby initialCreatedLobby = server.getLobbies().getFirst();

                        joiner.sendMessage(new JoinLobbyMessage(joiner.getLocalPeerId(), null,
                                initialCreatedLobby.getId(), "a_wrong_password"));
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(0, joinReceived.size());
                        Assertions.assertEquals(0, updateReceived.size());
                        Assertions.assertEquals(1, failReceived.size());
                        Assertions.assertTrue(failReceived.getFirst() instanceof JoinLobbyFailedMessage);
                        Assertions.assertTrue(failReceived.getFirst().getErrors().contains("invalid password"));
                    });
                });
            });
        });
    }

    @Test
    void testLeaveLobbyJoiner() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, owner -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, joiner -> {
                    withLobbyServer(network, server -> {
                        final List<LobbyLeavedMessage> leaveReceived = TestUtilities
                                .addMessageListener(LobbyLeavedMessage.class, joiner);
                        final List<ErrorGameMessageBase> failReceived = TestUtilities
                                .addMessageListener(ErrorGameMessageBase.class, joiner);
                        owner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        joiner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        final LobbyInfo info = new LobbyInfo("Test Lobby", "");
                        owner.sendMessage(new CreateLobbyMessage(owner.getLocalPeerId(), null, info));
                        Thread.sleep(Duration.ofSeconds(1));
                        final Lobby initialCreatedLobby = server.getLobbies().getFirst();

                        joiner.sendMessage(
                                new JoinLobbyMessage(joiner.getLocalPeerId(), null, initialCreatedLobby.getId(), ""));
                        Thread.sleep(Duration.ofSeconds(1));

                        joiner.sendMessage(
                                new LeaveLobbyMessage(joiner.getLocalPeerId(), null, initialCreatedLobby.getId()));
                        Thread.sleep(Duration.ofSeconds(1));

                        final Lobby lobby = server.getLobbies().getFirst();
                        Assertions.assertEquals(1, leaveReceived.size());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId()), lobby);
                        Assertions.assertEquals(0, failReceived.size());
                    });
                });
            });
        });
    }

    @Test
    void testLeaveLobbyOwner() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, owner -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, joiner -> {
                    withLobbyServer(network, server -> {
                        final List<LobbyUpdatedMessage> updateReceived = addMessageListener(LobbyUpdatedMessage.class,
                                owner);
                        final List<ErrorGameMessageBase> failReceived = addMessageListener(ErrorGameMessageBase.class,
                                owner);
                        owner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        joiner.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        final LobbyInfo info = new LobbyInfo("Test Lobby", "");
                        owner.sendMessage(new CreateLobbyMessage(owner.getLocalPeerId(), null, info));
                        Thread.sleep(Duration.ofSeconds(1));

                        final Lobby initialCreatedLobby = server.getLobbies().getFirst();
                        joiner.sendMessage(
                                new JoinLobbyMessage(joiner.getLocalPeerId(), null, initialCreatedLobby.getId(), ""));
                        Thread.sleep(Duration.ofSeconds(1));

                        joiner.sendMessage(
                                new LeaveLobbyMessage(joiner.getLocalPeerId(), null, initialCreatedLobby.getId()));
                        Thread.sleep(Duration.ofSeconds(1));

                        final Lobby lobby = server.getLobbies().getFirst();
                        Assertions.assertEquals(3, updateReceived.size());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId()), updateReceived.getLast().getLobby());
                        AssertionsUtilities.assertLobby(owner.getLocalPeerId(), info, initialCreatedLobby.getId(),
                                Set.of(owner.getLocalPeerId()), lobby);
                        Assertions.assertEquals(0, failReceived.size());
                    });
                });
            });
        });
    }
}
