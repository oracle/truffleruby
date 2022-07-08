/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

/** Determines if an object is immutable for Kernel#clone, Kernel#dup, and rb_obj_clone. */
@GenerateUncached
public abstract class IsImmutableObjectNode extends RubyBaseNode {

    public abstract boolean execute(Object object);

    @Specialization
    protected boolean isImmutableObject(boolean object) {
        return true;
    }

    @Specialization
    protected boolean isImmutableObject(int object) {
        return true;
    }

    @Specialization
    protected boolean isImmutableObject(long object) {
        return true;
    }

    @Specialization
    protected boolean isImmutableObject(double object) {
        return true;
    }

    @Specialization
    protected boolean isImmutableObject(ImmutableRubyObject object) {
        return true;
    }

    @Specialization
    protected boolean isImmutableObject(RubyDynamicObject object,
            @Cached LogicalClassNode logicalClassNode) {
        final RubyClass logicalClass = logicalClassNode.execute(object);
        return logicalClass == coreLibrary().rationalClass || logicalClass == coreLibrary().complexClass;
    }

}
