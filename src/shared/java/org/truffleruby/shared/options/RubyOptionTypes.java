/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import java.util.Collection;

import org.graalvm.options.OptionDescriptor;

public class RubyOptionTypes {

    @SuppressWarnings("unchecked")
    public static <T> T parseValue(OptionDescriptor descriptor, Object value) {
        if (value == null) {
            return (T) descriptor.getKey().getDefaultValue();
        }

        if (value instanceof String) {
            try {
                return (T) descriptor.getKey().getType().convert((String) value);
            } catch (IllegalArgumentException e) {
                throw new OptionTypeException(descriptor.getName(), (String) value);
            }
        } else if (value instanceof Boolean) {
            return (T) value;
        } else if (value instanceof Integer) {
            return (T) value;
        } else if (value instanceof Enum<?>) {
            return (T) value;
        } else if (value instanceof String[]) {
            return (T) value;
        } else if (value instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) value;
            final String[] strings = new String[collection.size()];
            int n = 0;
            for (Object item : collection) {
                strings[n] = item.toString();
                n++;
            }
            return (T) strings;
        } else {
            throw new OptionTypeException(descriptor.getName(), value.toString());
        }
    }

    public static String valueToString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String[]) {
            String[] strings = (String[]) value;
            String[] escapedValues = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                escapedValues[i] = StringArrayOptionType.escape(strings[i]);
            }
            return String.join(",", escapedValues);
        } else {
            return value.toString();
        }
    }

}
