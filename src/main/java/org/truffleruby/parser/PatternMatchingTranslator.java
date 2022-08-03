/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;
import org.truffleruby.parser.ast.ArrayPatternParseNode;
import org.truffleruby.parser.ast.FindPatternParseNode;
import org.truffleruby.parser.ast.HashPatternParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.ParseNode;

public class PatternMatchingTranslator extends Translator {

    public PatternMatchingTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode) {
        super(language, source, parserContext, currentNode);
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        throw new UnsupportedOperationException(node.toString() + " " + node.getPosition());
    }

    public RubyNode visitPatternNode(ParseNode node) {
        if (node instanceof ArrayPatternParseNode) {
            return visitArrayPatternNode((ArrayPatternParseNode) node);
        } else if (node instanceof FindPatternParseNode) {

        } else if (node instanceof HashPatternParseNode) {

        } else {
            throw new UnsupportedOperationException(node.toString() + " " + node.getPosition());
        }
    }

    public RubyNode translateArrayPatternNode(ArrayPatternParseNode node) {
        // For now, we are assuming that only preArgs exist.
        final int size = node.minimumArgsNum();
        ListParseNode pre = node.getPreArgs();
        ParseNode[] ch = pre.children();
        for (int i = 0; i < pre.size(); i++) {
            ch[i];
        }
    }


}
