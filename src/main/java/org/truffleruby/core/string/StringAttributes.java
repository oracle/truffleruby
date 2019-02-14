/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.truffleruby.core.rope.CodeRange;

public final class StringAttributes {
    private final int characterLength;
    private final CodeRange codeRange;

    public StringAttributes(int characterLength, CodeRange codeRange) {
        this.characterLength = characterLength;
        this.codeRange = codeRange;
    }

    public int getCharacterLength() {
        return characterLength;
    }

    public CodeRange getCodeRange() {
        return codeRange;
    }
}
