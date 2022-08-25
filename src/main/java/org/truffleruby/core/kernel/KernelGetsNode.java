/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class KernelGetsNode extends RubyContextSourceNode {

    @Child private DispatchNode callGetsNode;

    @Override
    public Object execute(VirtualFrame frame) {
        if (callGetsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callGetsNode = insert(DispatchNode.create());
        }
        return callGetsNode.callWithFrame(frame, coreLibrary().kernelModule, "gets");
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new KernelGetsNode();
        copy.copyFlags(this);
        return copy;
    }

}
