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

import java.util.Arrays;
import java.util.Objects;

public class ArgumentsDescriptorManager {

    private final WeakValueCache<Key, NonEmptyArgumentsDescriptor> CANONICAL_NON_EMPTY_DESCRIPTORS = new WeakValueCache<>();

    public ArgumentsDescriptor getArgumentsDescriptor(String[] keywordArgumentsNames, int keywordHashIndex,
            boolean keywordHashElidable) {
        assert !(keywordHashIndex == NonEmptyArgumentsDescriptor.NO_KEYWORD_HASH && keywordHashElidable);

        if (keywordArgumentsNames.length == 0 && keywordHashIndex == NonEmptyArgumentsDescriptor.NO_KEYWORD_HASH) {
            return EmptyArgumentsDescriptor.INSTANCE;
        }

        final Key key = new Key(keywordArgumentsNames, keywordHashIndex, keywordHashElidable);

        final NonEmptyArgumentsDescriptor descriptor = new NonEmptyArgumentsDescriptor(keywordArgumentsNames,
                keywordHashIndex, keywordHashElidable);

        return CANONICAL_NON_EMPTY_DESCRIPTORS.addInCacheIfAbsent(key, descriptor);
    }

    public static class Key {

        private final String[] keywordArgumentNames;
        private final int keywordHashIndex;
        private final boolean keywordHashElidable;

        public Key(
                String[] keywordArgumentNames,
                int keywordHashIndex,
                boolean keywordHashElidable) {
            this.keywordArgumentNames = keywordArgumentNames;
            this.keywordHashIndex = keywordHashIndex;
            this.keywordHashElidable = keywordHashElidable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key that = (Key) o;
            return keywordHashIndex == that.keywordHashIndex && keywordHashElidable == that.keywordHashElidable &&
                    Arrays.equals(keywordArgumentNames, that.keywordArgumentNames);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keywordHashIndex, keywordHashElidable);
            result = 31 * result + Arrays.hashCode(keywordArgumentNames);
            return result;
        }

    }

}
