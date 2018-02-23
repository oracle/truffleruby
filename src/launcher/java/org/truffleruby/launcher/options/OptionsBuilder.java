/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher.options;

import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionValues;
import org.truffleruby.launcher.RubyLauncher;
import org.truffleruby.launcher.RubyLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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

    public Options build() {
        final Options options = new Options(this);

        if (options.OPTIONS_LOG && RubyLogger.LOGGER.isLoggable(Level.CONFIG)) {
            for (OptionDescription<?> option : OptionsCatalog.allDescriptions()) {
                assert option.getName().startsWith(RubyLauncher.LANGUAGE_ID);
                final String xName = option.getName().substring(RubyLauncher.LANGUAGE_ID.length() + 1);
                RubyLogger.LOGGER.config("option " + xName + "=" + option.valueToString(options.fromDescription(option)));
            }
        }

        return options;
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
