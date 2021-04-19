/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is modified from org.jruby.util.Sprintf,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 *
 * Contains code modified from Sprintf.java
 *
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 */
package org.truffleruby.core.format.format;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.core.format.FormatNode;

@NodeChild("width")
@NodeChild("precision")
@NodeChild("value")
public abstract class FormatStringNode extends FormatNode {

    @Specialization
    protected Object formatBytes(int width, int precision, byte[] value) {
        byte[] precisionValue;
        if (precision > 0) {
            precisionValue = new byte[precision];
        } else {
            precisionValue = value;
        }
        return precisionValue;
    }

    @Specialization
    protected Object formatObject(int width, int precision, Object value) {
        return value;
    }
}
