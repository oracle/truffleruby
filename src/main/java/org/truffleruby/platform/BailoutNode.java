/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.platform;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyNode;

@NodeChild
public abstract class BailoutNode extends RubyNode {

    @Specialization(guards = "isRubyString(message)")
    protected DynamicObject bailout(DynamicObject message,
            @Cached("create()") ToJavaStringNode toJavaStringNode) {
        CompilerDirectives.bailout(toJavaStringNode.executeToJavaString(message));
        return nil();
    }

    public static BailoutNode create(RubyNode messageNode) {
        return BailoutNodeGen.create(messageNode);
    }

}
