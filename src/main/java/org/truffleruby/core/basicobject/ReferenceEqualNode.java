/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.basicobject;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;

@GenerateUncached
public abstract class ReferenceEqualNode extends RubyBaseNode {

    public abstract boolean execute(Object a, Object b);

    @Specialization
    protected boolean equal(boolean a, boolean b) {
        return a == b;
    }

    @Specialization
    protected boolean equal(int a, int b) {
        return a == b;
    }

    @Specialization
    protected boolean equal(long a, long b) {
        return a == b;
    }

    @Specialization
    protected boolean equal(double a, double b) {
        return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
    }

    @Specialization(guards = { "isNonPrimitiveRubyObject(a)", "isNonPrimitiveRubyObject(b)" })
    protected boolean equalRubyObjects(Object a, Object b) {
        return a == b;
    }

    @Specialization(guards = { "isNonPrimitiveRubyObject(a)", "isPrimitive(b)" })
    protected boolean rubyObjectPrimitive(Object a, Object b) {
        return false;
    }

    @Specialization(guards = { "isPrimitive(a)", "isNonPrimitiveRubyObject(b)" })
    protected boolean primitiveRubyObject(Object a, Object b) {
        return false;
    }

    @Specialization(guards = { "isPrimitive(a)", "isPrimitive(b)", "!comparablePrimitives(a, b)" })
    protected boolean nonComparablePrimitives(Object a, Object b) {
        return false;
    }

    @Specialization(guards = "isForeignObject(a) || isForeignObject(b)", limit = "getInteropCacheLimit()")
    protected boolean equalForeign(Object a, Object b,
            @CachedLibrary("a") InteropLibrary lhsInterop,
            @CachedLibrary("b") InteropLibrary rhsInterop) {
        if (lhsInterop.hasIdentity(a)) {
            return lhsInterop.isIdentical(a, b, rhsInterop);
        } else {
            return a == b;
        }
    }

    protected static boolean comparablePrimitives(Object a, Object b) {
        return (a instanceof Boolean && b instanceof Boolean) ||
                (RubyGuards.isImplicitLong(a) && RubyGuards.isImplicitLong(b)) ||
                (RubyGuards.isDouble(a) && RubyGuards.isDouble(b));
    }


    protected static boolean isNonPrimitiveRubyObject(Object object) {
        return object instanceof RubyDynamicObject || object instanceof ImmutableRubyObject;
    }
}
