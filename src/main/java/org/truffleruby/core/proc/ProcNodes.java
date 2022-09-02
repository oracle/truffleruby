/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.UnboundMethodNodes.MethodRuby2KeywordsNode;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.SymbolNodes;
import org.truffleruby.language.Nil;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.arguments.ArgumentDescriptorUtils;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FindAndReadDeclarationVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.TranslatorEnvironment;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Proc", isClass = true)
public abstract class ProcNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyProc allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }
    }

    @CoreMethod(names = "new", constructor = true, needsBlock = true, rest = true)
    public abstract static class ProcNewNode extends CoreMethodArrayArgumentsNode {
        public static ProcNewNode create() {
            return ProcNodesFactory.ProcNewNodeFactory.create(null);
        }

        public abstract RubyProc executeProcNew(VirtualFrame frame, RubyClass procClass, Object[] args, Object block);

        @Specialization
        protected RubyProc proc(VirtualFrame frame, RubyClass procClass, Object[] args, Nil block,
                @Cached FindAndReadDeclarationVariableNode readNode,
                @Cached ReadCallerFrameNode readCaller,
                @Cached ProcNewNode recurseNode,
                @Cached("new()") WarnNode warnNode) {
            final MaterializedFrame parentFrame = readCaller.execute(frame);

            Object parentBlock = readNode.execute(parentFrame, TranslatorEnvironment.METHOD_BLOCK_NAME, nil);

            if (parentBlock == nil) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorProcWithoutBlock(this));
            } else {
                if (warnNode.shouldWarnForDeprecation()) {
                    warnNode.warningMessage(
                            getContext().getCallStack().getTopMostUserSourceSection(),
                            "Capturing the given block using Kernel#proc is deprecated; use `&block` instead");
                }

                final RubyProc proc = (RubyProc) parentBlock;
                return recurseNode.executeProcNew(frame, procClass, args, proc);
            }
        }

        @Specialization(guards = { "procClass == getProcClass()", "block.getShape() == getProcShape()" })
        protected RubyProc procNormalOptimized(RubyClass procClass, Object[] args, RubyProc block) {
            return block;
        }

        @Specialization(guards = "procClass == metaClass(block)")
        protected RubyProc procNormal(RubyClass procClass, Object[] args, RubyProc block) {
            return block;
        }

        @Specialization(guards = "procClass != metaClass(block)")
        protected RubyProc procSpecial(VirtualFrame frame, RubyClass procClass, Object[] args, RubyProc block,
                @Cached DispatchNode initialize) {
            // Instantiate a new instance of procClass as classes do not correspond

            final RubyProc proc = new RubyProc(
                    procClass,
                    getLanguage().procShape,
                    block.type,
                    block.arity,
                    block.argumentDescriptors,
                    block.callTargets,
                    block.callTarget,
                    block.declarationFrame,
                    block.declarationVariables,
                    block.declaringMethod,
                    block.frameOnStackMarker,
                    block.declarationContext);

            AllocationTracing.trace(proc, this);
            initialize.callWithDescriptor(proc, "initialize", block, RubyArguments.getDescriptor(frame), args);
            return proc;
        }

        protected RubyClass getProcClass() {
            return coreLibrary().procClass;
        }

        protected Shape getProcShape() {
            return getLanguage().procShape;
        }

        protected RubyClass metaClass(RubyProc object) {
            return object.getMetaClass();
        }
    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyProc dup(RubyProc proc) {
            final RubyClass logicalClass = proc.getLogicalClass();
            final RubyProc copy = new RubyProc(
                    logicalClass,
                    getLanguage().procShape,
                    proc.type,
                    proc.arity,
                    proc.argumentDescriptors,
                    proc.callTargets,
                    proc.callTarget,
                    proc.declarationFrame,
                    proc.declarationVariables,
                    proc.declaringMethod,
                    proc.frameOnStackMarker,
                    proc.declarationContext);

            AllocationTracing.trace(copy, this);
            return copy;
        }
    }

    @CoreMethod(names = "arity")
    public abstract static class ArityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int arity(RubyProc proc) {
            return proc.getArityNumber();
        }
    }

    @CoreMethod(names = "binding")
    public abstract static class BindingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyBinding binding(RubyProc proc) {
            final MaterializedFrame frame = proc.declarationFrame;
            final SourceSection sourceSection = proc.getSharedMethodInfo().getSourceSection();
            return BindingNodes.createBinding(getContext(), getLanguage(), frame, sourceSection);
        }
    }

    @GenerateUncached
    @CoreMethod(names = { "call", "[]", "yield" }, rest = true, needsBlock = true, alwaysInlined = true)
    public abstract static class CallNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object call(Frame callerFrame, RubyProc proc, Object[] rubyArgs, RootCallTarget target,
                @Cached CallBlockNode callBlockNode) {
            return callBlockNode.executeCallBlock(
                    proc.declarationContext,
                    proc,
                    ProcOperations.getSelf(proc),
                    RubyArguments.getBlock(rubyArgs),
                    RubyArguments.getDescriptor(rubyArgs),
                    RubyArguments.getRawArguments(rubyArgs),
                    null);
        }
    }

    @CoreMethod(names = { "==", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean equal(RubyProc self, Object otherObj,
                @Cached LogicalClassNode logicalClassNode,
                @Cached ConditionProfile classProfile,
                @Cached ConditionProfile lambdaProfile) {
            if (classProfile.profile(logicalClassNode.execute(self) != logicalClassNode.execute(otherObj))) {
                return false;
            }
            final RubyProc other = (RubyProc) otherObj;

            if (lambdaProfile.profile(self.isLambda() != other.isLambda())) {
                return false;
            }

            return self.callTarget == other.callTarget &&
                    self.declarationFrame == other.declarationFrame;
        }
    }

    @CoreMethod(names = "lambda?")
    public abstract static class LambdaNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean lambda(RubyProc proc) {
            return proc.type == ProcType.LAMBDA;
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray parameters(RubyProc proc) {
            final ArgumentDescriptor[] argsDesc = proc.getArgumentDescriptors();
            final boolean isLambda = proc.type == ProcType.LAMBDA;
            return ArgumentDescriptorUtils
                    .argumentDescriptorsToParameters(getLanguage(), getContext(), argsDesc, isLambda);
        }

    }

    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object sourceLocation(RubyProc proc) {
            SourceSection sourceSection = proc.getSharedMethodInfo().getSourceSection();

            final String sourcePath = getLanguage().getSourcePath(sourceSection.getSource());
            if (!sourceSection.isAvailable() || sourcePath.endsWith("/lib/truffle/truffle/cext.rb")) {
                return nil;
            } else {
                final RubyString file = createString(
                        fromJavaStringNode,
                        sourcePath,
                        Encodings.UTF_8);

                return createArray(new Object[]{ file, sourceSection.getStartLine() });
            }
        }

    }


    @Primitive(name = "proc_create_same_arity")
    public abstract static class ProcCreateSameArityNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyProc createSameArityProc(RubyProc userProc, RubyProc block) {
            final RubyProc composedProc = block.withArity(
                    userProc.arity,
                    userProc.argumentDescriptors,
                    coreLibrary().procClass,
                    getLanguage().procShape);
            AllocationTracing.trace(composedProc, this);
            return composedProc;
        }
    }

    @Primitive(name = "proc_specify_arity", lowerFixnum = 1)
    public abstract static class ProcSpecifyArityNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyProc specifyArityProc(RubyProc block, int argc) {
            final Arity newArity;
            if (argc <= -1) {
                newArity = new Arity(-(argc + 1), 0, true);
            } else {
                newArity = new Arity(argc, 0, false);
            }

            final RubyProc composedProc = block.withArity(
                    newArity,
                    null,
                    coreLibrary().procClass,
                    getLanguage().procShape);
            AllocationTracing.trace(composedProc, this);
            return composedProc;
        }
    }

    @Primitive(name = "proc_symbol_to_proc_symbol")
    public abstract static class ProcSymbolToProcSymbolNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object symbolToProcSymbol(RubyProc proc) {
            if (proc.arity == SymbolNodes.ToProcNode.ARITY) {
                return getSymbol(proc.getSharedMethodInfo().getBacktraceName());
            } else {
                return nil;
            }
        }

    }

    @Primitive(name = "single_block_arg")
    public abstract static class SingleBlockArgNode extends PrimitiveNode {

        @Specialization
        protected Object singleBlockArg(VirtualFrame frame,
                @Cached ConditionProfile emptyArgsProfile,
                @Cached ConditionProfile singleArgProfile) {

            /* In Rubinius, this method inspects the values yielded to the block, regardless of whether the block
             * captures the values, and returns the first value in the list of values yielded to the block.
             *
             * NB: In our case the arguments have already been destructured by the time this node is encountered. Thus,
             * we don't need to do the destructuring work that Rubinius would do and in the case that we receive
             * multiple arguments we need to reverse the destructuring by collecting the values into an array. */
            int userArgumentCount = RubyArguments.getPositionalArgumentsCount(frame, false);

            if (emptyArgsProfile.profile(userArgumentCount == 0)) {
                return nil;
            } else {
                if (singleArgProfile.profile(userArgumentCount == 1)) {
                    return RubyArguments.getArgument(frame, 0);
                } else {
                    Object[] extractedArguments = RubyArguments.getPositionalArguments(frame.getArguments(), false);
                    return createArray(extractedArguments, userArgumentCount);
                }
            }
        }
    }

    @Primitive(name = "proc_ruby2_keywords", raiseIfFrozen = 0)
    public abstract static class ProcRuby2KeywordsNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object ruby2Keywords(RubyProc proc) {
            return MethodRuby2KeywordsNode.ruby2Keywords(proc.getSharedMethodInfo(), proc.callTarget);
        }
    }
}
