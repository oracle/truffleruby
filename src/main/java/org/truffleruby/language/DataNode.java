/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

public class DataNode extends RubyContextSourceNode {

    @Child private StringNodes.MakeStringNode makeStringNode;
    @Child private DispatchNode callHelperNode;

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
            callHelperNode = insert(DispatchNode.create());
        }

        final String path = getPath();
        final RubyEncoding rubyLocaleEncoding = getContext().getEncodingManager().getLocaleEncoding();
        final RubyString pathString = makeStringNode.executeMake(path, rubyLocaleEncoding, CodeRange.CR_UNKNOWN);
        final Object data = callHelperNode
                .call(coreLibrary().truffleInternalModule, "get_data", pathString, endPosition);

        coreLibrary().objectClass.fields.setConstant(getContext(), null, "DATA", data);

        return nil;
    }

    @TruffleBoundary
    private String getPath() {
        return getLanguage().getSourcePath(getEncapsulatingSourceSection().getSource());
    }

}
