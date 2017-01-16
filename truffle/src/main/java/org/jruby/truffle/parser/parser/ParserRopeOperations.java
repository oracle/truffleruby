/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.parser.parser;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jcodings.Encoding;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.rope.RopeNodes;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.parser.ParserByteList;

import static org.jruby.truffle.core.rope.CodeRange.CR_UNKNOWN;

public class ParserRopeOperations {

    public ParserByteList withEncoding(ParserByteList rope, Encoding encoding) {
        final Rope newRope = parserByteListNode.getWithEncodingNode().executeWithEncoding(rope.toRope(), encoding, CR_UNKNOWN);

        if (newRope == rope.toRope()) {
            return rope;
        }

        return new ParserByteList(newRope);
    }

    public ParserByteList makeShared(ParserByteList rope, int sharedStart, int sharedLength) {
        final Rope newRope = parserByteListNode.getMakeSubstringNode().executeMake(rope.toRope(), sharedStart, sharedLength);

        if (newRope == rope.toRope()) {
            return rope;
        }

        return new ParserByteList(newRope);
    }

    private static class ParserByteListNode extends RubyNode {

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

    private final ParserByteListNode parserByteListNode = new ParserByteListNode();

}
