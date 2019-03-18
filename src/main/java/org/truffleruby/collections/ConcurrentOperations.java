/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class ConcurrentOperations {

    /**
     * Replaces {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} as it does not scale.
     * The JDK method takes a monitor for every access if the key is present.
     * See https://bugs.openjdk.java.net/browse/JDK-8161372 which only fixes it in Java 9
     * if they are no collisions in the bucket.
     * This method might execute the function multiple times, in contrast to computeIfAbsent().
     */
    public static <K, V> V getOrCompute(Map<K, V> map, K key, Function<? super K, ? extends V> compute) {
        V value = map.get(key);
        if (value != null) {
            return value;
        } else {
            final V newValue = compute.apply(key);
            final V oldValue = map.putIfAbsent(key, newValue);
            return oldValue != null ? oldValue : newValue;
        }
    }

    /**
     * Similar to {@link Map#replace(Object, Object, Object)} except that the old value can also be
     * null (missing). Returns true if the replace succeeded, or false if it should be retried
     * because <code>map.get(key)</code> is no longer associated to <code>oldValue</code>.
     */
    public static <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
        return oldValue == null ? map.putIfAbsent(key, newValue) == null : map.replace(key, oldValue, newValue);
    }

}
