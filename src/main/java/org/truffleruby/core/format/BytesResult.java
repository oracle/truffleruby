/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format;

import org.truffleruby.extra.ffi.Pointer;

public class BytesResult {

    private final byte[] output;
    private final int outputLength;
    private final FormatEncoding encoding;

    private final Pointer[] associated;

    public BytesResult(
            byte[] output,
            int outputLength,
            FormatEncoding encoding,
            Pointer[] associated) {
        this.output = output;
        this.outputLength = outputLength;
        this.encoding = encoding;
        this.associated = associated;
    }

    public byte[] getOutput() {
        return output;
    }

    public int getOutputLength() {
        return outputLength;
    }

    public FormatEncoding getEncoding() {
        return encoding;
    }

    public Pointer[] getAssociated() {
        return associated;
    }

}
