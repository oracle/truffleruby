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
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyNode;

import java.util.Arrays;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

public class ParserRopeOperations {

    public Rope withEncoding(Rope rope, Encoding encoding) {
        if (TruffleOptions.AOT) {
            return RopeOperations.create(rope.getBytes(), encoding, CR_UNKNOWN);
        } else {
            final Rope newRope = ropeNode.getWithEncodingNode().executeWithEncoding(rope, encoding, CR_UNKNOWN);

            if (newRope == rope) {
                return rope;
            }

            return newRope;
        }
    }

    public Rope makeShared(Rope rope, int sharedStart, int sharedLength) {
        if (TruffleOptions.AOT) {
            return RopeOperations.create(Arrays.copyOfRange(rope.getBytes(), sharedStart, sharedStart + sharedLength), rope.getEncoding(), rope.getCodeRange());
        } else {
            final Rope newRope = ropeNode.getMakeSubstringNode().executeMake(rope, sharedStart, sharedLength);

            if (newRope == rope) {
                return rope;
            }

            return newRope;
        }
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

    private final RopeNode ropeNode = TruffleOptions.AOT ? null : new RopeNode();

}
