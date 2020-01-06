/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.SymbolNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ArgumentDescriptorUtils;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FindAndReadDeclarationVariableNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.TranslatorEnvironment;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Proc", isClass = true)
public abstract class ProcNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @CoreMethod(names = "new", constructor = true, needsBlock = true, rest = true)
    public abstract static class ProcNewNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode initializeNode;
        @Child private AllocateObjectNode allocateObjectNode;

        public abstract DynamicObject executeProcNew(
                VirtualFrame frame,
                DynamicObject procClass,
                Object[] args,
                Object block);

        @Specialization
        protected DynamicObject proc(VirtualFrame frame, DynamicObject procClass, Object[] args, NotProvided block,
                @Cached("create(nil())") FindAndReadDeclarationVariableNode readNode,
                @Cached ReadCallerFrameNode readCaller) {
            final MaterializedFrame parentFrame = readCaller.execute(frame);

            DynamicObject parentBlock = (DynamicObject) readNode
                    .execute(parentFrame, TranslatorEnvironment.METHOD_BLOCK_NAME);

            if (parentBlock == nil()) {
                parentBlock = tryParentBlockForCExts();
            }

            if (parentBlock == nil()) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorProcWithoutBlock(this));
            }

            return executeProcNew(frame, procClass, args, parentBlock);
        }

        @TruffleBoundary
        protected static void debug(String str) {
            System.err.println(str);
        }

        @TruffleBoundary
        protected DynamicObject tryParentBlockForCExts() {
            /*
             * TODO CS 11-Mar-17 to pass the remaining cext proc specs we need to determine here if Proc.new has been
             * called from a cext from rb_funcall, and then reach down the stack to the Ruby method that originally
             * went into C and get the block from there.
             */

            return nil();
        }

        @Specialization(guards = { "procClass == getProcClass()", "block.getShape() == getProcShape()" })
        protected DynamicObject procNormalOptimized(DynamicObject procClass, Object[] args, DynamicObject block) {
            return block;
        }

        protected DynamicObject getProcClass() {
            return coreLibrary().procClass;
        }

        protected Shape getProcShape() {
            return coreLibrary().procFactory.getShape();
        }

        @Specialization(guards = "procClass == metaClass(block)")
        protected DynamicObject procNormal(DynamicObject procClass, Object[] args, DynamicObject block) {
            return block;
        }

        @Specialization(guards = "procClass != metaClass(block)")
        protected DynamicObject procSpecial(VirtualFrame frame, DynamicObject procClass, Object[] args,
                DynamicObject block) {
            // Instantiate a new instance of procClass as classes do not correspond

            final DynamicObject proc = getAllocateObjectNode().allocate(procClass, Layouts.PROC.build(
                    Layouts.PROC.getType(block),
                    Layouts.PROC.getSharedMethodInfo(block),
                    Layouts.PROC.getCallTargetForType(block),
                    Layouts.PROC.getCallTargetForLambdas(block),
                    Layouts.PROC.getDeclarationFrame(block),
                    Layouts.PROC.getMethod(block),
                    Layouts.PROC.getBlock(block),
                    Layouts.PROC.getFrameOnStackMarker(block),
                    Layouts.PROC.getDeclarationContext(block)));

            getInitializeNode().callWithBlock(proc, "initialize", block, args);

            return proc;
        }

        protected DynamicObject metaClass(DynamicObject object) {
            return Layouts.BASIC_OBJECT.getMetaClass(object);
        }

        private AllocateObjectNode getAllocateObjectNode() {
            if (allocateObjectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateObjectNode = insert(AllocateObjectNode.create());
            }

            return allocateObjectNode;
        }

        private CallDispatchHeadNode getInitializeNode() {
            if (initializeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                initializeNode = insert(CallDispatchHeadNode.createPrivate());
            }

            return initializeNode;
        }

    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode;

        @Specialization
        protected DynamicObject dup(DynamicObject proc) {
            final DynamicObject copy = getAllocateObjectNode()
                    .allocate(Layouts.BASIC_OBJECT.getLogicalClass(proc), Layouts.PROC.build(
                            Layouts.PROC.getType(proc),
                            Layouts.PROC.getSharedMethodInfo(proc),
                            Layouts.PROC.getCallTargetForType(proc),
                            Layouts.PROC.getCallTargetForLambdas(proc),
                            Layouts.PROC.getDeclarationFrame(proc),
                            Layouts.PROC.getMethod(proc),
                            Layouts.PROC.getBlock(proc),
                            Layouts.PROC.getFrameOnStackMarker(proc),
                            Layouts.PROC.getDeclarationContext(proc)));

            return copy;
        }

        private AllocateObjectNode getAllocateObjectNode() {
            if (allocateObjectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateObjectNode = insert(AllocateObjectNode.create());
            }

            return allocateObjectNode;
        }

    }

    @CoreMethod(names = "arity")
    public abstract static class ArityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int arity(DynamicObject proc) {
            return Layouts.PROC.getSharedMethodInfo(proc).getArity().getArityNumber();
        }

    }

    @CoreMethod(names = "binding")
    public abstract static class BindingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject binding(DynamicObject proc) {
            final MaterializedFrame frame = Layouts.PROC.getDeclarationFrame(proc);
            final SourceSection sourceSection = Layouts.PROC.getSharedMethodInfo(proc).getSourceSection();
            return BindingNodes.createBinding(getContext(), frame, sourceSection);
        }
    }

    @CoreMethod(names = { "call", "[]", "yield" }, rest = true, needsBlock = true)
    public abstract static class CallNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        @Specialization
        protected Object call(DynamicObject proc, Object[] args, NotProvided block) {
            return callBlockNode.executeCallBlock(
                    Layouts.PROC.getDeclarationContext(proc),
                    proc,
                    ProcOperations.getSelf(proc),
                    null,
                    args);
        }

        @Specialization
        protected Object call(DynamicObject proc, Object[] args, DynamicObject block) {
            return callBlockNode.executeCallBlock(
                    Layouts.PROC.getDeclarationContext(proc),
                    proc,
                    ProcOperations.getSelf(proc),
                    block,
                    args);
        }

    }

    @CoreMethod(names = "lambda?")
    public abstract static class LambdaNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean lambda(DynamicObject proc) {
            return Layouts.PROC.getType(proc) == ProcType.LAMBDA;
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject parameters(DynamicObject proc) {
            final ArgumentDescriptor[] argsDesc = Layouts.PROC.getSharedMethodInfo(proc).getArgumentDescriptors();
            final boolean isLambda = Layouts.PROC.getType(proc) == ProcType.LAMBDA;
            return ArgumentDescriptorUtils.argumentDescriptorsToParameters(getContext(), argsDesc, isLambda);
        }

    }

    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object sourceLocation(DynamicObject proc) {
            SourceSection sourceSection = Layouts.PROC.getSharedMethodInfo(proc).getSourceSection();

            if (!sourceSection.isAvailable() ||
                    sourceSection.getSource().getName().endsWith("/lib/truffle/truffle/cext.rb")) {
                return nil();
            } else {
                final DynamicObject file = makeStringNode.executeMake(
                        getContext().getPath(sourceSection.getSource()),
                        UTF8Encoding.INSTANCE,
                        CodeRange.CR_UNKNOWN);

                final Object[] objects = new Object[]{ file, sourceSection.getStartLine() };
                return createArray(objects, objects.length);
            }
        }

    }

    @Primitive(name = "proc_symbol_to_proc_symbol")
    public abstract static class ProcSymbolToProcSymbolNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected DynamicObject symbolToProcSymbol(DynamicObject proc) {
            if (Layouts.PROC.getSharedMethodInfo(proc).getArity() == SymbolNodes.ToProcNode.ARITY) {
                return getSymbol(Layouts.PROC.getSharedMethodInfo(proc).getName());
            } else {
                return nil();
            }
        }

    }

    @Primitive(name = "single_block_arg")
    public static abstract class SingleBlockArgNode extends PrimitiveNode {

        @Specialization
        protected Object singleBlockArg(
                VirtualFrame frame,
                @Cached("createBinaryProfile()") ConditionProfile emptyArgsProfile,
                @Cached("createBinaryProfile()") ConditionProfile singleArgProfile) {

            /*
             * In Rubinius, this method inspects the values yielded to the block, regardless of whether the block
             * captures the values, and returns the first value in the list of values yielded to the block.
             *
             * NB: In our case the arguments have already been destructured by the time this node is encountered.
             * Thus, we don't need to do the destructuring work that Rubinius would do and in the case that we receive
             * multiple arguments we need to reverse the destructuring by collecting the values into an array.
             */
            int userArgumentCount = RubyArguments.getArgumentsCount(frame);

            if (emptyArgsProfile.profile(userArgumentCount == 0)) {
                return nil();
            } else {
                if (singleArgProfile.profile(userArgumentCount == 1)) {
                    return RubyArguments.getArgument(frame, 0);
                } else {
                    Object[] extractedArguments = RubyArguments.getArguments(frame);
                    return createArray(extractedArguments, userArgumentCount);
                }
            }
        }
    }


}
