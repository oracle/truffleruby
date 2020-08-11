/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.yield;

import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class YieldNode extends RubyBaseNode {

    public static YieldNode create() {
        return YieldNodeGen.create();
    }

    public static YieldNode getUncached() {
        return YieldNodeGen.getUncached();
    }

    public final Object executeDispatch(RubyProc block, Object... args) {
        return executeDispatchWithArrayArguments(block, args);
    }

    public abstract Object executeDispatchWithArrayArguments(RubyProc block, Object[] argumentsObjects);

    @Specialization
    protected Object dispatch(RubyProc block, Object[] argumentsObjects,
            @Cached CallBlockNode callBlockNode) {
        return callBlockNode.executeCallBlock(
                block.declarationContext,
                block,
                ProcOperations.getSelf(block),
                null,
                argumentsObjects);
    }
}
