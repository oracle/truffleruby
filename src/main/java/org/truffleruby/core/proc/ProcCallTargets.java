/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.parser.MethodTranslator;

import java.util.function.Supplier;

/** Holds compiled call target for a {@link BlockDefinitionNode} (one instance per node). Each {@link RubyProc} created
 * by evaluating the definition has a reference to one of these. The point is that there are two possible call targets
 * for blocks: the proc and the lambda one. We only eagerly compile one in {@link MethodTranslator#compileBlockNode},
 * but we might need to later compile the other, given a {@link RubyProc} instance. This class thus acts as a shared
 * cache for the call targets. */
public final class ProcCallTargets {

    // At least one of those two call targets won't be null.
    private @CompilationFinal RootCallTarget callTargetForProc;
    private @CompilationFinal RootCallTarget callTargetForLambda;

    // Non-null if one of the call targets is null.
    private @CompilationFinal Supplier<RootCallTarget> altCallTargetCompiler;

    public ProcCallTargets(
            RootCallTarget callTargetForProc,
            RootCallTarget callTargetForLambda,
            Supplier<RootCallTarget> altCallTargetCompiler) {
        assert callTargetForProc != null || callTargetForLambda != null;

        this.callTargetForProc = callTargetForProc;
        this.callTargetForLambda = callTargetForLambda;
        this.altCallTargetCompiler = altCallTargetCompiler;
    }

    public ProcCallTargets(RootCallTarget callTargetForProc, RootCallTarget callTargetForLambda) {
        this(callTargetForProc, callTargetForLambda, null);
    }

    public RootCallTarget getCallTargetForProc() {
        if (callTargetForProc == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callTargetForProc = altCallTargetCompiler.get();
            copySplit(callTargetForLambda, callTargetForProc);
            altCallTargetCompiler = null;
        }
        return callTargetForProc;
    }

    public RootCallTarget getCallTargetForLambda() {
        if (callTargetForLambda == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callTargetForLambda = altCallTargetCompiler.get();
            copySplit(callTargetForProc, callTargetForLambda);
            altCallTargetCompiler = null;
        }
        return callTargetForLambda;
    }

    private void copySplit(RootCallTarget src, RootCallTarget dst) {
        RubyRootNode.forTarget(dst).setSplit(RubyRootNode.forTarget(src).getSplit());
    }
}
