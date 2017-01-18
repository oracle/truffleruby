/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.parser.parser;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.language.RubyNode;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

public class ParserRopeOperations {

    public Rope withEncoding(Rope rope, Encoding encoding) {
        final Rope newRope = RopeNode.getWithEncodingNode().executeWithEncoding(rope, encoding, CR_UNKNOWN);

        if (newRope == rope) {
            return rope;
        }

        return newRope;
    }

    public Rope makeShared(Rope rope, int sharedStart, int sharedLength) {
        final Rope newRope = RopeNode.getMakeSubstringNode().executeMake(rope, sharedStart, sharedLength);

        if (newRope == rope) {
            return rope;
        }

        return newRope;
    }

    private static class RopeNode extends RubyNode {

        @Child RopeNodes.MakeSubstringNode makeSubstringNode;
        @Child RopeNodes.WithEncodingNode withEncodingNode;

        public RopeNodes.MakeSubstringNode getMakeSubstringNode() {
            if (makeSubstringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeSubstringNode = insert(RopeNodes.MakeSubstringNode.create());
            }

            return makeSubstringNode;
        }

        public RopeNodes.WithEncodingNode getWithEncodingNode() {
            if (withEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                withEncodingNode = insert(RopeNodes.WithEncodingNode.create());
            }

            return withEncodingNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return nil();
        }

    }

    private final RopeNode RopeNode = new RopeNode();

}
