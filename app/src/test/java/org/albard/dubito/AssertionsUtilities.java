package org.albard.dubito;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerId;
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
        Assertions.assertEquals(expectedOwnerId, actualLobby.getOwner());
        Assertions.assertEquals(expectedInfo, actualLobby.getInfo());
        assertIterableEqualsUnordered(expectedParticipants, actualLobby.getParticipants());
    }

    public static <X> void assertIterableEqualsUnordered(final Collection<X> expected, final Collection<X> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        expected.forEach(x -> actual.contains(x));
    }

    public static <X> void assertStreamEqualsUnordered(final Stream<X> expected, final Stream<X> actual) {
        assertIterableEqualsUnordered(expected.toList(), actual.toList());
    }
}
