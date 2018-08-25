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
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.language.RubyBaseNode;

public abstract class ForeignToRubyNode extends RubyBaseNode {

    public static ForeignToRubyNode create() {
        return ForeignToRubyNodeGen.create();
    }

    protected abstract Object executeConvert(Object value);

    @Specialization
    public DynamicObject convertCharacterCached(char value,
            @Cached("create()") FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.executeFromJavaString(String.valueOf(value));
    }

    @Specialization
    public DynamicObject convertStringCached(String value,
            @Cached("create()") FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.executeFromJavaString(value);
    }

    @Specialization(guards = { "!isCharacter(value)", "!isString(value)" })
    public Object convert(Object value) {
        return value;
    }

}
