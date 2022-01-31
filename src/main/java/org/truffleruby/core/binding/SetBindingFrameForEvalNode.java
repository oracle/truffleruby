/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

public class SetBindingFrameForEvalNode extends RubyContextSourceNode {

    @Child RubyNode body;

    private final FrameDescriptor descriptor;

    public SetBindingFrameForEvalNode(FrameDescriptor descriptor, RubyNode body) {
        this.descriptor = descriptor;
        this.body = body;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        assert frame.getFrameDescriptor() == descriptor;

        assert RubyArguments.getArgumentsCount(frame) == 1;
        RubyBinding binding = (RubyBinding) RubyArguments.getArgument(frame, 0);

        assert RubyArguments.getDeclarationFrame(frame) == binding.getFrame();
        assert binding.getFrame().getFrameDescriptor() != descriptor;
        binding.setFrame(frame.materialize());

        return body.execute(frame);
    }
}
