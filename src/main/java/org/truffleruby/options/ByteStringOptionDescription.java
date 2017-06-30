/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.options;

import org.graalvm.options.OptionType;

import java.nio.charset.Charset;

public class ByteStringOptionDescription extends OptionDescription<byte[]> {

    private final byte[] defaultValue;

    public ByteStringOptionDescription(String name, String description, byte[] defaultValue) {
        super(name, description);
        this.defaultValue = defaultValue;
    }

    @Override
    public byte[] getDefaultValue() {
        return defaultValue;
    }

    @Override
    public byte[] checkValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return parseString((String) value);
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else {
            throw new OptionTypeException(getName(), value.toString());
        }
    }

    private static byte[] parseString(String text) {
        return text.getBytes(Charset.defaultCharset());
    }

    @Override
    public String toString(Object value) {
        if (value == null) {
            return "null";
        } else {
            return new String((byte[]) value, Charset.defaultCharset());
        }
    }

    private static final OptionType<byte[]> OPTION_TYPE = new OptionType<>("byte[]", new byte[]{}, ByteStringOptionDescription::parseString);

    @Override
    protected OptionType<byte[]> getOptionType() {
        return OPTION_TYPE;
    }

}
