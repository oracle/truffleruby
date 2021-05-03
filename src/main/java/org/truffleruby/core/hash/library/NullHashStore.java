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
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.PackedArrayStrategy;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class NullHashStore {

    public static final NullHashStore NULL_HASH_STORE = new NullHashStore();

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode) {
        return defaultNode.accept((VirtualFrame) frame, hash, key);
    }

    // TODO the theory is that this PEs as nicely as the original (which manually inserts the entry in the packed array)
    //   verify this :)
    @ExportMessage
    protected boolean set(RubyHash hash, Object key, Object value, boolean byIdentity,
            @CachedLanguage RubyLanguage language,
            @CachedLibrary(limit = "1") HashStoreLibrary packedHashStoreLibrary) {
        final Object[] packedStore = PackedArrayStrategy.createStore(language);
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
    protected void each(RubyHash hash, RubyProc block) {
        // nothing to do, the hash is empty
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

        assert HashOperations.verifyStore(context, dest);
    }
}
