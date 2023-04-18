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

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.DummyNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.ParseNode;

public abstract class BaseTranslator extends Translator {

    protected final TranslatorEnvironment environment;

    public BaseTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            TranslatorEnvironment environment) {
        super(language, source, parserContext, currentNode);
        this.environment = environment;
    }

    protected RubyNode addNewlineIfNeeded(ParseNode jrubyNode, RubyNode node) {
        if (jrubyNode.isNewline()) {
            TruffleSafepoint.poll(DummyNode.INSTANCE);

            final SourceIndexLength current = node.getEncapsulatingSourceIndexLength();

            if (current == null) {
                return node;
            }

            if (environment.getParseEnvironment().isCoverageEnabled()) {
                node.unsafeSetIsCoverageLine();
                language.coverageManager.setLineHasCode(source, current.toSourceSection(source).getStartLine());
            }
            node.unsafeSetIsNewLine();
        }

        return node;
    }

}
