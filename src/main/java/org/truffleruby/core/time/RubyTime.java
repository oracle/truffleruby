/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.time;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;

import java.time.ZonedDateTime;

public class RubyTime extends RubyDynamicObject {

    ZonedDateTime dateTime;
    Object offset;
    Object zone;
    boolean relativeOffset;
    boolean isUtc;

    public RubyTime(
            RubyClass rubyClass,
            Shape shape,
            ZonedDateTime dateTime,
            Object zone,
            Object offset,
            boolean relativeOffset,
            boolean isUtc) {
        super(rubyClass, shape);
        assert zone instanceof RubyString || zone == Nil.INSTANCE;
        this.dateTime = dateTime;
        this.offset = offset;
        this.zone = zone;
        this.relativeOffset = relativeOffset;
        this.isUtc = isUtc;
    }

}
