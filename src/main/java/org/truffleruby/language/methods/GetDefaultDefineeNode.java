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

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.objects.SingletonClassNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class GetDefaultDefineeNode extends RubyNode {

    @Child private SingletonClassNode singletonClassNode;

    @Override
    public DynamicObject execute(VirtualFrame frame) {
        final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
        return declarationContext.getModuleToDefineMethods(getSingletonClassNode());
    }

    private SingletonClassNode getSingletonClassNode() {
        if (singletonClassNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            singletonClassNode = insert(SingletonClassNode.create());
        }
        return singletonClassNode;
    }
}
