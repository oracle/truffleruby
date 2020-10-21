/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Define a method from a module body (module/class/class << self ... end). */
public class ModuleBodyDefinitionNode extends RubyContextNode {

    private final String name;
    private final SharedMethodInfo sharedMethodInfo;
    private final RootCallTarget callTarget;
    private final boolean captureBlock;
    private final boolean dynamicLexicalScope;
    private final Map<RubyModule, LexicalScope> lexicalScopes;

    public ModuleBodyDefinitionNode(
            String name,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTarget,
            boolean captureBlock,
            boolean dynamicLexicalScope) {
        this.name = name;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTarget = callTarget;
        this.captureBlock = captureBlock;
        this.dynamicLexicalScope = dynamicLexicalScope;
        this.lexicalScopes = dynamicLexicalScope ? new ConcurrentHashMap<>() : null;
    }

    public ModuleBodyDefinitionNode(ModuleBodyDefinitionNode node) {
        this(node.name, node.sharedMethodInfo, node.callTarget, node.captureBlock, node.dynamicLexicalScope);
    }

    public InternalMethod createMethod(VirtualFrame frame, LexicalScope staticLexicalScope, RubyModule module) {
        final RubyProc capturedBlock;

        if (captureBlock) {
            capturedBlock = RubyArguments.getBlock(frame);
        } else {
            capturedBlock = null;
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
                callTarget,
                null,
                capturedBlock);
    }

    @TruffleBoundary
    private LexicalScope prepareLexicalScope(LexicalScope staticLexicalScope, LexicalScope parentLexicalScope,
            RubyModule module) {
        staticLexicalScope.unsafeSetLiveModule(module);
        if (!dynamicLexicalScope) {
            return staticLexicalScope;
        } else {
            // Cache the scope per module in case the module body is run multiple times.
            // This allows dynamic constant lookup to cache better.
            return ConcurrentOperations.getOrCompute(
                    lexicalScopes,
                    module,
                    k -> new LexicalScope(parentLexicalScope, module));
        }
    }

}
