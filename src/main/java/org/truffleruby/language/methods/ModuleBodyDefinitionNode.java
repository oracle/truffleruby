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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Define a method from a module body (module/class/class << self ... end). */
public class ModuleBodyDefinitionNode extends RubyBaseNode {

    private final String name;
    private final SharedMethodInfo sharedMethodInfo;
    private final RootCallTarget callTarget;
    private final boolean captureBlock;

    private final LexicalScope staticLexicalScope;
    private final Map<RubyModule, LexicalScope> dynamicLexicalScopes;

    public ModuleBodyDefinitionNode(
            String name,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTarget,
            boolean captureBlock,
            LexicalScope staticLexicalScope) {
        this.name = name;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTarget = callTarget;
        this.captureBlock = captureBlock;
        this.staticLexicalScope = staticLexicalScope;
        this.dynamicLexicalScopes = staticLexicalScope != null ? null : new ConcurrentHashMap<>();
    }

    public InternalMethod createMethod(VirtualFrame frame, RubyModule module) {
        final Object capturedBlock;

        if (captureBlock) {
            capturedBlock = RubyArguments.getBlock(frame);
        } else {
            capturedBlock = nil();
        }

        final LexicalScope parentLexicalScope = RubyArguments.getMethod(frame).getLexicalScope();
        final LexicalScope lexicalScope = prepareLexicalScope(staticLexicalScope, parentLexicalScope, module);
        final DeclarationContext declarationContext = new DeclarationContext(
                Visibility.PUBLIC,
                new DeclarationContext.FixedDefaultDefinee(module),
                RubyArguments.getDeclarationContext(frame).getRefinements());
        return new InternalMethod(
                getContext(),
                sharedMethodInfo,
                lexicalScope,
                declarationContext,
                name,
                module,
                Visibility.PUBLIC,
                false,
                null,
                null,
                callTarget,
                null,
                capturedBlock);
    }

    @TruffleBoundary
    private LexicalScope prepareLexicalScope(LexicalScope staticLexicalScope, LexicalScope parentLexicalScope,
            RubyModule module) {
        if (staticLexicalScope != null) {
            staticLexicalScope.unsafeSetLiveModule(module);
            return staticLexicalScope;
        } else if (getLanguage().singleContext) {
            // Cache the scope per module in case the module body is run multiple times.
            // This allows dynamic constant lookup to cache better.
            return ConcurrentOperations.getOrCompute(
                    dynamicLexicalScopes,
                    module,
                    k -> new LexicalScope(parentLexicalScope, module));
        } else {
            return new LexicalScope(parentLexicalScope, module);
        }
    }

}
