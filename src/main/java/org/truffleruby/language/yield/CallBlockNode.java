/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.yield;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.DeclarationContext;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;

@ReportPolymorphism
@GenerateUncached
public abstract class CallBlockNode extends RubyBaseWithoutContextNode {

    public static CallBlockNode create() {
        return CallBlockNodeGen.create();
    }

    public abstract Object executeCallBlock(DeclarationContext declarationContext, DynamicObject block, Object self,
            Object blockArgument, Object[] arguments);

    // blockArgument is typed as Object below because it must accept "null".
    @Specialization(guards = "getBlockCallTarget(block) == cachedCallTarget", limit = "getCacheLimit()")
    protected Object callBlockCached(
            DeclarationContext declarationContext,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("getBlockCallTarget(block)") RootCallTarget cachedCallTarget,
            @Cached("createBlockCallNode(context, block, cachedCallTarget)") DirectCallNode callNode) {
        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, arguments);
        return callNode.call(frameArguments);
    }

    @Specialization(replaces = "callBlockCached")
    protected Object callBlockUncached(
            DeclarationContext declarationContext,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @Cached IndirectCallNode callNode) {
        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, arguments);
        return callNode.call(getBlockCallTarget(block), frameArguments);
    }

    private Object[] packArguments(DeclarationContext declarationContext, DynamicObject block, Object self,
            Object blockArgument, Object[] arguments) {
        return RubyArguments.pack(
                Layouts.PROC.getDeclarationFrame(block),
                null,
                Layouts.PROC.getMethod(block),
                declarationContext,
                Layouts.PROC.getFrameOnStackMarker(block),
                self,
                (DynamicObject) blockArgument,
                arguments);
    }

    protected static RootCallTarget getBlockCallTarget(DynamicObject block) {
        return Layouts.PROC.getCallTargetForType(block);
    }

    protected DirectCallNode createBlockCallNode(RubyContext context, DynamicObject block, RootCallTarget callTarget) {
        final DirectCallNode callNode = Truffle.getRuntime().createDirectCallNode(callTarget);

        final boolean clone = Layouts.PROC.getSharedMethodInfo(block).shouldAlwaysClone() ||
                context.getOptions().YIELD_ALWAYS_CLONE;
        if (clone && callNode.isCallTargetCloningAllowed()) {
            callNode.cloneCallTarget();
        }

        if (context.getOptions().YIELD_ALWAYS_INLINE && callNode.isInlinable()) {
            callNode.forceInlining();
        }

        return callNode;
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().YIELD_CACHE;
    }

}
