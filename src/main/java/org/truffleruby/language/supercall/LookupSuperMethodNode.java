/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.MetaClassNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Caches {@link ModuleOperations#lookupSuperMethod} on an actual instance. */
public abstract class LookupSuperMethodNode extends RubyBaseNode {

    public abstract InternalMethod executeLookupSuperMethod(VirtualFrame frame, Object self);

    // The check for same metaClass is overly restrictive,
    // but seems to be the only reasonable check in term of performance.
    // The ideal condition would be to check if both ancestor lists starting at
    // the current method's module are identical, which is non-trivial
    // if the current method's module is an (included) module and not a class.

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "getCurrentMethod(frame) == currentMethod",
                    "metaClassNode.execute(this, self) == selfMetaClass" },
            assumptions = "superMethod.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupSuperMethodCached(VirtualFrame frame, Object self,
            @Cached("getCurrentMethod(frame)") InternalMethod currentMethod,
            @Cached @Shared MetaClassNode metaClassNode,
            @Cached("metaClassNode.execute(this, self)") RubyClass selfMetaClass,
            @Cached("doLookup(currentMethod, selfMetaClass)") MethodLookupResult superMethod) {
        return superMethod.getMethod();
    }

    @Specialization
    protected InternalMethod lookupSuperMethodUncached(VirtualFrame frame, Object self,
            @Cached @Shared MetaClassNode metaClassNode) {
        final InternalMethod currentMethod = getCurrentMethod(frame);
        final RubyClass selfMetaClass = metaClassNode.execute(this, self);
        return doLookup(currentMethod, selfMetaClass).getMethod();
    }

    protected InternalMethod getCurrentMethod(VirtualFrame frame) {
        return RubyArguments.getMethod(frame);
    }


    @TruffleBoundary
    protected MethodLookupResult doLookup(InternalMethod currentMethod, RubyClass selfMetaClass) {
        MethodLookupResult superMethod = ModuleOperations.lookupSuperMethod(currentMethod, selfMetaClass);
        // TODO (eregon, 12 June 2015): Is this correct?
        if (!superMethod.isDefined()) {
            return superMethod.withNoMethod();
        }
        return superMethod;
    }

    protected int getCacheLimit() {
        return getLanguage().options.METHOD_LOOKUP_CACHE;
    }
}
