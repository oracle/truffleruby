/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.frame.VirtualFrame;

/** Load libraries required from the command line (-r LIBRARY) */
public class LoadRequiredLibrariesNode extends RubyNode {

    @Child private CallDispatchHeadNode requireNode = CallDispatchHeadNode.createPrivate();
    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    @Override
    public Object execute(VirtualFrame frame) {
        Object self = RubyArguments.getSelf(frame);

        for (String requiredLibrary : getContext().getOptions().REQUIRED_LIBRARIES) {
            requireNode.call(
                    self,
                    "require",
                    makeStringNode.executeMake(requiredLibrary, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
        }

        return nil();
    }

}
