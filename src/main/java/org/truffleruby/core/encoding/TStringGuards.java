/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is modified from org.jruby.runtime.encoding.EncodingService,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.BROKEN;

public abstract class TStringGuards {

    public static boolean is7Bit(AbstractTruffleString tstring, RubyEncoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(tstring, encoding.tencoding) == ASCII;
    }

    public static boolean is7BitUncached(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return is7Bit(tstring, encoding, TruffleString.GetByteCodeRangeNode.getUncached());
    }

    public static boolean isBrokenCodeRange(AbstractTruffleString string, RubyEncoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(string, encoding.tencoding) == BROKEN;
    }

}
