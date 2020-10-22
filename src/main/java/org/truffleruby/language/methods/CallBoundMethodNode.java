/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;

@GenerateUncached
public abstract class CallBoundMethodNode extends RubyBaseNode {

    public static CallBoundMethodNode create() {
        return CallBoundMethodNodeGen.create();
    }

    public abstract Object executeCallBoundMethod(RubyMethod method, Object[] arguments, Object block);

    @Specialization
    protected Object call(RubyMethod method, Object[] arguments, Object block,
            @Cached ProcOrNullNode procOrNullNode,
            @Cached CallInternalMethodNode callInternalMethodNode) {
        final InternalMethod internalMethod = method.method;
        final RubyProc typedBlock = procOrNullNode.executeProcOrNull(block);
        final Object[] frameArguments = packArguments(method, internalMethod, arguments, typedBlock);

        return callInternalMethodNode.execute(internalMethod, frameArguments);
    }

    private Object[] packArguments(RubyMethod method, InternalMethod internalMethod, Object[] arguments,
            RubyProc block) {
        return RubyArguments.pack(
                null,
                null,
                null,
                internalMethod,
                null,
                method.receiver,
                block,
                arguments);
    }
}
