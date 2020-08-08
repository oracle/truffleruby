/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(RubyLibrary.class)
public class RubyBignum extends RubyDynamicObject {

    public final BigInteger value;

    public RubyBignum(Shape shape, BigInteger value) {
        super(shape);
        this.value = value;
    }

    @ExportMessage
    protected void freeze() {
    }

}
