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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.SingletonClassNodeGen;

public class GetDefaultDefineeNode extends RubyNode {

    @Child private SingletonClassNode singletonClassNode = SingletonClassNodeGen.create(null);

    @Override
    public DynamicObject execute(VirtualFrame frame) {
        return getDefaultDefinee(RubyArguments.getMethod(frame), RubyArguments.getSelf(frame), RubyArguments.getDeclarationContext(frame));
    }

    @TruffleBoundary
    public DynamicObject getDefaultDefinee(InternalMethod method, Object self, DeclarationContext declarationContext) {
        final DynamicObject capturedDefaultDefinee = method.getCapturedDefaultDefinee();

        if (capturedDefaultDefinee != null) {
            return capturedDefaultDefinee;
        }

        return declarationContext.getModuleToDefineMethods(self, method, getContext(), singletonClassNode);
    }
}
