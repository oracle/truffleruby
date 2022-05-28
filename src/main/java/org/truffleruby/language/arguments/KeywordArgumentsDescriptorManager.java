/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.string.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class KeywordArgumentsDescriptorManager {

    public static final KeywordArgumentsDescriptor EMPTY = new KeywordArgumentsDescriptor(
            StringUtils.EMPTY_STRING_ARRAY, KeywordArgumentsDescriptor.SplatType.NO_SPLAT);

    private final WeakValueCache<Key, KeywordArgumentsDescriptor> CANONICAL_KEYWORD_DESCRIPTORS = new WeakValueCache<>();

    public KeywordArgumentsDescriptorManager() {
        CANONICAL_KEYWORD_DESCRIPTORS
                .put(new Key(StringUtils.EMPTY_STRING_ARRAY, KeywordArgumentsDescriptor.SplatType.NO_SPLAT), EMPTY);
    }

    public KeywordArgumentsDescriptor getArgumentsDescriptor(String[] keywords,
            KeywordArgumentsDescriptor.SplatType splatType) {
        final Key key = new Key(keywords, splatType);
        final KeywordArgumentsDescriptor descriptor = new KeywordArgumentsDescriptor(keywords, splatType);
        return CANONICAL_KEYWORD_DESCRIPTORS.addInCacheIfAbsent(key, descriptor);
    }

    public static class Key {

        private final String[] keywords;
        private final KeywordArgumentsDescriptor.SplatType splatType;

        public Key(String[] keywords, KeywordArgumentsDescriptor.SplatType splatType) {
            this.keywords = keywords;
            this.splatType = splatType;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other == null || other.getClass() != getClass()) {
                return false;
            }

            final Key otherKey = (Key) other;
            return Arrays.equals(keywords, otherKey.keywords) && splatType == otherKey.splatType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(keywords), splatType);
        }

    }

}
