/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.yield;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.language.RubyBaseWithoutContextNode;

@GenerateUncached
public abstract class YieldNode extends RubyBaseWithoutContextNode {

    public static YieldNode create() {
        return YieldNodeGen.create();
    }

    public final Object executeDispatch(DynamicObject block, Object... argumentsObjects) {
        return executeDispatchWithArrayArguments(block, argumentsObjects);
    }

    public abstract Object executeDispatchWithArrayArguments(DynamicObject block, Object[] argumentsObjects);

    @Specialization
    public Object Dispatch(DynamicObject block, Object[] argumentsObjects,
            @Cached CallBlockNode callBlockNode) {
        return callBlockNode.executeCallBlock(
                Layouts.PROC.getDeclarationContext(block),
                block,
                ProcOperations.getSelf(block),
                null,
                argumentsObjects);
    }
}
