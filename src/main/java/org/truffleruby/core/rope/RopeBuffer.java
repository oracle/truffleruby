/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public class RopeBuffer extends LeafRope {

    private final RopeBuilder byteList;

    protected RopeBuffer(byte[] bytes, Encoding encoding, CodeRange codeRange, boolean singleByteOptimizable, int characterLength) {
        super(bytes, encoding, codeRange, singleByteOptimizable, characterLength);
        this.byteList = RopeBuilder.createRopeBuilder(bytes, encoding);
    }

    public RopeBuffer(Rope original) {
        this(original.getBytesCopy(),
                original.getEncoding(),
                original.getCodeRange(),
                original.isSingleByteOptimizable(),
                original.characterLength());
    }

    public RopeBuffer(RopeBuilder byteList, CodeRange codeRange, boolean singleByteOptimizable, int characterLength) {
        super(byteList.getUnsafeBytes(), byteList.getEncoding(), codeRange, singleByteOptimizable, characterLength);
        this.byteList =  byteList;
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        byteList.setEncoding(newEncoding);
        return this;
    }

    @Override
    public byte getByteSlow(int index) {
        return byteList.get(index);
    }

    public RopeBuilder getByteList() {
        return byteList;
    }

    @Override
    public String toString() {
        // This should be used for debugging only.
        return byteList.toString();
    }

    public RopeBuffer dup() {
        final RopeBuilder duped = new RopeBuilder();
        duped.append(byteList.getBytes());
        return new RopeBuffer(duped, getCodeRange(), isSingleByteOptimizable(), characterLength());
    }
}
