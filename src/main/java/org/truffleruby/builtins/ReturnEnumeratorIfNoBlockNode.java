/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReturnEnumeratorIfNoBlockNode extends RubyContextSourceNode {

    private final String methodName;
    @Child private RubyNode method;
    @Child private DispatchNode toEnumNode;
    @CompilationFinal private RubySymbol methodSymbol;
    private final ConditionProfile noBlockProfile = ConditionProfile.create();

    public ReturnEnumeratorIfNoBlockNode(String methodName, RubyNode method) {
        this.methodName = methodName;
        this.method = method;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object block = RubyArguments.getBlock(frame);

        if (noBlockProfile.profile(block == nil())) {
            if (toEnumNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toEnumNode = insert(DispatchNode.create());
            }

            if (methodSymbol == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                methodSymbol = getSymbol(methodName);
            }

            final Object receiver = RubyArguments.getSelf(frame);
            final Object[] rubyArgs = RubyArguments.repack(frame.getArguments(), receiver, 0, 1);
            RubyArguments.setArgument(rubyArgs, 0, methodSymbol);

            return toEnumNode.dispatch(null, receiver, "to_enum", rubyArgs);
        } else {
            return method.execute(frame);
        }
    }

}
