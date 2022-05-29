/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */


package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public abstract class RopeGuards {

    public static boolean isBinaryString(Encoding encoding) {
        return encoding == ASCIIEncoding.INSTANCE;
    }

    public static boolean isAsciiCompatible(Encoding encoding) {
        return encoding.isAsciiCompatible();
    }

}
