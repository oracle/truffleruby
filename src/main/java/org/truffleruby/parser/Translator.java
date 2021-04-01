/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.parser.ast.NilImplicitParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.visitor.AbstractNodeVisitor;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public abstract class Translator extends AbstractNodeVisitor<RubyNode> {

    protected final Source source;
    protected final ParserContext parserContext;
    protected final Node currentNode;
    protected final RubyLanguage language;

    public Translator(RubyLanguage language, Source source, ParserContext parserContext, Node currentNode) {
        this.language = language;
        this.source = source;
        this.parserContext = parserContext;
        this.currentNode = currentNode;
    }

    public static RubyNode sequence(SourceIndexLength sourceSection, List<RubyNode> sequence) {
        final List<RubyNode> flattened = flatten(sequence, true);

        if (flattened.isEmpty()) {
            final RubyNode literal = new NilLiteralNode(true);
            literal.unsafeSetSourceSection(sourceSection);
            return literal;
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            final RubyNode[] flatSequence = flattened.toArray(RubyNode.EMPTY_ARRAY);

            final SourceIndexLength enclosingSourceSection = enclosing(sourceSection, flatSequence);
            return withSourceSection(enclosingSourceSection, new SequenceNode(flatSequence));
        }
    }

    public static SourceIndexLength enclosing(SourceIndexLength base, RubyNode... sequence) {
        if (base == null) {
            return base;
        }

        int start = base.getCharIndex();
        int end = base.getCharEnd();

        for (RubyNode node : sequence) {
            final SourceIndexLength sourceSection = node.getSourceIndexLength();

            if (sourceSection != null) {
                start = Integer.min(start, sourceSection.getCharIndex());
                end = Integer.max(end, sourceSection.getCharEnd());
            }
        }

        return new SourceIndexLength(start, end - start);
    }

    private static List<RubyNode> flatten(List<RubyNode> sequence, boolean allowTrailingNil) {
        return flattenFromN(sequence, allowTrailingNil, 0);
    }

    private static List<RubyNode> flattenFromN(List<RubyNode> sequence, boolean allowTrailingNil, int n) {
        final List<RubyNode> flattened = new ArrayList<>();

        for (; n < sequence.size(); n++) {
            final boolean lastNode = n == sequence.size() - 1;
            final RubyNode node = sequence.get(n);

            if (node instanceof NilLiteralNode && ((NilLiteralNode) node).isImplicit()) {
                if (allowTrailingNil && lastNode) {
                    flattened.add(node);
                }
            } else if (node instanceof SequenceNode) {
                flattened.addAll(flatten(Arrays.asList(((SequenceNode) node).getSequence()), lastNode));
            } else if (node.canSubsumeFollowing() && !lastNode) {
                List<RubyNode> rest = flattenFromN(sequence, allowTrailingNil, n + 1);
                flattened.add(node.subsumeFollowing(new SequenceNode(rest.toArray(RubyNode.EMPTY_ARRAY))));
                return flattened;
            } else {
                flattened.add(node);
            }
        }

        return flattened;
    }

    protected RubyNode nilNode(SourceIndexLength sourceSection) {
        final RubyNode literal = new NilLiteralNode(false);
        literal.unsafeSetSourceSection(sourceSection);
        return literal;
    }

    protected RubyNode translateNodeOrNil(SourceIndexLength sourceSection, ParseNode node) {
        final RubyNode rubyNode;
        if (node == null || node instanceof NilImplicitParseNode) {
            rubyNode = nilNode(sourceSection);
        } else {
            rubyNode = node.accept(this);
        }
        return rubyNode;
    }

    public static SourceSection translateSourceSection(Source source, SourceIndexLength sourceSection) {
        if (sourceSection == null) {
            return null;
        } else {
            return sourceSection.toSourceSection(source);
        }
    }

    public static RubyNode loadSelf(RubyLanguage language, TranslatorEnvironment environment) {
        final FrameSlot slot = environment.getFrameDescriptor().findOrAddFrameSlot(SelfNode.SELF_IDENTIFIER);
        return new WriteLocalVariableNode(slot, profileArgument(language, new ReadSelfNode()));
    }

    public static RubyNode profileArgument(RubyLanguage language, RubyNode argumentNode) {
        if (language.options.PROFILE_ARGUMENTS) {
            return ProfileArgumentNodeGen.create(argumentNode);
        } else {
            return argumentNode;
        }
    }

    public static <T extends RubyNode> T withSourceSection(SourceIndexLength sourceSection, T node) {
        if (sourceSection != null) {
            node.unsafeSetSourceSection(sourceSection);
        }
        return node;
    }

}
