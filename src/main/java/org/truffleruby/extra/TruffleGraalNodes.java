/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.cast.ToCallTargetNode;
import org.truffleruby.core.proc.ProcCallTargets;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyLambdaRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.annotations.Split;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.locals.ReadDeclarationVariableNode;
import org.truffleruby.language.locals.WriteDeclarationVariableNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.NodeUtil;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

@CoreModule("Truffle::Graal")
public abstract class TruffleGraalNodes {

    @CoreMethod(names = "always_split", onSingleton = true, required = 1)
    public abstract static class AlwaysSplitNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object alwaysSplit(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                RubyRootNode.of(callTarget).setSplit(Split.ALWAYS);
            }
            return executable;
        }
    }

    @CoreMethod(names = "never_split", onSingleton = true, required = 1)
    public abstract static class NeverSplitNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object neverSplit(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            if (getContext().getOptions().NEVER_SPLIT_HONOR) {
                RubyRootNode.of(callTarget).setSplit(Split.NEVER);
            }
            return executable;
        }
    }

    /** This method creates a new Proc for an existing <b>lambda</b> proc with a copy of the captured variables' values,
     * which is correct if these variables are not changed in the parent scope later on. It works by replacing
     * {@link ReadDeclarationVariableNode} with the captured variables' values. This avoids constantly reading from the
     * declaration frame (which always escapes in a define_method) and folds many checks on these captured variables
     * since their values become compilation constants.
     * <p>
     * Similar to Smalltalk's fixTemps, but not mutating the Proc. */
    @CoreMethod(names = "copy_captured_locals", onSingleton = true, required = 1)
    public abstract static class CopyCapturedLocalsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "proc.isLambda()")
        protected RubyProc copyCapturedLocals(RubyProc proc) {
            final RubyLambdaRootNode rootNode = RubyLambdaRootNode.of(proc.callTarget);
            final RubyNode newBody = rootNode.copyBody();

            assert NodeUtil.findAllNodeInstances(newBody, WriteDeclarationVariableNode.class).isEmpty();

            for (ReadDeclarationVariableNode readNode : NodeUtil
                    .findAllNodeInstances(newBody, ReadDeclarationVariableNode.class)) {
                final MaterializedFrame frame = RubyArguments
                        .getDeclarationFrame(proc.declarationFrame, readNode.getFrameDepth() - 1);
                final Object value = frame.getValue(readNode.getFrameSlot());
                readNode.replace(new ObjectLiteralNode(value));
            }

            final RubyLambdaRootNode newRootNode = rootNode.copyRootNode(rootNode.getSharedMethodInfo(), newBody);
            final RootCallTarget newCallTarget = newRootNode.getCallTarget();

            final SpecialVariableStorage variables = proc.declarationVariables;

            final Object[] args = RubyArguments
                    .pack(
                            null,
                            null,
                            RubyArguments.getMethod(proc.declarationFrame),
                            null,
                            nil,
                            nil,
                            EmptyArgumentsDescriptor.INSTANCE,
                            EMPTY_ARGUMENTS);

            // The Proc no longer needs the original declaration frame. However, all procs must have a
            // declaration frame (to allow Proc#binding) so we shall create an empty one.
            final MaterializedFrame newDeclarationFrame = getLanguage().createEmptyDeclarationFrame(args, variables);

            return new RubyProc(
                    coreLibrary().procClass,
                    getLanguage().procShape,
                    ProcType.LAMBDA,
                    proc.arity,
                    proc.argumentDescriptors,
                    new ProcCallTargets(newCallTarget),
                    newCallTarget,
                    newDeclarationFrame,
                    variables,
                    proc.declaringMethod,
                    proc.frameOnStackMarker,
                    proc.declarationContext);

            // TODO(norswap): trace allocation?
        }

    }

    @Primitive(name = "assert_compilation_constant")
    public abstract static class AssertCompilationConstantNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object assertCompilationConstant(Object value) {
            if (!CompilerDirectives.isCompilationConstant(value)) {
                notConstantBoundary();
            }

            return value;
        }

        @TruffleBoundary
        private void notConstantBoundary() {
            throw new RaiseException(getContext(), coreExceptions().graalErrorAssertConstantNotConstant(this));
        }
    }

    @Primitive(name = "assert_not_compiled")
    public abstract static class AssertNotCompilationConstantNode extends PrimitiveNode {

        @Specialization
        protected Object assertNotCompiled() {
            if (CompilerDirectives.inCompiledCode()) {
                compiledBoundary();
            }

            return nil;
        }

        @TruffleBoundary
        private void compiledBoundary() {
            throw new RaiseException(getContext(), coreExceptions().graalErrorAssertNotCompiledCompiled(this));
        }
    }

    @Primitive(name = "compiler_bailout")
    public abstract static class BailoutNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(message)", limit = "1")
        protected Object bailout(Object message,
                @Cached RubyStringLibrary strings,
                @Cached ToJavaStringNode toJavaStringNode) {
            CompilerDirectives.bailout(toJavaStringNode.executeToJavaString(message));
            return nil;
        }
    }

    @Primitive(name = "blackhole")
    public abstract static class BlackholeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object blackhole(boolean value) {
            CompilerDirectives.blackhole(value);
            return nil;
        }

        @Specialization
        protected Object blackhole(int value) {
            CompilerDirectives.blackhole(value);
            return nil;
        }

        @Specialization
        protected Object blackhole(long value) {
            CompilerDirectives.blackhole(value);
            return nil;
        }

        @Specialization
        protected Object blackhole(double value) {
            CompilerDirectives.blackhole(value);
            return nil;
        }

        @Specialization
        protected Object blackhole(Object value) {
            CompilerDirectives.blackhole(value);
            return nil;
        }

    }

    @CoreMethod(names = "total_compilation_time", onSingleton = true)
    public abstract static class TotalCompilationTimeNode extends CoreMethodArrayArgumentsNode {
        private static CompilationMXBean bean;

        @TruffleBoundary
        @Specialization
        protected final long totalCompilationTime() {
            if (bean == null) {
                bean = ManagementFactory.getCompilationMXBean();
            }

            return bean.getTotalCompilationTime();
        }
    }
}
