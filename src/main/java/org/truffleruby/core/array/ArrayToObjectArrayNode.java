/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayToObjectArrayNode extends RubyBaseNode {

    public static ArrayToObjectArrayNode create() {
        return ArrayToObjectArrayNodeGen.create();
    }

    public Object[] unsplat(Object[] arguments) {
        assert arguments.length == 1;
        assert RubyGuards.isRubyArray(arguments[0]);
        return executeToObjectArray((DynamicObject) arguments[0]);
    }

    public abstract Object[] executeToObjectArray(DynamicObject array);

    @Specialization(guards = "strategy.matches(array)", limit = "STORAGE_STRATEGIES")
    protected Object[] toObjectArrayOther(DynamicObject array,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.boxedCopyNode()") ArrayOperationNodes.ArrayBoxedCopyNode boxedCopyNode) {
        final int size = strategy.getSize(array);
        return boxedCopyNode.execute(Layouts.ARRAY.getStore(array), size);
    }

}
