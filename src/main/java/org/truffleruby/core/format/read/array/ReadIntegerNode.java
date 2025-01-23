/*
 * Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.array;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.convert.ToIntegerNode;
import org.truffleruby.core.format.convert.ToIntegerNodeGen;
import org.truffleruby.core.format.read.SourceNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "source", type = SourceNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ReadIntegerNode extends FormatNode {

    @Child private ToIntegerNode toIntegerNode;

    private final ConditionProfile convertedTypeProfile = ConditionProfile.create();

    @Specialization(limit = "storageStrategyLimit()")
    Object read(VirtualFrame frame, Object source,
            @CachedLibrary("source") ArrayStoreLibrary sources) {
        if (toIntegerNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toIntegerNode = insert(ToIntegerNodeGen.create(null));
        }

        final Object value = toIntegerNode
                .executeToInteger(frame, sources.read(source, advanceSourcePosition(frame)));

        if (convertedTypeProfile.profile(value instanceof Long)) {
            return (int) (long) value;
        } else {
            return (int) value;
        }
    }

}
