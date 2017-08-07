/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher.options;

import org.graalvm.options.OptionType;

import java.util.Locale;

public abstract class EnumOptionDescription<T extends Enum<T>> extends OptionDescription<T> {

    private final T defaultValue;
    private final Class<T> type;
    private final OptionType<T> optionType;

    EnumOptionDescription(String name, String description, T defaultValue, Class<T> type) {
        super(name, description);
        this.defaultValue = defaultValue;
        this.type = type;
        this.optionType = new OptionType<>(type.getName(), defaultValue, this::stringConverter);
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T checkValue(Object value) {
        try {
            return checkValueInner(value);
        } catch (IllegalArgumentException e) {
            throw new OptionTypeException(getName(), value.toString());
        }
    }

    private T stringConverter(Object value) {
        T result = checkValueInner(value);
        if (result == null) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    protected T checkValueInner(Object value) {
        if (value instanceof String) {
            return Enum.valueOf(type, ((String) value).toUpperCase(Locale.ENGLISH));
        } else if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            return null;
        }
    }

    @Override
    protected OptionType<T> getOptionType() {
        return optionType;
    }

}
