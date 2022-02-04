/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@ImportStatic(StringCachingGuards.class)
public abstract class RubyToForeignNode extends RubyBaseNode {

    public static RubyToForeignNode create() {
        return RubyToForeignNodeGen.create();
    }

    public abstract Object executeConvert(Object value);

    @Specialization(guards = "isRubySymbolOrString(value)")
    protected String convertString(Object value,
            @Cached ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Fallback
    protected Object noConversion(Object value) {
        return value;
    }

}
