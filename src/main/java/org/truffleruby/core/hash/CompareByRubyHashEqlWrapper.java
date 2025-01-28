/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.core.hash.HashingNodes.ToHashByHashCode;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;

/** Wraps a key so that it will be hashed and compared according to Ruby Hash key semantics, using #hash and #eql?. */
public class CompareByRubyHashEqlWrapper {

    private final Object key;
    private final int hashCode;

    /** @param hashCode the result of {@link ToHashByHashCode} on the key */
    public CompareByRubyHashEqlWrapper(Object key, int hashCode) {
        this.key = key;
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        // We use an uncached SameOrEqlNode here since we are not in PE-able code
        return obj instanceof CompareByRubyHashEqlWrapper other && hashCode == other.hashCode &&
                SameOrEqlNode.executeUncached(key, other.key);
    }

    public static final class RecordingCompareByRubyHashEqlWrapper extends CompareByRubyHashEqlWrapper {

        public Object keyInMap;

        public RecordingCompareByRubyHashEqlWrapper(Object key, int hashCode) {
            super(key, hashCode);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                this.keyInMap = ((CompareByRubyHashEqlWrapper) obj).key;
                return true;
            } else {
                return false;
            }
        }
    }
}
