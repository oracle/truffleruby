/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public abstract class LazyRope extends ManagedRope {

    protected LazyRope(Encoding encoding, int byteLength, int characterLength) {
        super(encoding, CodeRange.CR_7BIT, byteLength, characterLength, 1, null);
    }

    @Override
    protected byte getByteSlow(int index) {
        return getBytes()[index];
    }

    protected abstract byte[] fulfill();

}
