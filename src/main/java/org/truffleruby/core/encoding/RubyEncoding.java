/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.encoding;

import org.jcodings.Encoding;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.object.Shape;

public class RubyEncoding extends RubyDynamicObject {

    public final Encoding encoding;
    public final RubyString name;

    public RubyEncoding(Shape shape, Encoding encoding, RubyString name) {
        super(shape);
        this.encoding = encoding;
        this.name = name;
    }

}
