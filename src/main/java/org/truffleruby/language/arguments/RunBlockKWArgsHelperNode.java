/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.locals.ReadFrameSlotNode;
import org.truffleruby.language.locals.ReadFrameSlotNodeGen;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class RunBlockKWArgsHelperNode extends RubyContextSourceNode {

    @Child private ReadFrameSlotNode readArrayNode;
    @Child private WriteFrameSlotNode writeArrayNode;
    @Child private DispatchNode callHelperNode;
    @Child private NotOptimizedWarningNode notOptimizedWarningNode = NotOptimizedWarningNode.create();

    private final Object kwrestName;

    public RunBlockKWArgsHelperNode(FrameSlot arrayFrameSlot, Object kwrestName) {
        readArrayNode = ReadFrameSlotNodeGen.create(arrayFrameSlot);
        writeArrayNode = WriteFrameSlotNodeGen.create(arrayFrameSlot);
        callHelperNode = DispatchNode.create();
        this.kwrestName = kwrestName;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notOptimizedWarningNode
                .warn("keyword rest argument in combination with masgn or destructuring is not yet optimized");

        final Object array = readArrayNode.executeRead(frame);

        final RubyBinding binding = BindingNodes.createBinding(getContext(), getLanguage(), frame.materialize());
        final Object remainingArray = callHelperNode.call(
                coreLibrary().truffleInternalModule,
                "load_arguments_from_array_kw_helper",
                array,
                kwrestName,
                binding);

        writeArrayNode.executeWrite(frame, remainingArray);

        return nil;
    }

}
