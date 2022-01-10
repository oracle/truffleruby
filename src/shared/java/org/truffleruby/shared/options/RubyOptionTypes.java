/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;


public class RubyOptionTypes {

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
