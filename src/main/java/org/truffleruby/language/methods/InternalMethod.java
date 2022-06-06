/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.NodeFactory;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.CachedSupplier;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;

/** A Ruby method: either a method in a module, a literal module/class body or some meta-information for eval'd code.
 * Blocks capture the method in which they are defined. */
public class InternalMethod implements ObjectGraphNode {

    private final SharedMethodInfo sharedMethodInfo;
    /** Contains the "dynamic" lexical scope in case this method is under a class << expr; HERE; end */
    private final LexicalScope lexicalScope;
    private final DeclarationContext declarationContext;
    /** The active refinements used during lookup and remembered this way on the method we will call */
    private final DeclarationContext activeRefinements;
    private final String name;

    /** The module on which the method was initially declared in the source code (e.g., with def) */
    private final RubyModule declaringModule;
    /** The module owning this InternalMethod (i.e., the method is part of that module's instance methods) */
    private final RubyModule owner;
    private final Visibility visibility;
    private final boolean undefined;
    private final boolean unimplemented; // similar to MRI's rb_f_notimplement
    /** True if the method is defined in the core library (in Java or Ruby) */
    private final boolean builtIn;
    public final NodeFactory<? extends RubyBaseNode> alwaysInlinedNodeFactory;
    private final RubyProc proc; // only if method is created from a Proc

    private final CachedSupplier<RootCallTarget> callTargetSupplier;
    @CompilationFinal private RootCallTarget callTarget;
    private final Object capturedBlock;

    public static InternalMethod fromProc(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            DeclarationContext declarationContext,
            String name,
            RubyModule declaringModule,
            Visibility visibility,
            RubyProc proc,
            RootCallTarget callTarget) {
        return new InternalMethod(
                context,
                sharedMethodInfo,
                proc.method.getLexicalScope(),
                declarationContext,
                name,
                declaringModule,
                visibility,
                false,
                null,
                proc,
                callTarget,
                null,
                Nil.get());
    }

    public InternalMethod(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            RubyModule declaringModule,
            Visibility visibility,
            RootCallTarget callTarget) {
        this(
                context,
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                visibility,
                false,
                null,
                null,
                callTarget,
                null,
                Nil.get());
    }

    /** Constructor for new methods, computing builtIn from the context */
    public InternalMethod(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            RubyModule declaringModule,
            Visibility visibility,
            boolean undefined,
            NodeFactory<? extends RubyBaseNode> alwaysInlined,
            RubyProc proc,
            RootCallTarget callTarget,
            CachedSupplier<RootCallTarget> callTargetSupplier,
            Object capturedBlock) {
        this(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                declaringModule,
                visibility,
                undefined,
                false,
                !context.getCoreLibrary().isLoaded(),
                alwaysInlined,
                null,
                proc,
                callTarget,
                callTargetSupplier,
                capturedBlock);
    }

    private InternalMethod(
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            RubyModule declaringModule,
            RubyModule owner,
            Visibility visibility,
            boolean undefined,
            boolean unimplemented,
            boolean builtIn,
            NodeFactory<? extends RubyBaseNode> alwaysInlined,
            DeclarationContext activeRefinements,
            RubyProc proc,
            RootCallTarget callTarget,
            CachedSupplier<RootCallTarget> callTargetSupplier,
            Object capturedBlock) {
        assert declaringModule != null;
        assert lexicalScope != null;
        assert !sharedMethodInfo.isBlock() : sharedMethodInfo;
        assert callTarget == null || RubyRootNode.of(callTarget).getSharedMethodInfo() == sharedMethodInfo;
        assert Nil.is(capturedBlock) || capturedBlock instanceof RubyProc : capturedBlock;
        this.sharedMethodInfo = sharedMethodInfo;
        this.lexicalScope = lexicalScope;
        this.declarationContext = declarationContext;
        this.declaringModule = declaringModule;
        this.owner = owner;
        this.name = name;
        this.visibility = visibility;
        this.undefined = undefined;
        this.unimplemented = unimplemented;
        this.builtIn = builtIn;
        this.alwaysInlinedNodeFactory = alwaysInlined;
        this.activeRefinements = activeRefinements;
        this.proc = proc;
        this.callTarget = callTarget;
        this.callTargetSupplier = callTargetSupplier;
        this.capturedBlock = capturedBlock;
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public RubyModule getDeclaringModule() {
        return declaringModule;
    }

    public RubyModule getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isDefined() {
        return !undefined;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public boolean isImplemented() {
        return !unimplemented;
    }

    public boolean isUnimplemented() {
        return unimplemented;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public boolean alwaysInlined() {
        return alwaysInlinedNodeFactory != null;
    }

    public int getArityNumber() {
        return sharedMethodInfo.getArity().getMethodArityNumber();
    }

    public RubyProc getProc() {
        return proc;
    }

    public RootCallTarget getCallTarget() {
        if (callTarget == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callTarget = callTargetSupplier.get();
            assert RubyRootNode.of(callTarget).getSharedMethodInfo() == sharedMethodInfo;
        }
        return callTarget;
    }

    public InternalMethod withDeclaringModule(RubyModule newDeclaringModule) {
        if (newDeclaringModule == declaringModule) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    newDeclaringModule,
                    owner,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    activeRefinements,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod withOwner(RubyModule newOwner) {
        if (newOwner == owner) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    declaringModule,
                    newOwner,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    activeRefinements,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod withName(String newName) {
        if (newName.equals(name)) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    newName,
                    declaringModule,
                    owner,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    activeRefinements,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod withVisibility(Visibility newVisibility) {
        if (newVisibility == visibility) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    declaringModule,
                    owner,
                    newVisibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    activeRefinements,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod withActiveRefinements(DeclarationContext context) {
        if (context == activeRefinements) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    declaringModule,
                    owner,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    context,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod withDeclarationContext(DeclarationContext newDeclarationContext) {
        if (newDeclarationContext == declarationContext) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    newDeclarationContext,
                    name,
                    declaringModule,
                    owner,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    alwaysInlinedNodeFactory,
                    activeRefinements,
                    proc,
                    callTarget,
                    callTargetSupplier,
                    capturedBlock);
        }
    }

    public InternalMethod undefined() {
        return new InternalMethod(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                owner,
                visibility,
                true,
                unimplemented,
                builtIn,
                alwaysInlinedNodeFactory,
                activeRefinements,
                proc,
                callTarget,
                callTargetSupplier,
                capturedBlock);
    }

    public InternalMethod unimplemented() {
        return new InternalMethod(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                owner,
                visibility,
                undefined,
                true,
                builtIn,
                alwaysInlinedNodeFactory,
                activeRefinements,
                proc,
                callTarget,
                callTargetSupplier,
                capturedBlock);
    }

    @TruffleBoundary
    public boolean isProtectedMethodVisibleTo(RubyClass callerClass) {
        assert visibility == Visibility.PROTECTED;

        for (RubyModule ancestor : callerClass.fields.ancestors()) {
            if (ancestor == declaringModule || ancestor.getMetaClass() == declaringModule) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return sharedMethodInfo.toString();
    }

    @Override
    public void getAdjacentObjects(Set<Object> adjacent) {
        if (declaringModule != null) {
            adjacent.add(declaringModule);
        }

        if (proc != null) {
            adjacent.add(proc);
        }
    }

    public Object getCapturedBlock() {
        return capturedBlock;
    }

    public LexicalScope getLexicalScope() {
        return lexicalScope;
    }

    public DeclarationContext getDeclarationContext() {
        return declarationContext;
    }

    public DeclarationContext getActiveRefinements() {
        return activeRefinements;
    }
}
