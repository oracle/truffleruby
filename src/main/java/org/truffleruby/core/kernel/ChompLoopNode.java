/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadGlobalVariableNode;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;

public class ChompLoopNode extends RubyContextSourceNode {

    @Child private DispatchNode callChompNode = DispatchNode.create();
    @Child private ReadGlobalVariableNode readGlobalVariableNode = ReadGlobalVariableNodeGen.create("$_");

    @Override
    public Object execute(VirtualFrame frame) {
        // $_.chomp!
        final Object lastLine = readGlobalVariableNode.execute(frame);
        return callChompNode.call(lastLine, "chomp!");
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new ChompLoopNode();
        return copy.copyFlags(this);
    }

}
