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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringArrayOptionDescription extends AppendableOptionDescription<String[]> {

    private final String[] defaultValue;

    StringArrayOptionDescription(OptionCategory category, String name, String description, String[] rubyOptions, String[] defaultValue) {
        super(category, name, description, rubyOptions);
        this.defaultValue = defaultValue;
    }

    @Override
    public String[] getDefaultValue() {
        return defaultValue.clone();
    }

    @Override
    public String[] checkValue(Object value) {
        if (value == null) {
            return new String[]{};
        } else if (value instanceof String[]) {
            return (String[]) value;
        } else if (value instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) value;
            final String[] strings = new String[collection.size()];
            int n = 0;
            for (Object item : collection) {
                strings[n] = item.toString();
                n++;
            }
            return strings;
        } else if (value instanceof String) {
            return parseStringArray((String) value);
        } else {
            throw new OptionTypeException(getName(), value.toString());
        }
    }

    // Allows input such as foo,bar,baz. You can escape commas.

    private String[] parseStringArray(String string) {
        try {
            return parseStringArrayInner(string);
        } catch (IllegalStateException e) {
            throw new OptionTypeException(getName(), string);
        }
    }

    @SuppressWarnings("fallthrough")
    private static String[] parseStringArrayInner(String string) {
        final List<String> values = new ArrayList<>();

        final int startOfString = 0;
        final int withinString = 1;
        final int escape = 2;

        int state = startOfString;

        final StringBuilder builder = new StringBuilder();

        for (int n = 0; n < string.length(); n++) {
            switch (state) {
                case startOfString:
                    builder.setLength(0);
                    state = withinString;
                    // continue with next case
                case withinString:
                    switch (string.charAt(n)) {
                        case ',':
                            values.add(builder.toString());
                            state = startOfString;
                            break;

                        case '\\':
                            state = escape;
                            break;

                        default:
                            builder.append(string.charAt(n));
                            break;
                    }
                    break;

                case escape:
                    state = withinString;
                    if (string.charAt(n) == ',') {
                        builder.append(string.charAt(n));
                    } else {
                        builder.append('\\');
                        builder.append(string.charAt(n));
                    }
                    break;
            }
        }

        switch (state) {
            case withinString:
                values.add(builder.toString());
                break;

            case escape:
                throw new IllegalArgumentException();
        }

        return values.toArray(new String[values.size()]);
    }

    @Override
    public String valueToString(Object value) {
        String[] strings = (String[]) value;
        String[] escapedValues = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            escapedValues[i] = escape(strings[i]);
        }
        return String.join(",", escapedValues);
    }

    @Override
    String append(String currentValues, String newElement) {
        if (currentValues.isEmpty()) {
            return escape(newElement);
        } else {
            return currentValues + ',' + escape(newElement);
        }
    }

    private String escape(String string) {
        return string.
                // , -> \,
                replaceAll(",", "\\\\,");
    }

    private static final OptionType<String[]> OPTION_TYPE = new OptionType<>("String[]", new String[]{}, StringArrayOptionDescription::parseStringArrayInner);

    @Override
    protected OptionType<String[]> getOptionType() {
        return OPTION_TYPE;
    }

}
