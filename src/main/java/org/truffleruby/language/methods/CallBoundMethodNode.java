/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.ProcOrNullNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

@NodeChildren({
        @NodeChild("method"),
        @NodeChild("arguments"),
        @NodeChild("block")
})
public abstract class CallBoundMethodNode extends RubyNode {

    @Child private CallInternalMethodNode callInternalMethodNode = CallInternalMethodNodeGen.create(null, null);
    @Child private ProcOrNullNode procOrNullNode = ProcOrNullNodeGen.create(null);

    public abstract Object executeCallBoundMethod(DynamicObject method, Object[] arguments, Object block);

    @Specialization
    protected Object call(DynamicObject method, Object[] arguments, Object block) {
        final InternalMethod internalMethod = Layouts.METHOD.getMethod(method);
        final DynamicObject typedBlock = procOrNullNode.executeProcOrNull(block);
        final Object[] frameArguments = packArguments(method, internalMethod, arguments, typedBlock);

        return callInternalMethodNode.executeCallMethod(internalMethod, frameArguments);
    }

    private Object[] packArguments(DynamicObject method, InternalMethod internalMethod, Object[] arguments, DynamicObject block) {
        return RubyArguments.pack(
                null,
                null,
                internalMethod,
                DeclarationContext.METHOD,
                null,
                Layouts.METHOD.getReceiver(method),
                block,
                arguments);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().DISPATCH_CACHE;
    }
}
