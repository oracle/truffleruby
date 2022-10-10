/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.InternalRespondToNode;

public class ShouldDestructureNode extends RubyContextSourceNode {

    @Child private InternalRespondToNode respondToToAry;

    private final boolean keywordArguments;
    private final BranchProfile checkIsArrayProfile = BranchProfile.create();

    public ShouldDestructureNode(boolean keywordArguments) {
        this.keywordArguments = keywordArguments;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (RubyArguments.getDescriptor(frame) instanceof KeywordArgumentsDescriptor) {
            return false;
        }

        if (RubyArguments.getPositionalArgumentsCount(frame, keywordArguments) != 1) {
            return false;
        }

        checkIsArrayProfile.enter();

        final Object firstArgument = RubyArguments.getArgument(frame, 0);

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

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new ShouldDestructureNode(keywordArguments);
        return copy.copyFlags(this);
    }

}
