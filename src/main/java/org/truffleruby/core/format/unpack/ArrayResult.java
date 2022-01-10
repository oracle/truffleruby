/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.unpack;

public class ArrayResult {

    private final Object output;
    private final int outputLength;

    public ArrayResult(Object output, int outputLength) {
        this.output = output;
        this.outputLength = outputLength;
    }

    public Object getOutput() {
        return output;
    }

    public int getOutputLength() {
        return outputLength;
    }

}
