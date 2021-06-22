/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
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

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.ISO8859_1Encoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;

import java.nio.charset.StandardCharsets;

/** Like {@link StandardCharsets} but for JCoding Encoding's */
public abstract class StandardEncodings {

    public static final Encoding US_ASCII = USASCIIEncoding.INSTANCE;
    public static final Encoding ISO_8859_1 = ISO8859_1Encoding.INSTANCE;
    public static final Encoding UTF_8 = UTF8Encoding.INSTANCE;
    public static final Encoding BINARY = ASCIIEncoding.INSTANCE;

}
