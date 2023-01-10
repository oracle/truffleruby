/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.readline;

import org.graalvm.shadowed.org.jline.reader.impl.DefaultParser;

public class ParserWithCustomDelimiters extends DefaultParser {

    private char[] delimiters = {
            ' ',
            '\t',
            '\n',
            '\\',
            '\'',
            '"',
            '`',
            '@',
            '$',
            '>',
            '<',
            '=',
            ';',
            '|',
            '&',
            '{',
            '(' };

    public String getDelimiters() {
        return new String(delimiters);
    }

    public void setDelimiters(String delimiters) {
        this.delimiters = delimiters.toCharArray();
    }

    @Override
    public boolean isDelimiterChar(CharSequence buffer, int pos) {
        char c = buffer.charAt(pos);
        for (char delimiter : delimiters) {
            if (c == delimiter) {
                return true;
            }
        }
        return false;
    }
}
