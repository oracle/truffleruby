/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArraySliceNodeGen;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyCheckArityRootNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.ReadKeywordDescriptorNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;

import java.util.Arrays;
import java.util.Map;

@NodeChild("descriptor")
public abstract class ReadArgumentsNode extends RubyContextSourceNode {

    public static final Object MISSING = new Object();
    private static final WriteFrameSlotNode DUMMY = WriteFrameSlotNodeGen.create(null);

    private final Map<String, WriteFrameSlotNode> expected;
    private final boolean needsExpandedHash;
    private final Arity staticArity;
    private final boolean acceptsKeywords;

    // This isn't compilation constant
    private boolean ruby2Keywords = false;

    @Child private WriteLocalVariableNode writeSelfNode;
    @Children private final RubyNode[] runBlockKWArgs;
    @Children private final RubyNode[] requiredPositionalArgs;
    @Child private RubyNode keyRestArg;
    @Child private RubyNode restArg;
    @Child private RubyNode optionalArg;
    @Children private final RubyNode[] localAssignmentReadNode;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private CheckKeywordArgumentsNode checkKeywordArgumentsNode;
    @Child private HashStoreLibrary hashes = HashStoreLibrary.createDispatched();
    @Child private ExpandKeywordArgumentsNode expandKeywordArgumentsNode = ExpandKeywordArgumentsNode.create();
    @Children private CheckKeywordArgumentNode[] checkKeywordArgumentNodes;
    @Child private ReadPostArgumentNode readPostArgument;
    @Children private WriteFrameSlotNode[] writePreArgumentNode;
    @Children private NewReadPreArgumentNode[] readPreArgumentNodes;
    @Children private RubyNode[] preOptNodes;
    @Children private WriteFrameSlotNode[] writeLocalAssignmentNodes;
    @Child private DispatchNode readKeywordArgsHashNode;

    private final BranchProfile arityExceptionProfile = BranchProfile.create();

    @CompilerDirectives.CompilationFinal public static RubySymbol EMPTY_ARGUMENTS_HASH;

    @CompilerDirectives.CompilationFinal(dimensions = 1) private final FrameSlot[] keywordParameterFrameSlots;

    public static ReadArgumentsNode create(
            WriteLocalVariableNode writeSelfNode,
            RubyNode[] runBlockKWArgs,
            RubyNode[] requiredPositionalArgs,
            RubyNode keyRestArg,
            RubyNode restArg,
            ReadPostArgumentNode readPostArgument,
            RubyNode optionalArg,
            Map<String, WriteFrameSlotNode> expected,
            boolean needsExpandedHash,
            Arity staticArity,
            FrameSlot[] keywordParameterFrameSlots,
            CheckKeywordArgumentNode[] checkKeywordArgumentNodes,
            RubyNode[] localAssignmentReadNode,
            WriteFrameSlotNode[] writePreArgumentNode,
            NewReadPreArgumentNode[] readPreArgumentNodes,
            WriteFrameSlotNode[] writeLocalAssignmentNodes,
            RubyNode[] preOptNodes) {

        return ReadArgumentsNodeGen.create(
                writeSelfNode,
                runBlockKWArgs,
                requiredPositionalArgs,
                keyRestArg,
                restArg,
                readPostArgument,
                optionalArg,
                expected,
                needsExpandedHash,
                staticArity,
                keywordParameterFrameSlots,
                checkKeywordArgumentNodes,
                localAssignmentReadNode,
                writePreArgumentNode,
                readPreArgumentNodes,
                writeLocalAssignmentNodes,
                preOptNodes,
                new ReadKeywordDescriptorNode());
    }

    protected ReadArgumentsNode(
            WriteLocalVariableNode writeSelfNode,
            RubyNode[] runBlockKWArgs,
            RubyNode[] requiredPositionalArgs,
            RubyNode keyRestArg,
            RubyNode restArg,
            ReadPostArgumentNode readPostArgument,
            RubyNode optionalArg,
            Map<String, WriteFrameSlotNode> expected,
            boolean needsExpandedHash,
            Arity staticArity,
            FrameSlot[] keywordParameterFrameSlots,
            CheckKeywordArgumentNode[] checkKeywordArgumentNodes,
            RubyNode[] localAssignmentReadNode,
            WriteFrameSlotNode[] writePreArgumentNode,
            NewReadPreArgumentNode[] readPreArgumentNodes,
            WriteFrameSlotNode[] writeLocalAssignmentNodes,
            RubyNode[] preOptNodes) {
        this.writeSelfNode = writeSelfNode;
        this.runBlockKWArgs = runBlockKWArgs;
        this.requiredPositionalArgs = requiredPositionalArgs;
        this.keyRestArg = keyRestArg;
        this.restArg = restArg;
        this.readPostArgument = readPostArgument;
        this.optionalArg = optionalArg;
        this.expected = expected;
        this.needsExpandedHash = needsExpandedHash;
        this.staticArity = staticArity;
        this.acceptsKeywords = staticArity.acceptsKeywords();
        this.keywordParameterFrameSlots = keywordParameterFrameSlots;
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(staticArity.getRequired());
        this.checkKeywordArgumentNodes = checkKeywordArgumentNodes;
        this.localAssignmentReadNode = localAssignmentReadNode;
        this.writePreArgumentNode = writePreArgumentNode;
        this.readPreArgumentNodes = readPreArgumentNodes;
        this.writeLocalAssignmentNodes = writeLocalAssignmentNodes;
        this.preOptNodes = preOptNodes;

        if (EMPTY_ARGUMENTS_HASH == null) {
            EMPTY_ARGUMENTS_HASH = RubyLanguage.getCurrentLanguage().symbolTable.getSymbol("empty arguments hash");
        }
    }

    public abstract Object execute(VirtualFrame frame, KeywordDescriptor descriptor);

    @Specialization
    protected Object empty(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        clearFrameSlots(frame);
        checkArity(frame, EmptyKeywordDescriptor.EMPTY);
        unloadSelf(frame);
        unloadBlockKWArgs(frame);
        unloadRequiredPositionals(frame, EmptyKeywordDescriptor.EMPTY);
        unloadLocalAssignmentReadNode(frame, EmptyKeywordDescriptor.EMPTY);
        unloadRestArgs(frame, EmptyKeywordDescriptor.EMPTY);
        unloadPostArgument(frame, EmptyKeywordDescriptor.EMPTY);
        unloadOptionalArgs(frame);
        unloadKeyRestArgs(frame, EmptyKeywordDescriptor.EMPTY);
        unloadDefaultArgs(frame, EmptyKeywordDescriptor.EMPTY);
        return null;
    }

    @ExplodeLoop
    @Specialization(guards = "descriptor == cachedDescriptor", limit = "4")
    protected Object cached(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor,
            @Cached("descriptor") NonEmptyKeywordDescriptor cachedDescriptor,
            @Cached(value = "getSlots(cachedDescriptor)") WriteFrameSlotNode[] descriptorSlots) {
        clearFrameSlots(frame); // TODO only those not in the descriptor
        expandKeywordHash(frame, cachedDescriptor);
        checkArity(frame, cachedDescriptor);
        unloadSelf(frame);
        unloadBlockKWArgs(frame);
        unloadRequiredPositionals(frame, cachedDescriptor);
        unloadLocalAssignmentReadNode(frame, cachedDescriptor);
        unloadRestArgs(frame, cachedDescriptor);
        unloadPostArgument(frame, cachedDescriptor);
        unloadOptionalArgs(frame);
        unloadKeyRestArgs(frame, cachedDescriptor);

        // For each keyword in the descriptor, store its value in a local, as long as it's expected.

        for (int n = 0; n < cachedDescriptor.getLength(); n++) {
            final WriteFrameSlotNode frameSlot = descriptorSlots[n];
            if (frameSlot != DUMMY) {
                final Object lastArgument = RubyArguments.getKeywordArgumentsValue(frame, n, cachedDescriptor);

                if (needsExpandedHash && lastArgument instanceof RubyHash) {
                    final RubyHash hash = (RubyHash) lastArgument;
                    final RubySymbol symbol = getSymbol(descriptor.getKeyword(n));

                    final Object value = readKeywordArg(hash, symbol);

                    frameSlot.executeWrite(frame, value);
                } else {
                    frameSlot.executeWrite(frame, lastArgument);
                }
            } else {
                if (!staticArity.hasKeywordsRest() && staticArity.hasKeywords()) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions()
                                    .argumentErrorUnknownKeyword(getSymbol(cachedDescriptor.getKeyword(n)), this));
                }
            }
        }

        unloadDefaultArgs(frame, cachedDescriptor);

        return null;
    }

    @Specialization(replaces = "cached")
    protected Object uncached(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        clearFrameSlots(frame);
        expandKeywordHash(frame, descriptor);
        checkArity(frame, descriptor);
        unloadSelf(frame);
        unloadBlockKWArgs(frame);
        unloadRequiredPositionals(frame, descriptor);
        unloadLocalAssignmentReadNode(frame, descriptor);
        unloadRestArgs(frame, descriptor);
        unloadPostArgument(frame, descriptor);
        unloadOptionalArgs(frame);
        unloadKeyRestArgs(frame, descriptor);

        // For each keyword in the descriptor, store its value in a local, as long as it's expected.

        for (int n = 0; n < descriptor.getLength(); n++) {
            final String keyword = descriptor.getKeyword(n);
            final WriteFrameSlotNode frameSlot = expected(keyword);

            if (frameSlot != null) {
                frameSlot.executeWrite(frame, RubyArguments.getKeywordArgumentsValue(frame, n, descriptor));
            } else {
                if (!staticArity.hasKeywordsRest() && staticArity.hasKeywords()) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentErrorUnknownKeyword(getSymbol(keyword), this));
                }
            }
        }

        unloadDefaultArgs(frame, descriptor);

        return null;
    }

    private void checkArity(VirtualFrame frame, KeywordDescriptor descriptor) {
        final RootNode rootNode = getRootNode();

        if (!(rootNode instanceof RubyCheckArityRootNode)) {
            return;
        }

        final Arity dynamicArity = ((RubyCheckArityRootNode) rootNode).arityForCheck;

        final int argumentsCount = RubyArguments.getArgumentsCount(frame, descriptor);
        int given = argumentsCount;

        if (dynamicArity.acceptsKeywords()) {
            if (descriptor instanceof NonEmptyKeywordDescriptor) {
                given -= 1;

                if (!dynamicArity.basicCheck(given)) {
                    arityExceptionProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError(given, dynamicArity.getRequired(), this));
                }

                if (((NonEmptyKeywordDescriptor) descriptor).isAlsoSplat()) {
                    final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame, descriptor);

                    if (!dynamicArity.hasKeywordsRest() && keywordArguments != null) {
                        if (checkKeywordArgumentsNode == null) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            checkKeywordArgumentsNode = insert(
                                    new CheckKeywordArgumentsNode(getLanguage(), dynamicArity));
                        }

                        hashes.eachEntry(
                                keywordArguments.store,
                                keywordArguments,
                                checkKeywordArgumentsNode,
                                argumentsCount);
                    }
                }
            } else {
                if (!dynamicArity.basicCheck(given)) {
                    arityExceptionProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError(given, dynamicArity.getRequired(), this));
                }

                final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame, descriptor);

                if (!dynamicArity.hasKeywordsRest() && keywordArguments != null) {
                    if (checkKeywordArgumentsNode == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        checkKeywordArgumentsNode = insert(new CheckKeywordArgumentsNode(getLanguage(), dynamicArity));
                    }

                    hashes.eachEntry(
                            keywordArguments.store,
                            keywordArguments,
                            checkKeywordArgumentsNode,
                            argumentsCount);
                }
            }
        } else {
            if (!dynamicArity.check(given)) {
                checkArityError(dynamicArity, given, this);
            }
        }
    }

    public static void checkArityError(Arity arity, int given, Node currentNode) {
        final RubyContext context = RubyContext.get(currentNode);
        if (arity.hasRest()) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentErrorPlus(given, arity.getRequired(), currentNode));
        } else if (arity.getOptional() > 0) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(
                            given,
                            arity.getRequired(),
                            arity.getOptional(),
                            currentNode));
        } else {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(given, arity.getRequired(), currentNode));
        }
    }

    private final BranchProfile expandProfile = BranchProfile.create();

    private void expandKeywordHash(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        if (needsExpandedHash) {
            if (!RubyGuards.isRubyHash(RubyArguments.getLastArgument(frame))) {
                expandProfile.enter();

                final RubyHash expandedHash = expandKeywordArgumentsNode.execute(
                        Arrays.copyOf(frame.getArguments(), frame.getArguments().length), descriptor,
                        RubyArguments.RUNTIME_ARGUMENT_COUNT);

                /* This field isn't compilation constant, but we're already behind a PE constant, a branch profile, and
                 * on a code path that does something relatively expensive. Could have an assumption on it in the
                 * future. */
                if (ruby2Keywords) {
                    expandedHash.ruby2_keywords = true;
                }

                RubyArguments.setArgument(
                        frame,
                        descriptor.getHashIndex(),
                        expandedHash);
            }
        }
    }

    private Object readKeywordArg(RubyHash hash, RubySymbol key) {
        if (readKeywordArgsHashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readKeywordArgsHashNode = insert(DispatchNode.create());
        }

        return readKeywordArgsHashNode.call(hash, "[]", key);
    }

    @ExplodeLoop
    private void clearFrameSlots(VirtualFrame frame) {
        for (FrameSlot frameSlot : keywordParameterFrameSlots) {
            frame.setObject(frameSlot, ReadArgumentsNode.MISSING);
        }
    }

    private void unloadSelf(VirtualFrame frame) {
        if (writeSelfNode != null) {
            writeSelfNode.execute(frame);
        }
    }

    @ExplodeLoop
    private void unloadBlockKWArgs(VirtualFrame frame) {
        if (runBlockKWArgs != null) {
            for (int i = 0; i < runBlockKWArgs.length; i++) {
                runBlockKWArgs[i].execute(frame);
            }
        }
    }

    @ExplodeLoop
    private void unloadRequiredPositionals(VirtualFrame frame, KeywordDescriptor descriptor) {
        int readPreArgumentCount = 0;
        for (int i = 0; i < requiredPositionalArgs.length; i++) {
            if (requiredPositionalArgs[i] instanceof SequenceNode) {
                requiredPositionalArgs[i].execute(frame);
            } else if (i < readPreArgumentNodes.length && i < writePreArgumentNode.length &&
                    readPreArgumentNodes[i] != null) {
                // TODO: Profile the ReadPreArgumentNode
                Object value = (readPreArgumentNodes[readPreArgumentCount]).execute(frame, descriptor, acceptsKeywords);
                writePreArgumentNode[readPreArgumentCount].executeWrite(frame, value);
                readPreArgumentCount++;
            } else {
                requiredPositionalArgs[i].execute(frame);
            }
        }
    }

    @ExplodeLoop
    private void unloadLocalAssignmentReadNode(VirtualFrame frame, KeywordDescriptor descriptor) {
        if (preOptNodes != null) {
            for (int i = 0; i < preOptNodes.length; i++) {
                if (preOptNodes[i] instanceof ReadOptionalArgumentNode) {
                    for (int c = 0; c < localAssignmentReadNode.length; c++) {
                        Object value = ((ReadOptionalArgumentNode) localAssignmentReadNode[c])
                                .execute(frame, descriptor);
                        writeLocalAssignmentNodes[c].executeWrite(frame, value);
                    }
                } else {
                    preOptNodes[i].execute(frame);
                }
            }

        }
    }

    private void unloadRestArgs(VirtualFrame frame, KeywordDescriptor descriptor) {
        if (restArg instanceof WriteLocalVariableNode) {
            assert ((WriteLocalVariableNode) restArg).valueNode instanceof ReadRestArgumentNode ||
                    ((WriteLocalVariableNode) restArg).valueNode instanceof ArraySliceNodeGen : ((WriteLocalVariableNode) restArg).valueNode
                            .getClass().getName();
            final Object hashOrArray;
            if (((WriteLocalVariableNode) restArg).valueNode instanceof ReadRestArgumentNode) {
                hashOrArray = ((ReadRestArgumentNode) ((WriteLocalVariableNode) restArg).valueNode).execute(frame,
                        descriptor);
            } else {
                hashOrArray = (((WriteLocalVariableNode) restArg).valueNode).execute(frame);
            }

            ((WriteLocalVariableNode) restArg).bypass(frame, hashOrArray);
        } else if (restArg instanceof RubyNode) {
            throw new UnsupportedOperationException(restArg.getClass().getName());
        }
    }

    private void unloadPostArgument(VirtualFrame frame, KeywordDescriptor descriptor) {
        if (readPostArgument != null) {
            readPostArgument.execute(frame, descriptor, acceptsKeywords);
        }
    }

    private void unloadOptionalArgs(VirtualFrame frame) {
        if (optionalArg instanceof RubyNode) {
            optionalArg.execute(frame);
        }
    }

    private void unloadKeyRestArgs(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        if (keyRestArg instanceof WriteLocalVariableNode) {
            ((WriteLocalVariableNode) keyRestArg).bypass(frame,
                    HashOperations.newEmptyHash(getContext(), getLanguage()));
        }
    }

    private void unloadKeyRestArgs(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        if (keyRestArg instanceof WriteLocalVariableNode) {
            assert needsExpandedHash;

            RubyHash hash = (RubyHash) RubyArguments.getArgument(frame, descriptor.getHashIndex());

            if (staticArity.hasKeywords()) {
                // TODO - we're expanding the hash, just to copy it and remove things from it
                hash = copyWithoutFormalKeywordArguments(hash);
            }

            ((WriteLocalVariableNode) keyRestArg).bypass(frame, hash);
        }
    }

    @TruffleBoundary
    private RubyHash copyWithoutFormalKeywordArguments(RubyHash hash) {
        hash = (RubyHash) RubyContext.send(hash, "dup");

        for (String keyword : staticArity.getKeywordArguments()) {
            RubyContext.send(hash, "delete", getSymbol(keyword));
        }

        return hash;
    }

    @ExplodeLoop
    private void unloadDefaultArgs(VirtualFrame frame, KeywordDescriptor descriptor) {
        for (int i = 0; i < checkKeywordArgumentNodes.length; i++) {
            checkKeywordArgumentNodes[i].execute(frame, descriptor);
        }
    }

    protected WriteFrameSlotNode[] getSlots(NonEmptyKeywordDescriptor descriptor) {
        WriteFrameSlotNode[] slots = new WriteFrameSlotNode[descriptor.getLength()];
        for (int n = 0; n < descriptor.getLength(); n++) {
            slots[n] = expected(descriptor.getKeyword(n));
            if (slots[n] == null) {
                slots[n] = DUMMY;
            }
        }

        return slots;
    }

    @TruffleBoundary
    private WriteFrameSlotNode expected(String keyword) {
        return expected.get(keyword);
    }

    public boolean needsExpandedHash() {
        return needsExpandedHash;
    }

    public void setRuby2Keywords() {
        assert needsExpandedHash; // We don't check ruby2Keywords unless needsExpandedHash is set
        ruby2Keywords = true;
    }

}
