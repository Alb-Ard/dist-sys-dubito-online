package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.albard.dubito.app.utils.ObservableMap;
import org.albard.dubito.app.utils.ObservableMapListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ObservableMapTest {
    @Test
    void testCreatesEmpty() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void testAddNew() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        Assertions.assertNull(map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), new Object()));
        Assertions.assertEquals(1, map.size());
    }

    @Test
    void testAddListener() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        final List<Object> keys = new LinkedList<>();
        final List<Object> values = new LinkedList<>();
        final Object value = new Object();
        map.addListener(new ObservableMapListener<>() {
            @Override
            public void entryAdded(final Object key, final Object value) {
                keys.add(key);
                values.add(value);
            }

            @Override
            public void entryRemoved(final Object key, final Object value) {
                Assertions.fail();
            }
        });
        map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), value);
        Assertions.assertEquals(1, keys.size());
        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(new InetSocketAddress("127.0.0.1", 5050), keys.get(0));
        Assertions.assertEquals(value, values.get(0));
    }

    @Test
    void testAddExisting() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        final Object value = new Object();
        map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), value);
        Assertions.assertNotNull(map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), value));
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(value, map.get(new InetSocketAddress("127.0.0.1", 5050)));
    }

    @Test
    void testRemoveExisting() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), new Object());
        Assertions.assertNotNull(map.remove(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(0, map.size());
        Assertions.assertEquals(null, map.get(new InetSocketAddress("127.0.0.1", 5050)));
    }

    @Test
    void testRemoveListener() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        final List<Object> keys = new LinkedList<>();
        keys.add(new InetSocketAddress("127.0.0.1", 5050));
        final List<Object> values = new LinkedList<>();
        final Object value = new Object();
        values.add(value);
        map.addListener(new ObservableMapListener<>() {
            @Override
            public void entryAdded(final Object key, final Object value) {
            }

            @Override
            public void entryRemoved(final Object key, final Object value) {
                keys.remove(key);
                values.remove(value);
            }
        });
        map.putIfAbsent(new InetSocketAddress("127.0.0.1", 5050), value);
        map.remove(new InetSocketAddress("127.0.0.1", 5050));
        Assertions.assertEquals(0, keys.size());
        Assertions.assertEquals(0, values.size());
    }

    @Test
    void testRemoveNonExisting() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        Assertions.assertNull(map.remove(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void testRemoveAlreadyRemoved() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        map.putIfAbsent(endPoint, new Object());
        Assertions.assertNotNull(map.remove(endPoint));
        Assertions.assertNull(map.remove(endPoint));
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void testClear() {
        final ObservableMap<Object, Object> map = ObservableMap.createEmpty();
        for (int i = 0; i < 10; i++) {
            final Object value = new Object();
            map.putIfAbsent(new InetSocketAddress("127.0.0.1", i), value);
            Assertions.assertEquals(value, map.get(new InetSocketAddress("127.0.0.1", i)));
        }
        map.clear();
        Assertions.assertEquals(0, map.size());
    }
}
