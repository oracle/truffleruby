/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SnippetNode;
import org.truffleruby.language.locals.ReadFrameSlotNode;
import org.truffleruby.language.locals.ReadFrameSlotNodeGen;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;

public class RunBlockKWArgsHelperNode extends RubyNode {

    @Child private ReadFrameSlotNode readArrayNode;
    @Child private WriteFrameSlotNode writeArrayNode;
    @Child private SnippetNode snippetNode;

    private final Object kwrestName;

    public RunBlockKWArgsHelperNode(FrameSlot arrayFrameSlot, Object kwrestName) {
        readArrayNode = ReadFrameSlotNodeGen.create(arrayFrameSlot);
        writeArrayNode = WriteFrameSlotNodeGen.create(arrayFrameSlot);
        this.kwrestName = kwrestName;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Log.notOptimizedOnce(Log.KWARGS_NOT_OPTIMIZED_YET);

        final Object array = readArrayNode.executeRead(frame);

        if (snippetNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            snippetNode = insert(new SnippetNode());
        }

        final Object remainingArray = snippetNode.execute(
                frame,
                "Truffle.load_arguments_from_array_kw_helper(array, kwrest_name, binding)",
                "array", array,
                "kwrest_name", kwrestName,
                "binding", Layouts.BINDING.createBinding(coreLibrary().getBindingFactory(), frame.materialize(), null));

        writeArrayNode.executeWrite(frame, remainingArray);

        return nil();
    }

}
