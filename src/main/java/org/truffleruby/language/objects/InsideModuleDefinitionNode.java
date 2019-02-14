/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.Tag;

@GenerateWrapper
public class InsideModuleDefinitionNode extends RubyNode {

    @Child private RubyNode body;

    public InsideModuleDefinitionNode() {
        // The instrumentation wrapper requires a no-arg constructor
    }

    public InsideModuleDefinitionNode(RubyNode body) {
        this.body = body;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return body.execute(frame);
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probeNode) {
        return new InsideModuleDefinitionNodeWrapper(this, probeNode);
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == TraceManager.ClassTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }

}
