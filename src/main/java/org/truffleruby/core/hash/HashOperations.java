/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.library.EmptyHashStore;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyBaseNode;

public abstract class HashOperations {

    public static RubyHash newEmptyHash(RubyContext context, RubyLanguage language) {
        return new RubyHash(
                context.getCoreLibrary().hashClass,
                language.hashShape,
                context,
                EmptyHashStore.NULL_HASH_STORE,
                0);
    }

    // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes
    public static final int BOOLEAN_CLASS_SALT = 55927484;
    public static final int INTEGER_CLASS_SALT = 1028093337;
    public static final int DOUBLE_CLASS_SALT = -1611229937;

    public static long hashBoolean(boolean value, RubyContext context, RubyBaseNode node) {
        return context.getHashing(node).hash(BOOLEAN_CLASS_SALT, Boolean.hashCode(value));
    }

    public static long hashLong(long value, RubyContext context, RubyBaseNode node) {
        return context.getHashing(node).hash(INTEGER_CLASS_SALT, value);
    }

    public static long hashDouble(double value, RubyContext context, RubyBaseNode node) {
        return context.getHashing(node).hash(DOUBLE_CLASS_SALT, Double.doubleToRawLongBits(value));
    }

    public static long hashBignum(RubyBignum value, RubyContext context, RubyBaseNode node) {
        return context.getHashing(node).hash(INTEGER_CLASS_SALT, BigIntegerOps.hashCode(value));
    }
}
