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

import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadBlockFromCurrentFrameArgumentsNode extends RubyContextSourceNode {

    @Override
    public Object execute(VirtualFrame frame) {
        final Object block = RubyArguments.getBlock(frame);
        assert block instanceof Nil || block instanceof RubyProc : block;
        return block;
    }

    public static class ConvertNilBlockToNotProvidedNode extends RubyContextSourceNode {

        @Child RubyNode child;

        private final ConditionProfile nilProfile = ConditionProfile.createBinaryProfile();

        public ConvertNilBlockToNotProvidedNode(RubyNode child) {
            this.child = child;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object block = child.execute(frame);

            assert block instanceof Nil || block instanceof RubyProc : block;

            if (nilProfile.profile(block instanceof Nil)) {
                return NotProvided.INSTANCE;
            } else {
                return block;
            }
        }

    }

}
