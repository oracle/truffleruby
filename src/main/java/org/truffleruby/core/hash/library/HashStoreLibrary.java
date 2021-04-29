/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash.library;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.HashGuards;
import org.truffleruby.core.hash.RubyHash;

/** Library for accessing and manipulating the storage used for representing hashes. This includes reading, modifying,
 * and copy the storage. */
@DefaultExport(PackedHashStoreLibrary.class)
@GenerateLibrary
public abstract class HashStoreLibrary extends Library {

    private static final LibraryFactory<HashStoreLibrary> FACTORY = LibraryFactory.resolve(HashStoreLibrary.class);

    public static LibraryFactory<HashStoreLibrary> getFactory() {
        return FACTORY;
    }

    public static HashStoreLibrary getDispatched() {
        return FACTORY.createDispatched(HashGuards.hashStrategyLimit());
    }

    /** Looks up the key in the hash and returns the associated value, or the result of calling {@code defaultNode} if
     * no entry for the given key exists. */
    @Abstract
    public abstract Object lookupOrDefault(Object store, Frame frame, RubyHash hash, Object key,
            BiFunctionNode defaultNode);

    /** Associates the key with the value and returns true only if the hash changed as a result of the operation (i.e.
     * returns false if the key was already associated with the value). {@code byIdentity} indicates whether the key
     * should be compared using Java identity, or using {@link SameOrEqlNode} semantics. */
    @Abstract
    public abstract boolean set(Object store, RubyHash hash, Object key, Object value, boolean byIdentity);
}
