/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

/**
 * A Ruby method: either a method in a module,
 * a literal module/class body
 * or some meta-information for eval'd code.
 * Blocks capture the method in which they are defined.
 */
public class InternalMethod implements ObjectGraphNode {

    private final SharedMethodInfo sharedMethodInfo;
    /** Contains the "dynamic" lexical scope in case this method is under a class << expr; HERE; end */
    private final LexicalScope lexicalScope;
    private final DeclarationContext declarationContext;
    private final String name;

    private final DynamicObject declaringModule;
    private final Visibility visibility;
    private final boolean undefined;
    private final boolean unimplemented; // similar to MRI's rb_f_notimplement
    private final boolean builtIn;
    /** A flag to tell whether there exist refinements of this method */
    private final boolean refined;
    private final DynamicObject proc; // only if method is created from a Proc

    private final CallTarget callTarget;
    private final DynamicObject capturedBlock;
    private final InternalMethod originalMethod;

    public static InternalMethod fromProc(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            DeclarationContext declarationContext,
            String name,
            DynamicObject declaringModule,
            Visibility visibility,
            DynamicObject proc,
            CallTarget callTarget) {
        return new InternalMethod(
                context,
                sharedMethodInfo,
                Layouts.PROC.getMethod(proc).getLexicalScope(),
                declarationContext,
                name,
                declaringModule,
                visibility,
                false,
                proc,
                callTarget,
                Layouts.PROC.getBlock(proc));
    }

    public InternalMethod(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            DynamicObject declaringModule,
            Visibility visibility,
            CallTarget callTarget) {
        this(context, sharedMethodInfo, lexicalScope, declarationContext, name, declaringModule, visibility, false, null, callTarget, null);
    }

    public InternalMethod(
            RubyContext context,
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            DynamicObject declaringModule,
            Visibility visibility,
            boolean undefined,
            DynamicObject proc,
            CallTarget callTarget,
            DynamicObject capturedBlock) {
        this(sharedMethodInfo, lexicalScope, declarationContext, name, declaringModule, visibility, undefined, false,
                !context.getCoreLibrary().isLoaded(), false, proc, callTarget, capturedBlock, null);
    }

    private InternalMethod(
            SharedMethodInfo sharedMethodInfo,
            LexicalScope lexicalScope,
            DeclarationContext declarationContext,
            String name,
            DynamicObject declaringModule,
            Visibility visibility,
            boolean undefined,
            boolean unimplemented,
            boolean builtIn,
            boolean refined,
            DynamicObject proc,
            CallTarget callTarget,
            DynamicObject capturedBlock,
            InternalMethod originalMethod) {
        assert RubyGuards.isRubyModule(declaringModule);
        assert lexicalScope != null;
        assert originalMethod == null || !originalMethod.isRefined();
        this.sharedMethodInfo = sharedMethodInfo;
        this.lexicalScope = lexicalScope;
        this.declarationContext = declarationContext;
        this.declaringModule = declaringModule;
        this.name = name;
        this.visibility = visibility;
        this.undefined = undefined;
        this.unimplemented = unimplemented;
        this.builtIn = builtIn;
        this.refined = refined;
        this.proc = proc;
        this.callTarget = callTarget;
        this.capturedBlock = capturedBlock;
        this.originalMethod = originalMethod;
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public DynamicObject getDeclaringModule() {
        return declaringModule;
    }

    public String getName() {
        return name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public boolean isUnimplemented() {
        return unimplemented;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public boolean isRefined() {
        return refined;
    }

    public CallTarget getCallTarget() {
        return callTarget;
    }

    public InternalMethod getOriginalMethod() {
        return originalMethod;
    }

    public InternalMethod withDeclaringModule(DynamicObject newDeclaringModule) {
        assert RubyGuards.isRubyModule(newDeclaringModule);

        if (newDeclaringModule == declaringModule) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    newDeclaringModule,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    refined,
                    proc,
                    callTarget,
                    capturedBlock,
                    originalMethod);
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
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    refined,
                    proc,
                    callTarget,
                    capturedBlock,
                    originalMethod);
        }
    }

    public InternalMethod withRefined(boolean newRefined) {
        if (refined == newRefined) {
            return this;
        } else {
            return new InternalMethod(
                    sharedMethodInfo,
                    lexicalScope,
                    declarationContext,
                    name,
                    declaringModule,
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    newRefined,
                    proc,
                    callTarget,
                    capturedBlock,
                    originalMethod);
        }
    }

    public InternalMethod withOriginalMethod(InternalMethod newOriginalMethod) {
        if (originalMethod == newOriginalMethod) {
            return this;
        } else {
            return new InternalMethod(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                visibility,
                undefined,
                unimplemented,
                builtIn,
                refined,
                proc,
                callTarget,
                capturedBlock,
                newOriginalMethod);
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
                    newVisibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    refined,
                    proc,
                    callTarget,
                    capturedBlock,
                    originalMethod);
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
                    visibility,
                    undefined,
                    unimplemented,
                    builtIn,
                    refined,
                    proc,
                    callTarget,
                    capturedBlock,
                    originalMethod);
        }
    }

    public InternalMethod undefined() {
        return new InternalMethod(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                visibility,
                true,
                unimplemented,
                builtIn,
                refined,
                proc,
                callTarget,
                capturedBlock,
                originalMethod);
    }

    public InternalMethod unimplemented() {
        return new InternalMethod(
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                declaringModule,
                visibility,
                undefined,
                true,
                builtIn,
                refined,
                proc,
                callTarget,
                capturedBlock,
                originalMethod);
    }

    @TruffleBoundary
    public boolean isVisibleTo(DynamicObject callerClass) {
        assert RubyGuards.isRubyClass(callerClass);

        switch (visibility) {
            case PUBLIC:
                return true;

            case PROTECTED:
                return isProtectedMethodVisibleTo(callerClass);

            case PRIVATE:
                // A private method may only be called with an implicit receiver,
                // in which case the visibility must not be checked.
                return false;

            default:
                throw new UnsupportedOperationException(visibility.name());
        }
    }

    @TruffleBoundary
    public boolean isProtectedMethodVisibleTo(DynamicObject callerClass) {
        assert visibility == Visibility.PROTECTED;

        for (DynamicObject ancestor : Layouts.MODULE.getFields(callerClass).ancestors()) {
            if (ancestor == declaringModule || Layouts.BASIC_OBJECT.getMetaClass(ancestor) == declaringModule) {
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
    public void getAdjacentObjects(Set<DynamicObject> adjacent) {
        if (declaringModule != null) {
            adjacent.add(declaringModule);
        }

        if (proc != null) {
            adjacent.add(proc);
        }
    }

    public DynamicObject getCapturedBlock() {
        return capturedBlock;
    }

    public LexicalScope getLexicalScope() {
        return lexicalScope;
    }

    public DeclarationContext getDeclarationContext() {
        return declarationContext;
    }

}
