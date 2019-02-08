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

public class StringOptionDescription extends AppendableOptionDescription<String> {

    private final String defaultValue;

    StringOptionDescription(OptionCategory category, String name, String description, String[] rubyOptions, String defaultValue) {
        super(category, name, description, rubyOptions);
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String checkValue(Object value) {
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    @Override
    protected OptionType<String> getOptionType() {
        return OptionType.defaultType("");
    }

    @Override
    String append(String currentValues, String newElement) {
        return currentValues + newElement;
    }
}
