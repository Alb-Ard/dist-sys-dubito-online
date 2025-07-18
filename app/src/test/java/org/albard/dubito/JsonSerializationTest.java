package org.albard.dubito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.abianchi.dubito.app.gameSession.models.CardValue;
import org.abianchi.dubito.messages.RoundCardGeneratedMessage;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.dubito.serialization.json.JsonObjectSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class JsonSerializationTest {
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
        final List<JsonTest> cardTypeTests = Arrays.stream(CardType.values()).map(x -> new JsonTest(x, CardType.class))
                .toList();

        final List<JsonTest> cardValueTests = Arrays.stream(CardValue.values())
                .map(x -> new JsonTest(x, CardValue.class)).toList();

        final List<JsonTest> messageTests = List.of(
                new JsonTest(new RoundCardGeneratedMessage(new PeerId("1"), null, CardValue.ACE), GameMessage.class),
                new JsonTest(new RoundCardGeneratedMessage(new PeerId("2"), null, CardValue.JOKER), GameMessage.class));

        final Builder<List<JsonTest>> testsStream = Stream.builder();
        cardTypeTests.forEach(x -> testsStream.add(List.of(x)));
        cardValueTests.forEach(x -> testsStream.add(List.of(x)));
        messageTests.forEach(x -> testsStream.add(List.of(x)));
        return testsStream.build();
    }
}
