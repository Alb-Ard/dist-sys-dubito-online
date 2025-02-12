package org.albard.dubito.app;

import java.util.List;
import java.util.Set;

import org.albard.dubito.app.lobby.Lobby;
import org.albard.dubito.app.lobby.LobbyId;
import org.albard.dubito.app.lobby.LobbyInfo;
import org.albard.dubito.app.network.PeerId;
import org.junit.jupiter.api.Assertions;

public final class AssertionsUtilities {
    private AssertionsUtilities() {
    }

    public static void assertLobbyNameValidationErrors(final String name, final List<String> errors) {
        if (name == null) {
            Assertions.assertTrue(errors.contains("name can't be null"));
        } else if (name.isBlank()) {
            Assertions.assertTrue(errors.contains("name can't be blank"));
        }
    }

    public static void assertLobby(final PeerId expectedOwnerId, final LobbyInfo expectedInfo,
            final LobbyId expectedLobbyId, final Set<PeerId> expectedParticipants, final Lobby actualLobby) {
        Assertions.assertEquals(expectedLobbyId, actualLobby.getId());
        Assertions.assertTrue(actualLobby.getOwner().equals(expectedOwnerId));
        Assertions.assertTrue(actualLobby.getInfo().equals(expectedInfo));
        Assertions.assertEquals(expectedParticipants.size(), actualLobby.getParticipants().size());
        expectedParticipants.forEach(p -> Assertions.assertTrue(actualLobby.getParticipants().contains(p)));
    }
}
