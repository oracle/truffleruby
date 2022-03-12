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

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class SuperCallNode extends RubyContextSourceNode {

    @Child private RubyNode arguments;
    @Child private RubyNode block;
    private final ArgumentsDescriptor descriptor;
    @CompilationFinal private ConditionProfile emptyProfile;
    @Child private LookupSuperMethodNode lookupSuperMethodNode;
    @Child private CallSuperMethodNode callSuperMethodNode;

    public SuperCallNode(RubyNode arguments, RubyNode block, ArgumentsDescriptor descriptor) {
        this.arguments = arguments;
        this.block = block;
        this.descriptor = descriptor;
    }

    @Override
    public final Object execute(VirtualFrame frame) {
        final Object self = RubyArguments.getSelf(frame);

        // Execute the arguments
        Object[] superArguments = (Object[]) arguments.execute(frame);

        // Remove empty kwargs in the caller, so the callee does not need to care about this special case
        ArgumentsDescriptor descriptor = this.descriptor;
        if (this.descriptor instanceof KeywordArgumentsDescriptor &&
                profileEmptyHash(((RubyHash) ArrayUtils.getLast(superArguments)).empty())) {
            superArguments = ArrayUtils.extractRange(superArguments, 0, superArguments.length - 1);
            descriptor = EmptyArgumentsDescriptor.INSTANCE;
        }

        // Execute the block
        final Object blockObject = block.execute(frame);

        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        if (callSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callSuperMethodNode = insert(CallSuperMethodNode.create());
        }

        return callSuperMethodNode.execute(frame, self, superMethod, descriptor, superArguments, blockObject);
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

    private boolean profileEmptyHash(boolean condition) {
        if (emptyProfile == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            emptyProfile = /* not a node, so no insert() */ ConditionProfile.create();
        }

        return emptyProfile.profile(condition);
    }

    private InternalMethod executeLookupSuperMethod(VirtualFrame frame, Object self) {
        if (lookupSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupSuperMethodNode = insert(LookupSuperMethodNodeGen.create());
        }
        return lookupSuperMethodNode.executeLookupSuperMethod(frame, self);
    }

}
