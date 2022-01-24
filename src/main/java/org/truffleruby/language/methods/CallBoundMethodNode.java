/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;

@GenerateUncached
public abstract class CallBoundMethodNode extends RubyBaseNode {

    public static CallBoundMethodNode create() {
        return CallBoundMethodNodeGen.create();
    }

    public abstract Object execute(Frame frame, Object receiver, Object[] rubyArgs);

    @Specialization
    protected Object call(Frame frame, Object receiver, Object[] rubyArgs,
            @Cached CallInternalMethodNode callInternalMethodNode) {
        final RubyMethod method = ((RubyMethod) receiver);
        final InternalMethod internalMethod = method.method;
        final Object[] newArgs = RubyArguments.repack(rubyArgs, method.receiver, 0,
                RubyArguments.getArgumentsCount(rubyArgs));
        RubyArguments.setMethod(newArgs, internalMethod);
        return callInternalMethodNode.execute(frame, newArgs);
    }

}
