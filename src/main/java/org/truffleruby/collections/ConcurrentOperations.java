/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
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
    public static <V, K> V getOrCompute(Map<K, V> map, K key, Function<? super K, ? extends V> compute) {
        V value = map.get(key);
        if (value != null) {
            return value;
        } else {
            final V newValue = compute.apply(key);
            final V oldValue = map.putIfAbsent(key, newValue);
            return oldValue != null ? oldValue : newValue;
        }
    }

}
