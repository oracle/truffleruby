/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild(value = "array", type = RubyNode.class)
@NodeChild(value = "index", type = RubyNode.class)
@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayReadNormalizedNode extends RubyContextSourceNode {

    public static ArrayReadNormalizedNode create() {
        return ArrayReadNormalizedNodeGen.create(null, null);
    }

    public abstract Object executeRead(DynamicObject array, int index);

    // Read within the bounds of an array with actual storage

    @Specialization(
            guards = "isInBounds(array, index)",
            limit = "STORAGE_STRATEGIES")
    protected Object readInBounds(DynamicObject array, int index,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {
        return arrays.read(Layouts.ARRAY.getStore(array), index);
    }

    // Reading out of bounds is nil for any array

    @Specialization(guards = "!isInBounds(array, index)")
    protected Object readOutOfBounds(DynamicObject array, int index) {
        return nil;
    }

    // Guards

    protected static boolean isInBounds(DynamicObject array, int index) {
        return index >= 0 && index < Layouts.ARRAY.getSize(array);
    }
}
