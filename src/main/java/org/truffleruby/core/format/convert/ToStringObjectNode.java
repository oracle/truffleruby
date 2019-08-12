/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.IsTaintedNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild("value")
public abstract class ToStringObjectNode extends FormatNode {

    public abstract Object executeToStringObject(VirtualFrame frame, Object object);

    @Specialization(guards = "isNil(nil)")
    public DynamicObject toStringString(DynamicObject nil) {
        return nil();
    }

    @Specialization(guards = "isRubyString(string)")
    public Object toStringString(VirtualFrame frame, DynamicObject string,
            @Cached IsTaintedNode isTaintedNode,
            @Cached("createBinaryProfile()") ConditionProfile taintedProfile) {
        if (taintedProfile.profile(isTaintedNode.executeIsTainted(string))) {
            setTainted(frame);
        }

        return string;
    }

    @Specialization(guards = "!isRubyString(object)")
    public Object toString(VirtualFrame frame, Object object,
            @Cached("createBinaryProfile()") ConditionProfile notStringProfile,
            @Cached ToStrNode toStrNode) {
        final Object value = toStrNode.executeToStr(frame, object);

        if (notStringProfile.profile(!RubyGuards.isRubyString(value))) {
            throw new NoImplicitConversionException(object, "String");
        }

        return executeToStringObject(frame, value);
    }

}
