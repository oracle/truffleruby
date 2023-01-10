/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.library.RubyStringLibrary;

@NodeChild("value")
public abstract class ToStringObjectNode extends FormatNode {

    public abstract Object executeToStringObject(VirtualFrame frame, Object object);

    @Specialization
    protected Object toStringString(Nil nil) {
        return nil;
    }

    @Specialization(guards = "strings.isRubyString(string)", limit = "1")
    protected Object toStringString(Object string,
            @Cached RubyStringLibrary strings) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected Object toString(VirtualFrame frame, Object object,
            @Cached ConditionProfile notStringProfile,
            @Cached ToStrNode toStrNode) {
        final Object value = toStrNode.execute(object);

        if (notStringProfile.profile(RubyGuards.isNotRubyString(value))) {
            throw new NoImplicitConversionException(object, "String");
        }

        return executeToStringObject(frame, value);
    }

}
