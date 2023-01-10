/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser.parser;

import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;

public class ParserRopeOperations {

    private final TruffleString.Encoding tencoding;

    public ParserRopeOperations(RubyEncoding encoding) {
        tencoding = encoding.tencoding;
    }

    public TruffleString makeShared(AbstractTruffleString rope, int sharedStart, int sharedLength) {
        return rope.substringByteIndexUncached(sharedStart, sharedLength, tencoding, true);
    }

}
