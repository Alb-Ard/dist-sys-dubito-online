package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.albard.dubito.app.lobby.Lobby;
import org.albard.dubito.app.lobby.LobbyInfo;
import org.albard.dubito.app.lobby.LobbyServer;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public final class LobbyServerParticipantsTest {
    @ParameterizedTest
    @ValueSource(strings = { "password" })
    @NullAndEmptySource
    void testJoinLobby(final String password) throws IOException, InterruptedException {
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork ownerClient = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        messengerFactory);
                final PeerNetwork joiningClient = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9002,
                        messengerFactory)) {
            ownerClient.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            joiningClient.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo info = new LobbyInfo("Test Lobby", password);
            ownerClient.sendMessage(new CreateLobbyMessage(ownerClient.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            final Lobby lobby = server.getLobbies().get(0);
            final boolean[] joinReceived = new boolean[] { false };
            final boolean[] updateReceived = new boolean[] { false };
            joiningClient.addMessageListener(m -> {
                if (m instanceof LobbyJoinedMessage lobbyJoinedMessage) {
                    Assertions.assertEquals(lobby.getId(), lobbyJoinedMessage.getLobbyId());
                    joinReceived[0] = true;
                    return true;
                }
                if (m instanceof LobbyUpdatedMessage lobbyUpdatedMessage) {
                    AssertionsUtilities.assertLobby(ownerClient.getLocalPeerId(), info, lobby.getId(),
                            Set.of(ownerClient.getLocalPeerId(), joiningClient.getLocalPeerId()),
                            lobbyUpdatedMessage.getLobby());
                    updateReceived[0] = true;
                    return true;
                }
                if (m instanceof JoinLobbyFailedMessage) {
                    Assertions.fail("Lobby join has failed");
                }
                return false;
            });
            joiningClient
                    .sendMessage(new JoinLobbyMessage(joiningClient.getLocalPeerId(), null, lobby.getId(), password));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertTrue(joinReceived[0]);
            Assertions.assertTrue(updateReceived[0]);
        }
    }
}
