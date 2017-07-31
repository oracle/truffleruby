/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.Layouts;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;

public class DataNode extends RubyNode {

    @Child private StringNodes.MakeStringNode makeStringNode;
    @Child private SnippetNode snippetNode;

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

        if (snippetNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            snippetNode = insert(new SnippetNode());
        }

        final String path = getEncapsulatingSourceSection().getSource().getPath();
        final Object data = snippetNode.execute(frame,
                "Truffle.get_data(file, offset)",
                "file", makeStringNode.executeMake(path, getContext().getEncodingManager().getLocaleEncoding(), CodeRange.CR_UNKNOWN),
                "offset", endPosition);

        Layouts.MODULE.getFields(coreLibrary().getObjectClass()).setConstant(getContext(), null, "DATA", data);

        return nil();
    }

}
