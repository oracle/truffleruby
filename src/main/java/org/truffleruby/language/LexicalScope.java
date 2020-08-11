/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.core.module.RubyModule;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/** Instances of this class represent the Ruby lexical scope for constants, which is only changed by `class Name`,
 * `module Name` and `class << expr`. Other lexical scope features such as refinement and the default definee are
 * handled in {@link org.truffleruby.language.methods.DeclarationContext}. */
public class LexicalScope {

    public static final LexicalScope NONE = null;

    /** Not null to allow using with @Specialization */
    public static final LexicalScope IGNORE = new LexicalScope(null, null);

    private final LexicalScope parent;
    @CompilationFinal private volatile RubyModule liveModule;

    public LexicalScope(LexicalScope parent, RubyModule liveModule) {
        this.parent = parent;
        this.liveModule = liveModule;
    }

    public LexicalScope(LexicalScope parent) {
        this(parent, null);
    }

    public LexicalScope getParent() {
        return parent;
    }

    public RubyModule getLiveModule() {
        return liveModule;
    }

    public void unsafeSetLiveModule(RubyModule liveModule) {
        this.liveModule = liveModule;
    }

    @TruffleBoundary
    public static RubyModule resolveTargetModuleForClassVariables(LexicalScope lexicalScope) {
        LexicalScope scope = lexicalScope;

        // MRI logic: ignore lexical scopes (cref) referring to singleton classes
        while (RubyGuards.isSingletonClass(scope.liveModule)) {
            scope = scope.parent;
        }

        return scope.liveModule;
    }

    @Override
    public String toString() {
        return " :: " + liveModule + (parent == null ? "" : parent.toString());
    }


}
