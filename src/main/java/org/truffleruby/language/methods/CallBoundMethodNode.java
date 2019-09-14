/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.Layouts;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@GenerateUncached
public abstract class CallBoundMethodNode extends RubyBaseWithoutContextNode {

    public static CallBoundMethodNode create() {
        return CallBoundMethodNodeGen.create();
    }

    public abstract Object executeCallBoundMethod(DynamicObject method, Object[] arguments, Object block);

    @Specialization
    protected Object call(DynamicObject method, Object[] arguments, Object block,
            @Cached ProcOrNullNode procOrNullNode,
            @Cached CallInternalMethodNode callInternalMethodNode) {
        final InternalMethod internalMethod = Layouts.METHOD.getMethod(method);
        final DynamicObject typedBlock = procOrNullNode.executeProcOrNull(block);
        final Object[] frameArguments = packArguments(method, internalMethod, arguments, typedBlock);

        return callInternalMethodNode.executeCallMethod(internalMethod, frameArguments);
    }

    private Object[] packArguments(DynamicObject method, InternalMethod internalMethod, Object[] arguments,
            DynamicObject block) {
        return RubyArguments.pack(
                null,
                null,
                internalMethod,
                null,
                Layouts.METHOD.getReceiver(method),
                block,
                arguments);
    }
}
