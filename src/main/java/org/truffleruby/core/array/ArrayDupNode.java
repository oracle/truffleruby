/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.Shape;

/** Dup an array, without using any method lookup. This isn't a call - it's an operation on a core class. */
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDupNode extends RubyContextNode {

    @Child private AllocateHelperNode helperNode;

    public abstract RubyArray executeDup(VirtualFrame frame, RubyArray array);

    @Specialization(
            guards = {
                    "from.size == cachedSize",
                    "cachedSize <= ARRAY_MAX_EXPLODE_SIZE" },
            limit = "getCacheLimit()")
    protected RubyArray dupProfiledSize(RubyArray from,
            @CachedLibrary("from.store") ArrayStoreLibrary fromStores,
            @CachedLibrary(limit = "1") ArrayStoreLibrary toStores,
            @Cached("from.size") int cachedSize,
            @CachedLanguage RubyLanguage language) {
        return copyArraySmall(language, fromStores, toStores, from, cachedSize);
    }

    @ExplodeLoop
    private RubyArray copyArraySmall(RubyLanguage language,
            ArrayStoreLibrary fromStores,
            ArrayStoreLibrary toStores,
            RubyArray from,
            int cachedSize) {
        final Object original = from.store;
        final Object copy = fromStores.allocator(original).allocate(cachedSize);
        for (int i = 0; i < cachedSize; i++) {
            toStores.write(copy, i, fromStores.read(original, i));
        }
        return allocateArray(language, coreLibrary().arrayClass, RubyLanguage.arrayShape, copy, cachedSize);
    }

    @Specialization(replaces = "dupProfiledSize")
    protected RubyArray dup(RubyArray from,
            @Cached ArrayCopyOnWriteNode cowNode,
            @CachedLanguage RubyLanguage language) {
        final int size = from.size;
        final Object copy = cowNode.execute(from, 0, from.size);
        return allocateArray(language, coreLibrary().arrayClass, RubyLanguage.arrayShape, copy, size);
    }

    private RubyArray allocateArray(RubyLanguage language, RubyClass rubyClass, Shape arrayShape, Object store,
            int size) {
        if (helperNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            helperNode = insert(AllocateHelperNode.create());
        }
        RubyArray array = new RubyArray(rubyClass, arrayShape, store, size);
        helperNode.trace(array, this, language);
        return array;
    }

    protected int getCacheLimit() {
        return getContext().getOptions().ARRAY_DUP_CACHE;
    }

}
