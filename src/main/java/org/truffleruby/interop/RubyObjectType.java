/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.Layouts;
import org.truffleruby.core.string.StringUtils;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;

public class RubyObjectType extends ObjectType {

    @Override
    @TruffleBoundary
    public String toString(DynamicObject object) {
        final String className = Layouts.BASIC_OBJECT.getLogicalClass(object).fields.getName();
        return StringUtils.format("DynamicObject@%x<%s>", System.identityHashCode(object), className);
    }

}
