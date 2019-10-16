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

import java.util.ArrayList;
import java.util.List;

import org.graalvm.options.OptionType;

public class StringArrayOptionType {

    public static final OptionType<String[]> INSTANCE = new OptionType<>(
            "String[]",
            StringArrayOptionType::parseStringArray);

    @SuppressWarnings("fallthrough")
    private static String[] parseStringArray(String string) {
        final List<String> values = new ArrayList<>();

        final int STRING_OF_STRING = 0;
        final int WITHIN_STRING = 1;
        final int ESCAPE = 2;

        int state = STRING_OF_STRING;

        final StringBuilder builder = new StringBuilder();

        for (int n = 0; n < string.length(); n++) {
            switch (state) {
                case STRING_OF_STRING:
                    builder.setLength(0);
                    state = WITHIN_STRING;
                    // continue with next case

                case WITHIN_STRING:
                    switch (string.charAt(n)) {
                        case ',':
                            values.add(builder.toString());
                            state = STRING_OF_STRING;
                            break;
                        case '\\':
                            state = ESCAPE;
                            break;
                        default:
                            builder.append(string.charAt(n));
                            break;
                    }
                    break;

                case ESCAPE:
                    state = WITHIN_STRING;
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
            case WITHIN_STRING:
                values.add(builder.toString());
                break;
            case ESCAPE:
                throw new IllegalArgumentException();
        }

        return values.toArray(new String[values.size()]);
    }

    public static String append(String currentValues, String newElement) {
        final String escaped = escape(newElement);

        if (currentValues.isEmpty()) {
            return escaped;
        } else {
            return currentValues + ',' + escaped;
        }
    }

    public static String escape(String string) {
        return string.replaceAll(",", "\\\\,");
    }

}
