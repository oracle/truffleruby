/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser.ast;

import java.util.Collections;
import java.util.List;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.visitor.NodeVisitor;

public class TruffleFragmentParseNode extends ParseNode {

    private final RubyNode fragment;

    public TruffleFragmentParseNode(SourceIndexLength position, RubyNode fragment) {
        super(position);
        this.fragment = fragment;
    }

    public RubyNode getFragment() {
        return fragment;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitTruffleFragmentNode(this);
    }

    @Override
    public List<ParseNode> childNodes() {
        return Collections.emptyList();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.JAVASCRIPT;
    }
}
