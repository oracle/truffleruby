/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jruby.truffle.collections.ByteArrayBuilder;

import static org.jruby.truffle.core.rope.CodeRange.CR_UNKNOWN;

public class RopeBuilder extends ByteArrayBuilder {

    private Encoding encoding = ASCIIEncoding.INSTANCE;

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void append(Rope other) {
        append(other.getBytes());
    }

    public Rope toRope() {
        return RopeOperations.create(getBytes(), encoding, CR_UNKNOWN);
    }

}
