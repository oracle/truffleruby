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

import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@ImportStatic(RubyGuards.class)
public abstract class ForeignToRubyNode extends RubyBaseNode {

    public static ForeignToRubyNode create() {
        return ForeignToRubyNodeGen.create();
    }

    public abstract Object executeConvert(Object value);

    @Specialization
    protected RubyString convertCharacterCached(char value,
            @Cached FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.executeFromJavaString(String.valueOf(value));
    }

    @Specialization
    protected RubyString convertStringCached(String value,
            @Cached FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.executeFromJavaString(value);
    }

    @Specialization(guards = { "!isCharacter(value)", "!isString(value)" })
    protected Object convert(Object value) {
        return value;
    }

}
