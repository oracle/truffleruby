/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.jcodings.Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.core.encoding.EncodingOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Take a Ruby object that has an encoding and extracts the Java-level encoding object.
 */
public abstract class ToEncodingNode extends RubyBaseNode {

    public static ToEncodingNode create() {
        return ToEncodingNodeGen.create();
    }

    public abstract Encoding executeToEncoding(Object value);

    @Specialization(guards = "isRubyString(value)")
    public Encoding stringToEncoding(DynamicObject value) {
        return StringOperations.encoding(value);
    }

    @Specialization(guards = "isRubySymbol(value)")
    public Encoding symbolToEncoding(DynamicObject value) {
        return Layouts.SYMBOL.getRope(value).getEncoding();
    }

    @Specialization(guards = "isRubyRegexp(value)")
    public Encoding regexpToEncoding(DynamicObject value) {
        return Layouts.REGEXP.getRegex(value).getEncoding();
    }

    @Specialization(guards = "isRubyEncoding(value)")
    public Encoding rubyEncodingToEncoding(DynamicObject value) {
        return EncodingOperations.getEncoding(value);
    }

    @Fallback
    public Encoding failure(Object value) {
        return null;
    }
}
