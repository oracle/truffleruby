/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.locals.ReadDeclarationVariableNode;
import org.truffleruby.language.locals.WriteDeclarationVariableNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.NodeUtil;

@CoreModule("Truffle::Graal")
public abstract class TruffleGraalNodes {

    @CoreMethod(names = "always_split", onSingleton = true, required = 1, argumentNames = "method_or_proc")
    public abstract static class AlwaysSplitNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyMethod splitMethod(RubyMethod rubyMethod) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                RubyRootNode rootNode = (RubyRootNode) rubyMethod.method.getCallTarget().getRootNode();
                rootNode.setSplit(Split.ALWAYS);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization
        protected RubyUnboundMethod splitUnboundMethod(RubyUnboundMethod rubyMethod) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                RubyRootNode rootNode = (RubyRootNode) rubyMethod.method.getCallTarget().getRootNode();
                rootNode.setSplit(Split.ALWAYS);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization
        protected RubyProc splitProc(RubyProc rubyProc) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                ((RubyRootNode) rubyProc.callTargetForType.getRootNode()).setSplit(Split.ALWAYS);
                ((RubyRootNode) rubyProc.callTargetForLambdas.getRootNode()).setSplit(Split.ALWAYS);
            }
            return rubyProc;
        }
    }

    @CoreMethod(names = "never_split", onSingleton = true, required = 1, argumentNames = "method_or_proc")
    public abstract static class NeverSplitNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyMethod neverSplitMethod(RubyMethod rubyMethod) {
            if (getContext().getOptions().NEVER_SPLIT_HONOR) {
                RubyRootNode rootNode = (RubyRootNode) rubyMethod.method.getCallTarget().getRootNode();
                rootNode.setSplit(Split.NEVER);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization
        protected RubyUnboundMethod neverSplitUnboundMethod(RubyUnboundMethod rubyMethod) {
            if (getContext().getOptions().NEVER_SPLIT_HONOR) {
                RubyRootNode rootNode = (RubyRootNode) rubyMethod.method.getCallTarget().getRootNode();
                rootNode.setSplit(Split.NEVER);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization
        protected RubyProc neverSplitProc(RubyProc rubyProc) {
            if (getContext().getOptions().NEVER_SPLIT_HONOR) {
                ((RubyRootNode) rubyProc.callTargetForType.getRootNode()).setSplit(Split.NEVER);
                ((RubyRootNode) rubyProc.callTargetForLambdas.getRootNode()).setSplit(Split.NEVER);
            }
            return rubyProc;
        }
    }

    /** This method creates a new Proc with a copy of the captured variables' values, which is correct if these
     * variables are not changed in the parent scope later on. It works by replacing {@link ReadDeclarationVariableNode}
     * with the captured variables' values. This avoids constantly reading from the declaration frame (which always
     * escapes in a define_method) and folds many checks on these captured variables since their values become
     * compilation constants.
     * <p>
     * Similar to Smalltalk's fixTemps, but not mutating the Proc. */
    @CoreMethod(names = "copy_captured_locals", onSingleton = true, required = 1)
    public abstract static class CopyCapturedLocalsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyProc copyCapturedLocals(RubyProc proc) {
            final RubyRootNode rootNode = (RubyRootNode) proc.callTargetForType.getRootNode();
            final RubyNode newBody = NodeUtil.cloneNode(rootNode.getBody());

            assert NodeUtil.findAllNodeInstances(newBody, WriteDeclarationVariableNode.class).isEmpty();

            for (ReadDeclarationVariableNode readNode : NodeUtil
                    .findAllNodeInstances(newBody, ReadDeclarationVariableNode.class)) {
                MaterializedFrame frame = RubyArguments
                        .getDeclarationFrame(proc.declarationFrame, readNode.getFrameDepth() - 1);
                Object value = frame.getValue(readNode.getFrameSlot());
                readNode.replace(new ObjectLiteralNode(value));
            }
            final RubyRootNode newRootNode = new RubyRootNode(
                    getContext(),
                    rootNode.getSourceSection(),
                    rootNode.getFrameDescriptor(),
                    rootNode.getSharedMethodInfo(),
                    newBody,
                    Split.HEURISTIC);
            final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final RootCallTarget callTargetForLambdas = proc.type == ProcType.LAMBDA
                    ? newCallTarget
                    : proc.callTargetForLambdas;

            SpecialVariableStorage storage = proc.declarationStorage;

            final Object[] args = RubyArguments
                    .pack(
                            null,
                            null,
                            null,
                            RubyArguments.getMethod(proc.declarationFrame),
                            null,
                            nil,
                            null,
                            EMPTY_ARGUMENTS);

            // The Proc no longer needs the original declaration frame. However, all procs must have a
            // declaration frame (to allow Proc#binding) so we shall create an empty one.
            final MaterializedFrame newDeclarationFrame = Truffle
                    .getRuntime()
                    .createMaterializedFrame(args, coreLibrary().emptyDeclarationDescriptor);

            newDeclarationFrame.setObject(coreLibrary().emptyDeclarationSpecialVariableSlot, storage);

            return new RubyProc(
                    coreLibrary().procClass,
                    RubyLanguage.procShape,
                    proc.type,
                    proc.sharedMethodInfo,
                    newCallTarget,
                    callTargetForLambdas,
                    newDeclarationFrame,
                    storage,
                    proc.method,
                    proc.block,
                    proc.frameOnStackMarker,
                    proc.declarationContext);

            // TODO(norswap): trace allocation?
        }

    }

    @NodeChild(value = "value", type = RubyNode.class)
    @Primitive(name = "assert_compilation_constant")
    public abstract static class AssertCompilationConstantNode extends PrimitiveNode {

        @Specialization
        protected Object assertCompilationConstant(Object value) {
            if (!CompilerDirectives.isCompilationConstant(value)) {
                notConstantBoundary();
            }

            return value;
        }

        @TruffleBoundary
        private void notConstantBoundary() {
            throw new RaiseException(getContext(), coreExceptions().graalErrorAssertConstantNotConstant(this), true);
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
            throw new RaiseException(getContext(), coreExceptions().graalErrorAssertNotCompiledCompiled(this), true);
        }
    }

    @Primitive(name = "compiler_bailout")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class BailoutNode extends PrimitiveNode {

        @Specialization
        protected Object bailout(RubyString message,
                @Cached ToJavaStringNode toJavaStringNode) {
            CompilerDirectives.bailout(toJavaStringNode.executeToJavaString(message));
            return nil;
        }
    }


}
