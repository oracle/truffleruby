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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class NullHashStore {

    public static final NullHashStore NULL_HASH_STORE = new NullHashStore();

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, PEBiFunction defaultNode) {
        return defaultNode.accept((VirtualFrame) frame, hash, key);
    }

    // TODO the theory is that this PEs as nicely as the original (which manually inserts the entry in the packed array)
    //   verify this :)
    @ExportMessage
    protected boolean set(RubyHash hash, Object key, Object value, boolean byIdentity,
            @CachedLanguage RubyLanguage language,
            @CachedLibrary(limit = "1") HashStoreLibrary packedHashStoreLibrary) {
        final Object[] packedStore = PackedHashStoreLibrary.createStore(language);
        hash.store = packedStore;
        return packedHashStoreLibrary.set(packedStore, hash, key, value, byIdentity);
    }

    @ExportMessage
    protected void clear(RubyHash hash) {
        // nothing to do, the hash is already empty
    }

    @ExportMessage
    protected Object delete(RubyHash hash, Object key) {
        return null;
    }

    @ExportMessage
    protected Object deleteLast(RubyHash hash, Object key) {
        throw CompilerDirectives.shouldNotReachHere("Cannot delete the last node of an empty hash");
    }

    @ExportMessage
    protected Object eachEntry(Frame frame, RubyHash hash, EachEntryCallback callback, Object state) {
        return state;
    }

    @ExportMessage
    protected Object eachEntrySafe(Frame frame, RubyHash hash, EachEntryCallback callback, Object state) {
        return state;
    }

    @ExportMessage
    protected void replace(RubyHash hash, RubyHash dest,
            @Cached PropagateSharingNode propagateSharing,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        if (hash == dest) {
            return;
        }
        propagateSharing.executePropagate(dest, hash);
        dest.store = NullHashStore.NULL_HASH_STORE;
        dest.size = 0;
        dest.firstInSequence = null;
        dest.lastInSequence = null;
        dest.defaultBlock = hash.defaultBlock;
        dest.defaultValue = hash.defaultValue;
        dest.compareByIdentity = hash.compareByIdentity;
        assert verify(hash);
    }

    @ExportMessage
    protected RubyArray shift(RubyHash hash) {
        return null;
    }

    @ExportMessage
    protected void rehash(RubyHash hash) {
        // nothing to do, the hash is empty
    }

    @ExportMessage
    public boolean verify(RubyHash hash) {
        assert hash.store == NULL_HASH_STORE;
        assert hash.size == 0;
        assert hash.firstInSequence == null;
        assert hash.lastInSequence == null;
        return true;
    }

    public static class EmptyHashLiteralNode extends HashLiteralNode {

        public EmptyHashLiteralNode(RubyLanguage language) {
            super(language, RubyNode.EMPTY_ARRAY);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return HashOperations.newEmptyHash(getContext(), getLanguage());
        }
    }
}
