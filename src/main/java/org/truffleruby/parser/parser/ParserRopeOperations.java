/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser.parser;

import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives;

public class ParserRopeOperations {

    public Rope withEncoding(Rope rope, Encoding encoding) {
        return ropeNode.getWithEncodingNode().executeWithEncoding(rope, encoding);
    }

    public Rope makeShared(Rope rope, int sharedStart, int sharedLength) {
        return ropeNode.getSubstringNode().executeSubstring(rope, sharedStart, sharedLength);
    }

    private static class RopeNode extends RubyBaseNode {

        @Child RopeNodes.SubstringNode substringNode;
        @Child RopeNodes.WithEncodingNode withEncodingNode;

        public RopeNodes.SubstringNode getSubstringNode() {
            if (substringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                substringNode = insert(RopeNodes.SubstringNode.create());
            }

            return substringNode;
        }

        public RopeNodes.WithEncodingNode getWithEncodingNode() {
            if (withEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                withEncodingNode = insert(RopeNodes.WithEncodingNode.create());
            }

            return withEncodingNode;
        }

    }

    private final RopeNode ropeNode = new RopeNode();

}
