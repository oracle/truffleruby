/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.MetaClassNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.HashMap;
import java.util.Map;

/** Caches {@link ModuleOperations#lookupSuperMethod} on an actual instance. */
public abstract class LookupSuperMethodNode extends RubyContextNode {

    @Child private MetaClassNode metaClassNode;

    public abstract InternalMethod executeLookupSuperMethod(VirtualFrame frame, Object self);

    // The check for same metaClass is overly restrictive,
    // but seems to be the only reasonable check in term of performance.
    // The ideal condition would be to check if both ancestor lists starting at
    // the current method's module are identical, which is non-trivial
    // if the current method's module is an (included) module and not a class.

    @Specialization(
            guards = { "getCurrentMethod(frame) == currentMethod", "metaClass(self) == selfMetaClass" },
            assumptions = "superMethod.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupSuperMethodCachedDynamicObject(VirtualFrame frame, DynamicObject self,
            @Cached("getCurrentMethod(frame)") InternalMethod currentMethod,
            @Cached("metaClass(self)") DynamicObject selfMetaClass,
            @Cached("doLookup(frame, currentMethod, selfMetaClass)") MethodLookupResult superMethod) {
        return superMethod.getMethod();
    }

    @Specialization
    protected InternalMethod lookupSuperMethodUncached(VirtualFrame frame, Object self) {
        final InternalMethod currentMethod = getCurrentMethod(frame);
        final DynamicObject selfMetaClass = metaClass(self);
        return doLookup(frame, currentMethod, selfMetaClass).getMethod();
    }

    protected InternalMethod getCurrentMethod(VirtualFrame frame) {
        return RubyArguments.getMethod(frame);
    }

    protected DynamicObject metaClass(Object object) {
        if (metaClassNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            metaClassNode = insert(MetaClassNode.create());
        }
        return metaClassNode.executeMetaClass(object);
    }

    protected MethodLookupResult doLookup(VirtualFrame frame, InternalMethod currentMethod,
            DynamicObject selfMetaClass) {
        assert RubyGuards.isRubyClass(selfMetaClass);

        MethodLookupResult superMethod = ModuleOperations
                .lookupSuperMethod(
                        currentMethod,
                        selfMetaClass,
                        getDeclarationContext(frame, currentMethod, selfMetaClass));
        // TODO (eregon, 12 June 2015): Is this correct?
        if (!superMethod.isDefined()) {
            return superMethod.withNoMethod();
        }
        return superMethod;
    }

    protected int getCacheLimit() {
        return getContext().getOptions().METHOD_LOOKUP_CACHE;
    }

    private DeclarationContext getDeclarationContext(VirtualFrame frame, InternalMethod currentMethod,
            DynamicObject selfMetaClass) {
        final DeclarationContext context = RubyArguments.getDeclarationContext(frame);

        if (currentMethod.isRefined()) {
            // super from the refined method has access to the parent's active refinements for the selfMetaClass
            final DynamicObject[] classRefinements = currentMethod
                    .getActiveRefinements()
                    .getRefinementsFor(selfMetaClass);

            if (classRefinements == null) {
                return context;
            } else {
                // add to the context active refinements for the selfMetaClass
                final Map<DynamicObject, DynamicObject[]> newRefinements = new HashMap<>(context.getRefinements());
                newRefinements.put(selfMetaClass, classRefinements);

                return context.withRefinements(newRefinements);
            }
        } else {
            return context;
        }
    }
}
