/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.jcodings.Encoding;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

/** Take a Ruby object that has an encoding and extracts the Java-level encoding object. */
public abstract class ToEncodingNode extends RubyContextNode {

    public static ToEncodingNode create() {
        return ToEncodingNodeGen.create();
    }

    public abstract Encoding executeToEncoding(Object value);

    @Specialization
    protected Encoding stringToEncoding(RubyString value) {
        assert value.encoding.encoding == value.rope.encoding;
        return value.encoding.encoding;
    }

    @Specialization
    protected Encoding immutableStringToEncoding(ImmutableRubyString value) {
        return value.getEncoding(getContext()).encoding;
    }

    @Specialization
    protected Encoding symbolToEncoding(RubySymbol value) {
        return value.getRope().getEncoding();
    }

    @Specialization
    protected Encoding regexpToEncoding(RubyRegexp value) {
        return value.regex.getEncoding();
    }

    @Specialization
    protected Encoding rubyEncodingToEncoding(RubyEncoding value) {
        return value.encoding;
    }

    @Fallback
    protected Encoding failure(Object value) {
        return null;
    }
}
