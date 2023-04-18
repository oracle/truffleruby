/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.IsNilNode;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.core.array.ArraySliceNodeGen;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.ArrayIsAtLeastAsLargeAsNode;
import org.truffleruby.language.arguments.CheckNoKeywordArgumentsNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadKeywordArgumentNode;
import org.truffleruby.language.arguments.ReadKeywordRestArgumentNode;
import org.truffleruby.language.arguments.ReadOptionalArgumentNode;
import org.truffleruby.language.arguments.ReadPostArgumentNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadRestArgumentNode;
import org.truffleruby.language.arguments.SaveMethodBlockNode;
import org.truffleruby.language.control.IfElseNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.AssignableParseNode;
import org.truffleruby.parser.ast.BlockArgParseNode;
import org.truffleruby.parser.ast.DAsgnParseNode;
import org.truffleruby.parser.ast.KeywordArgParseNode;
import org.truffleruby.parser.ast.KeywordRestArgParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.MultipleAsgnParseNode;
import org.truffleruby.parser.ast.NilImplicitParseNode;
import org.truffleruby.parser.ast.NoKeywordsArgParseNode;
import org.truffleruby.parser.ast.OptArgParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.RequiredKeywordArgumentValueParseNode;
import org.truffleruby.parser.ast.RestArgParseNode;
import org.truffleruby.parser.ast.StarParseNode;
import org.truffleruby.parser.ast.VCallParseNode;
import org.truffleruby.parser.ast.types.INameNode;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

public class LoadArgumentsTranslator extends Translator {

    private static class ArraySlot {

        private int arraySlot;
        private int previousIndex;

        public ArraySlot(int arraySlot, int previousIndex) {
            this.arraySlot = arraySlot;
            this.previousIndex = previousIndex;
        }

        public int getArraySlot() {
            return arraySlot;
        }

        public int getPreviousIndex() {
            return previousIndex;
        }
    }

    private final boolean isProc;
    private final boolean isMethod;
    private final BodyTranslator methodBodyTranslator;
    private final Deque<ArraySlot> arraySlotStack = new ArrayDeque<>();

    private enum State {
        PRE,
        OPT,
        POST
    }

    private final ArgsParseNode argsNode;
    private final int required;
    private final boolean hasKeywordArguments;

    private int index;
    private int indexFromEnd = 1;
    private State state;

    public LoadArgumentsTranslator(
            Node currentNode,
            ArgsParseNode argsNode,
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            boolean isProc,
            boolean isMethod,
            BodyTranslator methodBodyTranslator) {
        super(language, source, parserContext, currentNode);
        this.isProc = isProc;
        this.isMethod = isMethod;
        this.methodBodyTranslator = methodBodyTranslator;
        this.argsNode = argsNode;
        this.required = argsNode.getRequiredCount();
        this.hasKeywordArguments = argsNode.hasKwargs();
    }

    public RubyNode translate() {
        final SourceIndexLength sourceSection = argsNode.getPosition();

        final List<RubyNode> sequence = new ArrayList<>();

        //if (!arraySlotStack.isEmpty()) {
        sequence.add(loadSelf(language));
        //}

        final ParseNode[] args = argsNode.getArgs();

        final int preCount = argsNode.getPreCount();

        if (preCount > 0) {
            state = State.PRE;
            index = 0;
            for (int i = 0; i < preCount; i++) {
                sequence.add(args[i].accept(this));
                index++;
            }
        }

        // Do this before handling optional arguments as one might get
        // its default value via a `yield`.
        if (isMethod) {
            sequence.add(saveMethodBlockArg());
        }

        final int optArgCount = argsNode.getOptionalArgsCount();
        if (optArgCount > 0) {
            // (BlockParseNode 0, (OptArgParseNode:a 0, (LocalAsgnParseNode:a 0, (FixnumParseNode 0))), ...)
            state = State.OPT;
            index = argsNode.getPreCount();
            final int optArgIndex = argsNode.getOptArgIndex();
            for (int i = 0; i < optArgCount; i++) {
                sequence.add(args[optArgIndex + i].accept(this));
                ++index;
            }
        }

        if (argsNode.getRestArgNode() != null) {
            sequence.add(argsNode.getRestArgNode().accept(this));
        }

        int postCount = argsNode.getPostCount();

        // The load to use when the array is not nil and the length is smaller than the number of required arguments

        final List<RubyNode> notNilSmallerSequence = new ArrayList<>();

        if (postCount > 0) {
            state = State.POST;
            ParseNode[] children = argsNode.getPost().children();
            index = argsNode.getPreCount();
            for (int i = 0; i < children.length; i++) {
                notNilSmallerSequence.add(children[i].accept(this));
                index++;
            }
        }

        final RubyNode notNilSmaller = sequence(sourceSection, notNilSmallerSequence);

        // The load to use when the there is no rest

        final List<RubyNode> noRestSequence = new ArrayList<>();

        if (postCount > 0) {
            state = State.POST;
            ParseNode[] children = argsNode.getPost().children();
            index = argsNode.getPreCount() + argsNode.getOptionalArgsCount();
            for (int i = 0; i < children.length; i++) {
                noRestSequence.add(children[i].accept(this));
                index++;
            }
        }

        final RubyNode noRest = sequence(sourceSection, noRestSequence);

        // The load to use when the array is not nil and at least as large as the number of required arguments

        final List<RubyNode> notNilAtLeastAsLargeSequence = new ArrayList<>();

        if (postCount > 0) {
            state = State.POST;
            index = -1;

            int postIndex = argsNode.getPostIndex();
            for (int i = postCount - 1; i >= 0; i--) {
                notNilAtLeastAsLargeSequence.add(args[postIndex + i].accept(this));
                index--;
            }
        }

        final RubyNode notNilAtLeastAsLarge = sequence(sourceSection, notNilAtLeastAsLargeSequence);

        if (useArray()) {
            if (argsNode.getPreCount() == 0 || argsNode.hasRestArg()) {
                sequence.add(new IfElseNode(
                        new ArrayIsAtLeastAsLargeAsNode(required, loadArray(sourceSection)),
                        notNilAtLeastAsLarge,
                        notNilSmaller));
            } else {
                sequence.add(noRest);
            }
        } else {
            // TODO CS 10-Jan-16 needn't have created notNilSmaller
            sequence.add(notNilAtLeastAsLarge);
        }

        if (hasKeywordArguments) {
            final int keywordIndex = argsNode.getKeywordsIndex();
            final int keywordCount = argsNode.getKeywordCount();

            for (int i = 0; i < keywordCount; i++) {
                sequence.add(args[keywordIndex + i].accept(this));
            }
        }

        if (argsNode.getKeyRest() != null) {
            sequence.add(argsNode.getKeyRest().accept(this));
        }

        if (argsNode.getBlock() != null) {
            sequence.add(argsNode.getBlock().accept(this));
        }

        return sequence(sourceSection, sequence);
    }

    @Override
    public RubyNode visitKeywordRestArgNode(KeywordRestArgParseNode node) {
        final RubyNode readNode = new ReadKeywordRestArgumentNode(language, argsNode.getArity());
        final int slot = methodBodyTranslator.getEnvironment().declareVar(node.getName());

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitNoKeywordsArgNode(NoKeywordsArgParseNode node) {
        return new CheckNoKeywordArgumentsNode();
    }

    @Override
    public RubyNode visitKeywordArgNode(KeywordArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        final AssignableParseNode asgnNode = node.getAssignable();
        final String name = ((INameNode) asgnNode).getName();
        final int slot = methodBodyTranslator.getEnvironment().declareVar(name);

        final RubyNode defaultValue;
        if (asgnNode.getValueNode() instanceof RequiredKeywordArgumentValueParseNode) {
            defaultValue = null;
        } else {
            defaultValue = translateNodeOrNil(sourceSection, asgnNode.getValueNode());
        }

        final RubyNode readNode = ReadKeywordArgumentNode.create(language.getSymbol(name), defaultValue);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitArgumentNode(ArgumentParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        final RubyNode readNode = readArgument(sourceSection);
        final int slot = methodBodyTranslator.getEnvironment().findFrameSlot(node.getName());
        return new WriteLocalVariableNode(slot, readNode);
    }

    private RubyNode readArgument(SourceIndexLength sourceSection) {
        if (useArray()) {
            return ArrayIndexNodes.ReadConstantIndexNode.create(loadArray(sourceSection), index);
        } else {
            if (state == State.PRE) {
                return profileArgument(
                        language,
                        new ReadPreArgumentNode(
                                index,
                                hasKeywordArguments,
                                isProc ? MissingArgumentBehavior.NIL : MissingArgumentBehavior.RUNTIME_ERROR));
            } else if (state == State.POST) {
                return new ReadPostArgumentNode(-index, hasKeywordArguments, required);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public RubyNode visitRestArgNode(RestArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        final RubyNode readNode;

        if (argsNode == null) {
            throw new IllegalStateException("No arguments node visited");
        }

        int from = argsNode.getPreCount() + argsNode.getOptionalArgsCount();
        int to = -argsNode.getPostCount();
        if (useArray()) {
            readNode = ArraySliceNodeGen.create(from, to, loadArray(sourceSection));
        } else {
            readNode = new ReadRestArgumentNode(from, -to, hasKeywordArguments);
        }

        final int slot = methodBodyTranslator.getEnvironment().findFrameSlot(node.getName());
        return new WriteLocalVariableNode(slot, readNode);
    }

    public RubyNode saveMethodBlockArg() {
        final int slot = methodBodyTranslator.getEnvironment().declareVar(TranslatorEnvironment.METHOD_BLOCK_NAME);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitBlockArgNode(BlockArgParseNode node) {
        final int slot = methodBodyTranslator.getEnvironment().findFrameSlot(node.getName());
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitOptArgNode(OptArgParseNode node) {
        // (OptArgParseNode:a 0, (LocalAsgnParseNode:a 0, (FixnumParseNode 0)))
        return node.getValue().accept(this);
    }

    @Override
    public RubyNode visitLocalAsgnNode(LocalAsgnParseNode node) {
        return translateLocalAssignment(node.getPosition(), node.getName(), node.getValueNode());
    }

    @Override
    public RubyNode visitDAsgnNode(DAsgnParseNode node) {
        return translateLocalAssignment(node.getPosition(), node.getName(), node.getValueNode());
    }

    private RubyNode translateLocalAssignment(SourceIndexLength sourcePosition, String name, ParseNode valueNode) {
        final SourceIndexLength sourceSection = sourcePosition;

        final int slot = methodBodyTranslator.getEnvironment().declareVar(name);

        final RubyNode readNode;

        if (indexFromEnd == 1) {
            if (valueNode instanceof NilImplicitParseNode) {
                // Multiple assignment

                if (useArray()) {
                    readNode = ArrayIndexNodes.ReadConstantIndexNode.create(loadArray(sourceSection), index);
                } else {
                    readNode = readArgument(sourceSection);
                }
            } else {
                // Optional argument
                final RubyNode defaultValue;

                // The JRuby parser gets local variables that shadow methods with vcalls wrong - fix up here

                if (valueNode instanceof VCallParseNode) {
                    final String calledName = ((VCallParseNode) valueNode).getName();

                    // Just consider the circular case for now as that's all that's speced

                    if (calledName.equals(name)) {
                        defaultValue = new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, slot);
                        defaultValue.unsafeSetSourceSection(sourceSection);
                    } else {
                        defaultValue = valueNode.accept(this);
                    }
                } else {
                    defaultValue = valueNode.accept(this);
                }

                if (argsNode == null) {
                    throw new IllegalStateException("No arguments node visited");
                }

                int minimum = index + 1 + argsNode.getPostCount();

                if (useArray()) {
                    // TODO CS 10-Jan-16 we should really hoist this check, or see if Graal does it for us
                    readNode = new IfElseNode(
                            new ArrayIsAtLeastAsLargeAsNode(minimum, loadArray(sourceSection)),
                            ArrayIndexNodes.ReadConstantIndexNode.create(loadArray(sourceSection), index),
                            defaultValue);
                } else {
                    readNode = new ReadOptionalArgumentNode(
                            index,
                            minimum,
                            hasKeywordArguments,
                            defaultValue);
                }
            }
        } else {
            readNode = ArraySliceNodeGen.create(index, indexFromEnd, loadArray(sourceSection));
        }

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitArrayNode(ArrayParseNode node) {
        // (ArrayParseNode 0, (MultipleAsgn19Node 0, (ArrayParseNode 0, (LocalAsgnParseNode:a 0, ), (LocalAsgnParseNode:b 0, )), null, null)))
        if (node.size() == 1 && node.get(0) instanceof MultipleAsgnParseNode) {
            return node.children()[0].accept(this);
        } else {
            return defaultVisit(node);
        }
    }

    @Override
    public RubyNode visitMultipleAsgnNode(MultipleAsgnParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        // (MultipleAsgn19Node 0, (ArrayParseNode 0, (LocalAsgnParseNode:a 0, ), (LocalAsgnParseNode:b 0, )), null, null))

        final int arrayIndex = index;

        final int arraySlot = methodBodyTranslator.getEnvironment().declareLocalTemp("destructure");

        pushArraySlot(arraySlot);

        // The load to use when the array is not nil and the length is smaller than the number of required arguments

        final List<RubyNode> notNilSmallerSequence = new ArrayList<>();

        if (node.getPre() != null) {
            index = 0;
            for (ParseNode child : node.getPre().children()) {
                notNilSmallerSequence.add(child.accept(this));
                index++;
            }
        }

        if (node.getRest() != null) {
            index = node.getPreCount();
            indexFromEnd = -node.getPostCount();
            notNilSmallerSequence.add(node.getRest().accept(this));
            indexFromEnd = 1;
        }

        if (node.getPost() != null) {
            ParseNode[] children = node.getPost().children();
            index = node.getPreCount();
            for (int i = 0; i < children.length; i++) {
                notNilSmallerSequence.add(children[i].accept(this));
                index++;
            }
        }

        final RubyNode notNilSmaller = sequence(sourceSection, notNilSmallerSequence);

        // The load to use when the array is not nil and at least as large as the number of required arguments

        final List<RubyNode> notNilAtLeastAsLargeSequence = new ArrayList<>();

        if (node.getPre() != null) {
            index = 0;
            for (ParseNode child : node.getPre().children()) {
                notNilAtLeastAsLargeSequence.add(child.accept(this));
                index++;
            }
        }

        if (node.getRest() != null) {
            index = node.getPreCount();
            indexFromEnd = -node.getPostCount();
            notNilAtLeastAsLargeSequence.add(node.getRest().accept(this));
            indexFromEnd = 1;
        }

        if (node.getPost() != null) {
            ParseNode[] children = node.getPost().children();
            index = -1;
            for (int i = children.length - 1; i >= 0; i--) {
                notNilAtLeastAsLargeSequence.add(children[i].accept(this));
                index--;
            }
        }

        final RubyNode notNilAtLeastAsLarge = sequence(sourceSection, notNilAtLeastAsLargeSequence);

        popArraySlot(arraySlot);

        final List<RubyNode> nilSequence = new ArrayList<>();

        final ParameterCollector parametersToClearCollector = new ParameterCollector();

        if (node.getPre() != null) {
            for (ParseNode child : node.getPre().children()) {
                child.accept(parametersToClearCollector);
            }
        }

        if (node.getRest() != null) {
            if (node.getRest() instanceof INameNode) {
                final String name = ((INameNode) node.getRest()).getName();

                if (node.getPreCount() == 0 && node.getPostCount() == 0) {
                    nilSequence.add(
                            methodBodyTranslator
                                    .getEnvironment()
                                    .findOrAddLocalVarNodeDangerous(name, sourceSection)
                                    .makeWriteNode(
                                            ArrayLiteralNode
                                                    .create(language, new RubyNode[]{ new NilLiteralNode(true) })));
                } else {
                    nilSequence.add(
                            methodBodyTranslator
                                    .getEnvironment()
                                    .findOrAddLocalVarNodeDangerous(name, sourceSection)
                                    .makeWriteNode(ArrayLiteralNode.create(language, null)));
                }
            } else if (node.getRest() instanceof StarParseNode) {
                // Don't think we need to do anything
            } else {
                throw new UnsupportedOperationException("unsupported rest node " + node.getRest());
            }
        }

        if (node.getPost() != null) {
            for (ParseNode child : node.getPost().children()) {
                child.accept(parametersToClearCollector);
            }
        }

        for (String parameterToClear : parametersToClearCollector.getParameters()) {
            nilSequence.add(
                    methodBodyTranslator
                            .getEnvironment()
                            .findOrAddLocalVarNodeDangerous(parameterToClear, sourceSection)
                            .makeWriteNode(nilNode(sourceSection)));
        }

        if (node.getPre() != null) {
            // We haven't pushed a new array slot, so this will read the value which we couldn't convert to an array into the first destructured argument
            index = arrayIndex;
            nilSequence.add(node.getPre().get(0).accept(this));
        }

        final RubyNode nil = sequence(sourceSection, nilSequence);

        return sequence(sourceSection, Arrays.asList(
                new WriteLocalVariableNode(arraySlot, SplatCastNodeGen.create(
                        language,
                        SplatCastNode.NilBehavior.ARRAY_WITH_NIL,
                        true,
                        readArgument(sourceSection))),
                new IfElseNode(
                        new IsNilNode(new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, arraySlot)),
                        nil,
                        new IfElseNode(
                                new ArrayIsAtLeastAsLargeAsNode(
                                        node.getPreCount() + node.getPostCount(),
                                        new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, arraySlot)),
                                notNilAtLeastAsLarge,
                                notNilSmaller))));
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        // For normal expressions in the default value for optional arguments, use the normal body translator
        return node.accept(methodBodyTranslator);
    }

    public void pushArraySlot(int slot) {
        arraySlotStack.push(new ArraySlot(slot, index));
    }

    public void popArraySlot(int slot) {
        index = arraySlotStack.pop().getPreviousIndex();
    }

    protected boolean useArray() {
        return !arraySlotStack.isEmpty();
    }

    protected RubyNode loadArray(SourceIndexLength sourceSection) {
        final RubyNode node = new ReadLocalVariableNode(
                LocalVariableType.FRAME_LOCAL,
                arraySlotStack.peek().getArraySlot());
        node.unsafeSetSourceSection(sourceSection);
        return node;
    }

}
