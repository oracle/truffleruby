/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.language.RubyBaseNode;

@ImportStatic(StringCachingGuards.class)
public abstract class RubyToForeignNode extends RubyBaseNode {

    public static RubyToForeignNode create() {
        return RubyToForeignNodeGen.create();
    }

    public abstract Object executeConvert(Object value);

    @Specialization(guards = "isRubySymbol(value) || isRubyString(value)")
    public String convertString(DynamicObject value,
            @Cached("create()") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Specialization(guards = { "!isRubyString(value)", "!isRubySymbol(value)" })
    public Object noConversion(Object value) {
        return value;
    }

}
