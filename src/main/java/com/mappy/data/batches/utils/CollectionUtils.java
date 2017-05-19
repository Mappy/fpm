package com.mappy.data.batches.utils;

import com.google.common.collect.ImmutableMap;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtils {

    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> streamIterator(Iterator<T> it) {
        return stream(() -> it);
    }

    public static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return ImmutableMap.<K, V> builder().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).build();
    }

    public static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return ImmutableMap.<K, V> builder().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).build();
    }

    public static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return ImmutableMap.<K, V> builder().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).put(k8, v8).build();
    }

    public static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return ImmutableMap.<K, V> builder().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).put(k8, v8).put(k9, v9).build();
    }

}
