/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.strings.AbstractTruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;

import java.util.Objects;

public final class RopeWithEncoding {

    private final Rope rope;
    private final RubyEncoding encoding;

    public RopeWithEncoding(Rope rope, RubyEncoding encoding) {
        assert rope.encoding == encoding.jcoding;
        this.rope = rope;
        this.encoding = encoding;
    }

    public Rope getRope() {
        return rope;
    }

    public RubyEncoding getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RopeWithEncoding)) {
            return false;
        }
        RopeWithEncoding that = (RopeWithEncoding) o;
        return rope.equals(that.rope) && encoding == that.encoding;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rope, encoding);
    }

}
