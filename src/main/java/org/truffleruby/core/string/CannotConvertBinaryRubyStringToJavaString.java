/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

public class CannotConvertBinaryRubyStringToJavaString extends RuntimeException {

    private static final long serialVersionUID = 3484665101786967474L;

    private final int nonAsciiCharacter;

    public CannotConvertBinaryRubyStringToJavaString(int nonAsciiCharacter) {
        super(
                "Cannot convert a Ruby String with BINARY encoding containing non-US-ASCII character " +
                        nonAsciiCharacter + " to a Java String");
        this.nonAsciiCharacter = nonAsciiCharacter;
    }

    public int getNonAsciiCharacter() {
        return nonAsciiCharacter;
    }

}
