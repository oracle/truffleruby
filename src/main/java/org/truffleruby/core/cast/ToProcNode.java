/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.RootCallTarget;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolNodes;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import java.util.Map;

/** The `&` in `foo(&block)`. Converts the passed block to a RubyProc or nil. */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToProcNode extends RubyContextSourceNode {

    @Specialization
    protected Nil doNil(Nil nil) {
        return nil;
    }

    @Specialization
    protected RubyProc doRubyProc(RubyProc proc) {
        return proc;
    }

    // AST-inlined version of Symbol#to_proc
    // No need to guard the refinements here since refinements are always the same in a given source location
    @Specialization(
            guards = { "isSingleContext()", "symbol == cachedSymbol" },
            assumptions = "getLanguage().coreMethodAssumptions.symbolToProcAssumption",
            limit = "1")
    protected Object doRubySymbolASTInlined(VirtualFrame frame, RubySymbol symbol,
            @Cached("symbol") RubySymbol cachedSymbol,
            @Cached("getProcForSymbol(getRefinements(frame), cachedSymbol)") RubyProc cachedProc) {
        return cachedProc;
    }

    @Specialization(guards = { "!isNil(object)", "!isRubyProc(object)" }, replaces = "doRubySymbolASTInlined")
    protected RubyProc doObject(VirtualFrame frame, Object object,
            @Cached DispatchNode toProc,
            @Cached BranchProfile errorProfile) {
        // The semantics are to call #method_missing here
        final Object coerced;
        try {
            coerced = toProc.dispatch(frame, object, "to_proc", nil, EMPTY_ARGUMENTS);
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "Proc", this));
            } else {
                throw e;
            }
        }

        if (coerced instanceof RubyProc) {
            return (RubyProc) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "Proc", "to_proc", coerced, this));
        }
    }

    protected RubyProc getProcForSymbol(Map<RubyModule, RubyModule[]> refinements, RubySymbol symbol) {
        final RootCallTarget callTarget = SymbolNodes.ToProcNode
                .getOrCreateCallTarget(getContext(), getLanguage(), symbol, refinements);
        return SymbolNodes.ToProcNode.createProc(getContext(), getLanguage(), refinements, callTarget);
    }

    protected static Map<RubyModule, RubyModule[]> getRefinements(VirtualFrame frame) {
        return RubyArguments.getDeclarationContext(frame).getRefinements();
    }

}
