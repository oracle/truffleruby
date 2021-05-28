/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public abstract class LeafRope extends ManagedRope {

    public LeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, int characterLength) {
        super(encoding, codeRange, bytes.length, characterLength, bytes);
    }

    @Override
    public byte getByteSlow(int index) {
        return getRawBytes()[index];
    }

}
