/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.parser.ast.LocalVarParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.SelfParseNode;
import org.truffleruby.parser.ast.SplatParseNode;

import java.util.Arrays;

/** There are some patterns during translation where we want to produce a value once and use it many times. We may want
 * to implement this pattern in different ways depending on what produces the value. For example a complex expression we
 * don't control we'd want to translate and store in a local variable, but if it's something simpler like 'self' we can
 * execute it each time instead and skip storing and retrieving from a local variable. It also lets us see what the
 * original node was if it's simple, which we use to look for example if a receiver was originally self, even when we've
 * been desugaring. */
public interface ValueFromNode {

    RubyNode prepareAndThen(SourceIndexLength sourceSection, RubyNode subsequent);

    ParseNode get(SourceIndexLength sourceSection);

    class ValueFromEffectNode implements ValueFromNode {

        private final BodyTranslator translator;
        private final ParseNode node;
        private final String temp;
        private final int slot;

        private boolean sequenced = false;

        public ValueFromEffectNode(BodyTranslator translator, ParseNode node) {
            this.translator = translator;
            this.node = node;
            temp = translator.getEnvironment().allocateLocalTemp("value");
            slot = translator.getEnvironment().declareVar(temp);
        }

        @Override
        public RubyNode prepareAndThen(SourceIndexLength sourceSection, RubyNode subsequent) {
            if (sequenced) {
                throw new UnsupportedOperationException("don't use a value more than once");
            }
            sequenced = true;
            return Translator.sequence(
                    sourceSection,
                    Arrays.asList(
                            new WriteLocalVariableNode(slot, node.accept(translator)),
                            subsequent));
        }

        @Override
        public ParseNode get(SourceIndexLength sourceSection) {
            return new LocalVarParseNode(sourceSection, 0, temp);
        }

    }

    class ValueFromSelfNode implements ValueFromNode {

        @Override
        public RubyNode prepareAndThen(SourceIndexLength sourceSection, RubyNode subsequent) {
            return subsequent;
        }

        @Override
        public ParseNode get(SourceIndexLength sourceSection) {
            return new SelfParseNode(sourceSection);
        }

    }

    class ValueFromSplatNode implements ValueFromNode {

        private final ValueFromNode value;

        public ValueFromSplatNode(BodyTranslator translator, SplatParseNode node) {
            value = valueFromNode(translator, node.getValue());
        }

        @Override
        public RubyNode prepareAndThen(SourceIndexLength sourceSection, RubyNode subsequent) {
            return value.prepareAndThen(sourceSection, subsequent);
        }

        @Override
        public ParseNode get(SourceIndexLength sourceSection) {
            return new SplatParseNode(sourceSection, value.get(sourceSection));
        }
    }

    static ValueFromNode valueFromNode(BodyTranslator translator, ParseNode node) {
        if (node instanceof SelfParseNode) {
            return new ValueFromSelfNode();
        } else if (node instanceof SplatParseNode) {
            return new ValueFromSplatNode(translator, (SplatParseNode) node);
        } else {
            return new ValueFromEffectNode(translator, node);
        }
    }

}
