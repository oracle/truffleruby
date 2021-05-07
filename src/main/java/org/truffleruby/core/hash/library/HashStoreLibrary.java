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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.PEBiConsumer;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.HashGuards;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.yield.CallBlockNode;

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
            PEBiFunction defaultNode);

    /** Associates the key with the value and returns true only if the hash changed as a result of the operation (i.e.
     * returns false if the key was already associated with the value). {@code byIdentity} indicates whether the key
     * should be compared using Java identity, or using {@link SameOrEqlNode} semantics. */
    @Abstract
    public abstract boolean set(Object store, RubyHash hash, Object key, Object value, boolean byIdentity);

    public void clear(Object store, RubyHash hash) {
        hash.store = NullHashStore.NULL_HASH_STORE;
        hash.size = 0;
        hash.firstInSequence = null;
        hash.lastInSequence = null;
    }

    /** Removes the entry for the key from the hash, and returns the associated value. If the hash does not have an
     * entry for the key, returns {@code null}. */
    @Abstract
    public abstract Object delete(Object store, RubyHash hash, Object key);

    /** Calls {@code callback} on every entry in the hash. */
    @Abstract
    public abstract Object eachEntry(Object store, Frame frame, RubyHash hash, PEBiConsumer callback,
            Object state);

    /** Runs the given block over every entry, in the manner specified by {@link YieldPairNode}. */
    @Abstract
    public abstract void each(Object store, RubyHash hash, RubyProc block);

    /** Replaces the contents of {@code dest} with a copy of {@code hash}. */
    @Abstract
    public abstract void replace(Object store, RubyHash hash, RubyHash dest);

    /** Returns a ruby array containing the result of applying {@code block} on every hash entry, in the manner
     * specified by {@link YieldPairNode}. */
    @Abstract
    public abstract RubyArray map(Object store, RubyHash hash, RubyProc block);

    /** Removes a key-value pair from the hash and returns it as the two-item array [key, value], or null if the hash is
     * empty. */
    @Abstract
    public abstract RubyArray shift(Object store, RubyHash hash);

    /** Re-hashes the keys in the hash (if the keys are mutable objects, then changes to these objects may change the
     * hash. */
    @Abstract
    public abstract void rehash(Object store, RubyHash hash);

    /** Call the block with an key-value entry. If the block has > 1 arity, passes the key and the value as arguments,
     * otherwise passes an array containing the key and the value as single argument. */
    @GenerateUncached
    public abstract static class YieldPairNode extends RubyBaseNode {
        public abstract Object execute(RubyProc block, Object key, Object value);

        @Specialization
        protected Object yieldPair(RubyProc block, Object key, Object value,
                @CachedLanguage RubyLanguage language,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached CallBlockNode yieldNode,
                @Cached ConditionProfile arityMoreThanOne) {
            // MRI behavior, see rb_hash_each_pair()
            // We use getMethodArityNumber() here since for non-lambda the semantics are the same for both branches
            if (arityMoreThanOne.profile(block.sharedMethodInfo.getArity().getMethodArityNumber() > 1)) {
                return yieldNode.yield(block, key, value);
            } else {
                return yieldNode.yield(block, ArrayHelpers.createArray(context, language, new Object[]{ key, value }));
            }
        }
    }

    /** Useful for library implementations. */
    public static void reportLoopCount(HashStoreLibrary self, int n) {
        final Node node = self.isAdoptable() ? self : EncapsulatingNodeReference.getCurrent().get();
        LoopNode.reportLoopCount(node, n);
    }
}
