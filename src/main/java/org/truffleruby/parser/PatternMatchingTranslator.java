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
import org.truffleruby.parser.ast.ParseNode;

public class PatternMatchingTranslator extends Translator {

    public PatternMatchingTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode) {
        super(language, source, parserContext, currentNode);
        //        this.parent = parent;
        //        this.environment = environment;
        //        this.rubyWarnings = rubyWarnings;
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        throw new UnsupportedOperationException(node.toString() + " " + node.getPosition());
    }


}
