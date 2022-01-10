/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class SuperCallNode extends RubyContextSourceNode {

    @Child private RubyNode arguments;
    @Child private RubyNode block;
    @Child private LookupSuperMethodNode lookupSuperMethodNode;
    @Child private CallSuperMethodNode callSuperMethodNode;

    public SuperCallNode(RubyNode arguments, RubyNode block) {
        this.arguments = arguments;
        this.block = block;
    }

    @Override
    public final Object execute(VirtualFrame frame) {
        final Object self = RubyArguments.getSelf(frame);

        // Execute the arguments
        final Object[] superArguments = (Object[]) arguments.execute(frame);

        // Execute the block
        final Object blockObject = block.execute(frame);

        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        if (callSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callSuperMethodNode = insert(CallSuperMethodNode.create());
        }
        return callSuperMethodNode.execute(frame, self, superMethod, superArguments, blockObject);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        final Object self = RubyArguments.getSelf(frame);
        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        if (superMethod == null) {
            return nil;
        } else {
            return FrozenStrings.SUPER;
        }
    }

    private InternalMethod executeLookupSuperMethod(VirtualFrame frame, Object self) {
        if (lookupSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupSuperMethodNode = insert(LookupSuperMethodNodeGen.create());
        }
        return lookupSuperMethodNode.executeLookupSuperMethod(frame, self);
    }

}
