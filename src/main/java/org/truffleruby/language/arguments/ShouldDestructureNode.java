/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.ReadKeywordDescriptorNode;
import org.truffleruby.language.dispatch.InternalRespondToNode;

@NodeChild("descriptor")
public abstract class ShouldDestructureNode extends RubyContextSourceNode {

    @Child private InternalRespondToNode respondToToAry;

    private final BranchProfile checkIsArrayProfile = BranchProfile.create();

    protected ShouldDestructureNode() {
    };

    public static ShouldDestructureNode create() {
        return ShouldDestructureNodeGen.create(new ReadKeywordDescriptorNode());
    }

    public abstract Object execute(VirtualFrame frame, KeywordDescriptor descriptor);

    @Specialization
    protected Object empty(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        return shouldDestructureNode(frame, descriptor);
    }

    @Specialization
    protected Object nonEmpty(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        return shouldDestructureNode(frame, descriptor);
    }

    public Object shouldDestructureNode(VirtualFrame frame, KeywordDescriptor descriptor) {
        if (RubyArguments.getArgumentsCount(frame, descriptor) != 1) {
            return false;
        }

        checkIsArrayProfile.enter();

        final Object firstArgument = RubyArguments.getArgument(frame, 0, descriptor);

        if (RubyGuards.isRubyArray(firstArgument)) {
            return true;
        }

        if (respondToToAry == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            respondToToAry = insert(InternalRespondToNode.create());
        }

        // TODO(cseaton): check this is actually a static "find if there is such method" and not a
        // dynamic call to respond_to?
        return respondToToAry.execute(frame, firstArgument, "to_ary");
    }

}
