/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "value", type = RubyNode.class)
public abstract class ForeignToRubyNode extends RubyNode {

    public static ForeignToRubyNode create() {
        return ForeignToRubyNodeGen.create(null);
    }

    public abstract Object executeConvert(VirtualFrame frame, Object value);

    @Specialization
    public DynamicObject convertStringCached(VirtualFrame frame, String value,
                                             @Cached("create()") JavaStringToRubyStringNode javaStringToRubyStringNode) {
        return javaStringToRubyStringNode.executeConvert(frame, value);
    }

    @Specialization(guards = "!isString(value)")
    public Object convert(Object value) {
        return value;
    }

}
