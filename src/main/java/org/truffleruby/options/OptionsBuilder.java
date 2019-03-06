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

import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionValues;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.TruffleLanguage.Env;
import org.truffleruby.shared.options.RubyOptionTypes;

import java.util.HashMap;
import java.util.Map;

public class OptionsBuilder {

    private final Map<OptionDescriptor, Object> options = new HashMap<>();

    public void set(OptionValues optionValues) {
        for (OptionDescriptor descriptor : OptionsCatalog.allDescriptors()) {
            final OptionKey<?> key = optionValues.getDescriptors().get(descriptor.getName()).getKey();

            if (optionValues.hasBeenSet(key)) {
                set(descriptor.getName(), optionValues.get(key));
            }
        }
    }

    private void set(String name, Object value) {
        final OptionDescriptor descriptor = OptionsCatalog.fromName(name);

        if (descriptor == null) {
            throw new UnknownOptionException(name);
        }

        options.put(descriptor, RubyOptionTypes.parseValue(descriptor, value));
    }

    public Options build(Env env) {
        return new Options(this, env);
    }

    <T> T getOrDefault(OptionDescriptor descriptor) {
        final T value = cast(options.get(descriptor));

        if (value == null) {
            return cast(descriptor.getKey().getDefaultValue());
        } else {
            return value;
        }
    }

    <T> T getOrDefault(OptionDescriptor descriptor, T defaultValue) {
        final T value = cast(options.get(descriptor));

        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

}
