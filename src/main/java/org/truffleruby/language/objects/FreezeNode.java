/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.core.range.RubyObjectRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import static org.truffleruby.Layouts.FROZEN_FLAG;

// Specializations are order by their frequency on railsbench using --engine.SpecializationStatistics
@GenerateUncached
public abstract class FreezeNode extends RubyBaseNode {

    public abstract Object execute(Object object);

    @Specialization
    protected Object freezeRubyString(RubyString object) {
        return object.frozen = true;
    }

    @Specialization(guards = { "!isRubyObjectRange(object)", "isNotRubyString(object)" },
            limit = "getDynamicObjectCacheLimit()")
    protected Object freeze(RubyDynamicObject object,
            @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
        if (objectLibrary.isShared(object)) {
            synchronized (object) {
                objectLibrary.setShapeFlags(object, objectLibrary.getShapeFlags(object) | FROZEN_FLAG);
            }
        } else {
            objectLibrary.setShapeFlags(object, objectLibrary.getShapeFlags(object) | FROZEN_FLAG);
        }

        return object;
    }

    @Specialization
    protected Object freezeRubyObjectRange(RubyObjectRange object) {
        return object.frozen = true;
    }

    @Specialization
    protected Object freeze(ImmutableRubyObject object) {
        return object;
    }

    @Specialization
    protected Object freeze(boolean object) {
        return object;
    }

    @Specialization
    protected Object freeze(int object) {
        return object;
    }

    @Specialization
    protected Object freeze(long object) {
        return object;
    }

    @Specialization
    protected Object freeze(double object) {
        return object;
    }


}
