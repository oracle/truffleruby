/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.options;

import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionValues;
import org.truffleruby.shared.options.OptionDescription;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.TruffleLanguage.Env;

import java.util.HashMap;
import java.util.Map;

public class OptionsBuilder {

    private final Map<OptionDescription<?>, Object> options = new HashMap<>();

    public void set(Map<String, Object> properties) {
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            set(property.getKey(), property.getValue());
        }
    }

    public void set(OptionValues optionValues) {
        for (OptionDescription<?> option : OptionsCatalog.allDescriptions()) {
            final OptionKey<?> key = optionValues.getDescriptors().get(option.getName()).getKey();

            if (optionValues.hasBeenSet(key)) {
                set(option.getName(), optionValues.get(key));
            }
        }
    }

    private void set(String name, Object value) {
        final OptionDescription<?> description = OptionsCatalog.fromName(name);

        if (description == null) {
            throw new UnknownOptionException(name);
        }

        options.put(description, description.checkValue(value));
    }

    public Options build(Env env) {
        return new Options(this, env);
    }

    <T> T getOrDefault(OptionDescription<T> description) {
        final T value = description.cast(options.get(description));

        if (value == null) {
            return description.getDefaultValue();
        } else {
            return value;
        }
    }

    <T> T getOrDefault(OptionDescription<T> description, T defaultValue) {
        final T value = description.cast(options.get(description));

        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
