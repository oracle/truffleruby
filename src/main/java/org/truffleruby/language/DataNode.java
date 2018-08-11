/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.Layouts;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

public class DataNode extends RubyNode {

    @Child private StringNodes.MakeStringNode makeStringNode;
    @Child private CallDispatchHeadNode callHelperNode;

    private final int endPosition;

    public DataNode(int endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (makeStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            makeStringNode = insert(StringNodes.MakeStringNode.create());
        }

        if (callHelperNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callHelperNode = insert(CallDispatchHeadNode.createOnSelf());
        }

        final String path = getPath();
        final Object data = callHelperNode.call(coreLibrary().getTruffleInternalModule(), "get_data", makeStringNode.executeMake(path, getContext().getEncodingManager().getLocaleEncoding(), CodeRange.CR_UNKNOWN),
                endPosition);

        Layouts.MODULE.getFields(coreLibrary().getObjectClass()).setConstant(getContext(), null, "DATA", data);

        return nil();
    }

    @TruffleBoundary
    private String getPath() {
        return getContext().getSourceLoader().getAbsolutePath(getEncapsulatingSourceSection().getSource());
    }

}
