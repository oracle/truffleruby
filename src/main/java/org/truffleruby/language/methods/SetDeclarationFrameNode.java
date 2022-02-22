/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

public class SetDeclarationFrameNode extends RubyContextSourceNode {

    private final MaterializedFrame declarationFrame;

    @Child private RubyNode body;

    public SetDeclarationFrameNode(MaterializedFrame declarationFrame, RubyNode body) {
        this.declarationFrame = declarationFrame;
        this.body = body;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        RubyArguments.setDeclarationFrame(frame, declarationFrame);
        return body.execute(frame);
    }

}
