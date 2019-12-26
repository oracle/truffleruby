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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.locals.ReadDeclarationVariableNode;
import org.truffleruby.language.locals.WriteDeclarationVariableNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule("Truffle::Graal")
public abstract class TruffleGraalNodes {

    @CoreMethod(names = "always_split", onSingleton = true, required = 1, argumentNames = "method_or_proc")
    public abstract static class AlwaysSplitNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyMethod(rubyMethod)")
        protected DynamicObject splitMethod(DynamicObject rubyMethod) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                InternalMethod internalMethod = Layouts.METHOD.getMethod(rubyMethod);
                internalMethod.getSharedMethodInfo().setAlwaysClone(true);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyUnboundMethod(rubyMethod)")
        protected DynamicObject splitUnboundMethod(DynamicObject rubyMethod) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                InternalMethod internalMethod = Layouts.UNBOUND_METHOD.getMethod(rubyMethod);
                internalMethod.getSharedMethodInfo().setAlwaysClone(true);
            }
            return rubyMethod;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(rubyProc)")
        protected DynamicObject splitProc(DynamicObject rubyProc) {
            if (getContext().getOptions().ALWAYS_SPLIT_HONOR) {
                Layouts.PROC.getSharedMethodInfo(rubyProc).setAlwaysClone(true);
            }
            return rubyProc;
        }
    }

    /**
     * This method creates a new Proc with a copy of the captured variables' values, which is
     * correct if these variables are not changed in the parent scope later on. It works by
     * replacing {@link ReadDeclarationVariableNode} with the captured variables' values. This
     * avoids constantly reading from the declaration frame (which always escapes in a
     * define_method) and folds many checks on these captured variables since their values become
     * compilation constants.
     * <p>
     * Similar to Smalltalk's fixTemps, but not mutating the Proc.
     */
    @CoreMethod(names = "copy_captured_locals", onSingleton = true, required = 1)
    public abstract static class CopyCapturedLocalsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(proc)")
        protected DynamicObject copyCapturedLocals(DynamicObject proc) {
            final MaterializedFrame declarationFrame = Layouts.PROC.getDeclarationFrame(proc);

            final RootCallTarget callTarget = Layouts.PROC.getCallTargetForType(proc);
            final RubyRootNode rootNode = (RubyRootNode) callTarget.getRootNode();

            final RubyNode newBody = NodeUtil.cloneNode(rootNode.getBody());

            assert NodeUtil.findAllNodeInstances(newBody, WriteDeclarationVariableNode.class).isEmpty();

            for (ReadDeclarationVariableNode readNode : NodeUtil
                    .findAllNodeInstances(newBody, ReadDeclarationVariableNode.class)) {
                MaterializedFrame frame = RubyArguments
                        .getDeclarationFrame(declarationFrame, readNode.getFrameDepth() - 1);
                Object value = frame.getValue(readNode.getFrameSlot());
                readNode.replace(new ObjectLiteralNode(value));
            }
            final RubyRootNode newRootNode = new RubyRootNode(
                    getContext(),
                    rootNode.getSourceSection(),
                    rootNode.getFrameDescriptor(),
                    rootNode.getSharedMethodInfo(),
                    newBody,
                    true);
            final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final RootCallTarget callTargetForLambdas;
            if (Layouts.PROC.getType(proc) == ProcType.LAMBDA) {
                callTargetForLambdas = newCallTarget;
            } else {
                callTargetForLambdas = Layouts.PROC.getCallTargetForLambdas(proc);
            }

            final Object[] args = RubyArguments
                    .pack(null, null, RubyArguments.getMethod(declarationFrame), null, nil(), null, EMPTY_ARGUMENTS);
            // The Proc no longer needs the original declaration frame. However, all procs must have a
            // declaration frame (to allow Proc#binding) so we shall create an empty one.
            final MaterializedFrame newDeclarationFrame = Truffle
                    .getRuntime()
                    .createMaterializedFrame(args, coreLibrary().emptyDescriptor);

            return coreLibrary().procFactory.newInstance(Layouts.PROC.build(
                    Layouts.PROC.getType(proc),
                    Layouts.PROC.getSharedMethodInfo(proc),
                    newCallTarget,
                    callTargetForLambdas,
                    newDeclarationFrame,
                    Layouts.PROC.getMethod(proc),
                    Layouts.PROC.getBlock(proc),
                    Layouts.PROC.getFrameOnStackMarker(proc),
                    Layouts.PROC.getDeclarationContext(proc)));
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
        protected DynamicObject assertNotCompiled() {
            if (CompilerDirectives.inCompiledCode()) {
                compiledBoundary();
            }

            return nil();
        }

        @TruffleBoundary
        private void compiledBoundary() {
            throw new RaiseException(getContext(), coreExceptions().graalErrorAssertNotCompiledCompiled(this), true);
        }
    }

    @Primitive(name = "compiler_bailout")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class BailoutNode extends PrimitiveNode {

        @Specialization(guards = "isRubyString(message)")
        protected DynamicObject bailout(DynamicObject message,
                @Cached ToJavaStringNode toJavaStringNode) {
            CompilerDirectives.bailout(toJavaStringNode.executeToJavaString(message));
            return nil();
        }
    }


}
