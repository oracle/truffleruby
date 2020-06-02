/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class UsingNode extends RubyContextNode {

    public abstract void executeUsing(DynamicObject module);

    @TruffleBoundary
    @Specialization(guards = "isRubyModule(module)")
    protected void using(DynamicObject module) {
        if (RubyGuards.isRubyClass(module)) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorWrongArgumentType(module, "Module", this));
        }

        final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_WRITE);
        final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(callerFrame);
        final Map<DynamicObject, DynamicObject[]> newRefinements = usingModule(declarationContext, module);
        DeclarationContext.setRefinements(callerFrame, declarationContext, newRefinements);
    }

    @TruffleBoundary
    private Map<DynamicObject, DynamicObject[]> usingModule(DeclarationContext declarationContext,
            DynamicObject module) {
        final Map<DynamicObject, DynamicObject[]> newRefinements = new HashMap<>(declarationContext.getRefinements());

        // Iterate ancestors in reverse order so refinements upper in the chain have precedence
        final Deque<DynamicObject> reverseAncestors = new ArrayDeque<>();
        for (DynamicObject ancestor : Layouts.MODULE.getFields(module).ancestors()) {
            reverseAncestors.addFirst(ancestor);
        }

        for (DynamicObject ancestor : reverseAncestors) {
            final ConcurrentMap<DynamicObject, DynamicObject> refinements = Layouts.MODULE
                    .getFields(ancestor)
                    .getRefinements();
            for (Map.Entry<DynamicObject, DynamicObject> entry : refinements.entrySet()) {
                usingRefinement(entry.getKey(), entry.getValue(), newRefinements);
            }
        }

        return newRefinements;
    }

    private void usingRefinement(DynamicObject refinedModule, DynamicObject refinementModule,
            Map<DynamicObject, DynamicObject[]> newRefinements) {
        final DynamicObject[] refinements = newRefinements.get(refinedModule);
        if (refinements == null) {
            newRefinements.put(refinedModule, new DynamicObject[]{ refinementModule });
        } else {
            if (ArrayUtils.contains(refinements, refinementModule)) {
                // Already using this refinement
            } else {
                // Add new refinement in front
                newRefinements.put(refinedModule, unshift(refinements, refinementModule));
            }
        }
    }

    private static DynamicObject[] unshift(DynamicObject[] array, DynamicObject element) {
        final DynamicObject[] newArray = new DynamicObject[1 + array.length];
        newArray[0] = element;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

}
