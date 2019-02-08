/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionType;

public class IntegerOptionDescription extends OptionDescription<Integer> {

    private final int defaultValue;

    IntegerOptionDescription(OptionCategory category, String name, String description, String[] rubyOptions, int defaultValue) {
        super(category, name, description, rubyOptions);
        this.defaultValue = defaultValue;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Integer checkValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new OptionTypeException(getName(), value.toString());
            }
        } else {
            throw new OptionTypeException(getName(), value.toString());
        }
    }

    @Override
    protected OptionType<Integer> getOptionType() {
        return OptionType.defaultType(0);
    }

}
