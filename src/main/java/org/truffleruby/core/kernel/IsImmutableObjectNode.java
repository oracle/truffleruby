/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.LogicalClassNode;

/** Determines if an object is immutable for Kernel#clone, Kernel#dup, and rb_obj_clone. */
public abstract class IsImmutableObjectNode extends RubyContextNode {

    @Child private LogicalClassNode logicalClassNode;

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
    protected boolean isImmutableNilObject(Nil nil) {
        return true;
    }

    @Specialization(guards = "isRubyBignum(object)")
    protected boolean isImmutableBignumObject(DynamicObject object) {
        return true;
    }

    @Specialization(guards = "isRubySymbol(symbol)")
    protected boolean isImmutableSymbolObject(DynamicObject symbol) {
        return true;
    }

    @Specialization(guards = { "!isNil(object)", "!isRubyBignum(object)", "!isRubySymbol(object)" })
    protected boolean isImmutableObject(DynamicObject object) {
        final DynamicObject logicalClass = getLogicalClass(object);
        return logicalClass == coreLibrary().rationalClass || logicalClass == coreLibrary().complexClass;
    }

    private DynamicObject getLogicalClass(Object object) {
        if (logicalClassNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            logicalClassNode = insert(LogicalClassNode.create());
        }

        return logicalClassNode.executeLogicalClass(object);
    }


}
