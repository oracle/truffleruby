/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadGlobalVariableNode;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class KernelPrintLastLineNode extends RubyContextSourceNode {

    @Child private DispatchNode callPrintNode;
    @Child private ReadGlobalVariableNode readGlobalVariableNode;

    @Override
    public Object execute(VirtualFrame frame) {
        if (callPrintNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callPrintNode = insert(DispatchNode.create());
        }
        if (readGlobalVariableNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readGlobalVariableNode = insert(ReadGlobalVariableNodeGen.create("$_"));
        }
        final Object lastLine = readGlobalVariableNode.execute(frame);
        return callPrintNode.call(coreLibrary().kernelModule, "print", lastLine);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new KernelPrintLastLineNode();
        copy.copyFlags(this);
        return copy;
    }

}
