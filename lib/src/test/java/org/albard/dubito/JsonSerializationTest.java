package org.albard.dubito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.albard.dubito.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.dubito.serialization.json.JsonObjectSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class JsonSerializationTest {
    private enum TestEnum {
        VALUE_A(0), VALUE_B(1);

        private final int testValue;

        private TestEnum(int testValue) {
            this.testValue = testValue;
        }

        @Override
        public String toString() {
            return "TestEnum(" + this.testValue + ")";
        }
    }

    private static final class JsonTest {
        private final Object object;
        private final Class<?> objectClass;

        public JsonTest(final Object object, final Class<?> objectClass) {
            this.object = object;
            this.objectClass = objectClass;
        }

        @Override
        public String toString() {
            return object.toString();
        }
    }

    @ParameterizedTest
    @MethodSource("getSerializationTestValues")
    void jsonObjectSerializer_serializeDeserialize_shouldEqualStartingValue(final JsonTest testValue) {
        final ObjectSerializer<Object> sut = new JsonObjectSerializer<>();
        final Optional<?> result = sut.deserialize(new ByteArrayInputStream(sut.serialize(testValue.object)),
                testValue.objectClass);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(testValue.object, result.get());
    }

    @ParameterizedTest
    @MethodSource("getSerializationTestCollections")
    void jsonObjectSerializer_serializeDeserialize_shouldEqualStartingValues(final List<JsonTest> testValues) {
        final ObjectSerializer<Object> sut = new JsonObjectSerializer<>();
        final List<byte[]> serializedValues = testValues.stream().map(x -> sut.serialize(x.object)).toList();
        final byte[] serializedCombined = concatArrays(serializedValues);
        final var stream = new ByteArrayInputStream(serializedCombined);
        for (int i = 0; i < testValues.size(); i++) {
            final JsonTest testValue = testValues.get(i);
            final Optional<?> result = sut.deserialize(stream, testValue.objectClass);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(testValue.object, result.get());
        }
    }

    private static byte[] concatArrays(final List<byte[]> serializedValues) {
        final byte[] serializedCombined = new byte[serializedValues.stream().map(x -> x.length).reduce(0,
                (a, b) -> a + b)];
        int writeIndex = 0;
        for (final byte[] serializedValue : serializedValues) {
            System.arraycopy(serializedValue, 0, serializedCombined, writeIndex, serializedValue.length);
            writeIndex += serializedValue.length;
        }
        return serializedCombined;
    }

    /**
     * Returns a stream of all the tests data. Every item in the stream is an object
     * that will be serialized/deserialized as a single unit test.
     * 
     * @implNote This is calculated by flattening and removing duplicates on
     *           getSerializationTestCollections
     */
    private static Stream<JsonTest> getSerializationTestValues() {
        return getSerializationTestCollections().flatMap(x -> x.stream()).distinct();
    }

    /**
     * Returns a stream of all the tests data. Every item in the stream is a list of
     * test values that will be serialized/deserialized as a single unit test.
     */
    private static Stream<List<JsonTest>> getSerializationTestCollections() {
        final List<JsonTest> enumTests = Arrays.stream(TestEnum.values()).map(x -> new JsonTest(x, TestEnum.class))
                .toList();

        final List<JsonTest> messageTests = List.of(
                new JsonTest(new JoinLobbyMessage(new PeerId("1"), null, new LobbyId("10"), ""), GameMessage.class),
                new JsonTest(new LobbyJoinedMessage(new PeerId("2"), Set.of(new PeerId("1")), new LobbyId("10")),
                        GameMessage.class));

        final Builder<List<JsonTest>> testsStream = Stream.builder();
        enumTests.forEach(x -> testsStream.add(List.of(x)));
        messageTests.forEach(x -> testsStream.add(List.of(x)));
        return testsStream.build();
    }
}
