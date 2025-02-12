package org.albard.dubito.app.lobby;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.albard.dubito.app.Locked;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.CreateLobbyMessage;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.LobbyCreatedMessage;
import org.albard.dubito.app.messaging.messages.LobbyCreationFailedMessage;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;

public class LobbyServer implements Closeable {
    private final Locked<Map<LobbyId, Lobby>> lobbies = Locked.of(new HashMap<>());
    private final PeerNetwork network;

    private LobbyServer(final PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
    }

    public static LobbyServer createBound(final String bindAddress, final int bindPort) throws IOException {
        return new LobbyServer(PeerNetwork.createBound(PeerId.createNew(), bindAddress, bindPort,
                new MessengerFactory(MessageSerializer.createJson())));
    }

    public int getLobbyCount() {
        return this.lobbies.getValue().size();
    }

    public List<Lobby> getLobbies() {
        return List.copyOf(this.lobbies.getValue().values());
    }

    @Override
    public void close() throws IOException {
        this.network.close();
    }

    private boolean handleMessage(final GameMessage message) {
        if (message instanceof CreateLobbyMessage createLobbyMessage) {
            final List<Exception> validationErrors = createLobbyMessage.getLobbyInfo().validate();
            if (!validationErrors.isEmpty()) {
                final List<String> errorDecriptions = validationErrors.stream().map(e -> e.getMessage()).toList();
                System.err.println("Could not create lobby: " + errorDecriptions);
                this.network.sendMessage(new LobbyCreationFailedMessage(this.network.getLocalPeerId(),
                        Set.of(message.getSender()), errorDecriptions));
                return true;
            }
            final LobbyId newLobbyId = LobbyId.createNew();
            final boolean wasLobbyCreated = this.lobbies
                    .compareAndSet(
                            lobbies -> lobbies.values().stream()
                                    .noneMatch(l -> l.getParticipants().contains(createLobbyMessage.getSender())),
                            lobbies -> {
                                System.out.println(
                                        "Creating lobby \"" + createLobbyMessage.getLobbyInfo().name() + "\"...");
                                lobbies.put(newLobbyId, new Lobby(newLobbyId, createLobbyMessage.getSender(),
                                        createLobbyMessage.getLobbyInfo()));
                                return lobbies;
                            });
            if (wasLobbyCreated) {
                this.network.sendMessage(new LobbyCreatedMessage(this.network.getLocalPeerId(), null, newLobbyId));
            } else {
                this.network.sendMessage(new LobbyCreationFailedMessage(this.network.getLocalPeerId(),
                        Set.of(message.getSender()), List.of("user already in a lobby")));
            }
            return true;
        }
        return false;
    }
}
