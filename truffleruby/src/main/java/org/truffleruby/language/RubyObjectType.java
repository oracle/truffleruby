/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.RubyMessageResolutionAccessor;
import org.truffleruby.language.objects.shared.SharedObjects;

public class RubyObjectType extends ObjectType {

    @Override
    @TruffleBoundary
    public String toString(DynamicObject object) {
        if (RubyGuards.isRubyString(object)) {
            return RopeOperations.decodeRope(StringOperations.rope(object));
        } else if (RubyGuards.isRubySymbol(object)) {
            return Layouts.SYMBOL.getString(object);
        } else if (RubyGuards.isRubyException(object)) {
            return Layouts.EXCEPTION.getMessage(object).toString();
        } else if (RubyGuards.isRubyModule(object)) {
            return Layouts.MODULE.getFields(object).getName();
        } else {
            final String className = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(object)).getName();
            final Object isShared = SharedObjects.isShared(RubyContext.getInstance(), object) ? "(shared)" : "";
            return StringUtils.format("DynamicObject@%x<%s>%s", System.identityHashCode(object), className, isShared);
        }
    }

    @Override
    public ForeignAccess getForeignAccessFactory(DynamicObject object) {
        return RubyMessageResolutionAccessor.ACCESS;
    }

}
