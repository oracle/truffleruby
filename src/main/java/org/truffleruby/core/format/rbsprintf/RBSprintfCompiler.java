/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.rbsprintf;

import java.util.List;

import com.oracle.truffle.api.nodes.Node;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.format.FormatEncoding;
import org.truffleruby.core.format.FormatRootNode;
import org.truffleruby.core.format.rbsprintf.RBSprintfConfig.FormatArgumentType;
import org.truffleruby.core.rope.Rope;

import com.oracle.truffle.api.RootCallTarget;

public class RBSprintfCompiler {

    private final RubyLanguage language;
    private final Node currentNode;

    public RBSprintfCompiler(RubyLanguage language, Node currentNode) {
        this.language = language;
        this.currentNode = currentNode;
    }

    public RootCallTarget compile(Rope format, Object stringReader) {
        final RBSprintfSimpleParser parser = new RBSprintfSimpleParser(bytesToChars(format.getBytes()), false);
        final List<RBSprintfConfig> configs = parser.parse();
        final RBSprintfSimpleTreeBuilder builder = new RBSprintfSimpleTreeBuilder(configs, stringReader);

        return new FormatRootNode(
                language,
                currentNode.getEncapsulatingSourceSection(),
                FormatEncoding.find(format.getEncoding(), currentNode),
                builder.getNode()).getCallTarget();
    }

    private static int SIGN = 0x10;

    public Object typeList(Rope format, RubyContext context, RubyLanguage language) {
        final RBSprintfSimpleParser parser = new RBSprintfSimpleParser(bytesToChars(format.getBytes()), false);
        final List<RBSprintfConfig> configs = parser.parse();
        final int[] types = new int[3 * configs.size()]; // Ensure there is enough space for the argument types that might be in the format string.

        int pos = 0;
        int highWaterMark = -1;
        for (RBSprintfConfig config : configs) {
            final int typeInt;
            final int typePos;
            if (config.isLiteral()) {
                continue;
            }
            if (config.isPrecisionStar()) {
                types[pos] = FormatArgumentType.INT.ordinal();
                highWaterMark = Math.max(highWaterMark, pos);
                pos++;
            } else if (config.isPrecisionArg()) {
                types[config.getPrecision()] = FormatArgumentType.INT.ordinal();
                highWaterMark = Math.max(highWaterMark, config.getPrecision());
            }
            if (config.isWidthStar()) {
                types[pos] = FormatArgumentType.INT.ordinal();
                highWaterMark = Math.max(highWaterMark, pos);
                pos++;
            } else if (config.isArgWidth()) {
                types[config.getWidth()] = FormatArgumentType.INT.ordinal();
                highWaterMark = Math.max(highWaterMark, config.getWidth());
            }
            switch (config.getFormat()) {
                case 'd':
                case 'i':
                    typeInt = config.getFormatArgumentType().ordinal() | SIGN;
                    break;
                default:
                    typeInt = config.getFormatArgumentType().ordinal();
            }
            if (config.getAbsoluteArgumentIndex() != null) {
                typePos = config.getAbsoluteArgumentIndex() - 1; //Parameters are 1 indexed, but our array is 0 indexed.
            } else {
                typePos = pos++;
            }
            types[typePos] = typeInt;
            highWaterMark = Math.max(highWaterMark, typePos);
        }

        return new RubyArray(context.getCoreLibrary().arrayClass, language.arrayShape, types, highWaterMark + 1);
    }

    private static char[] bytesToChars(byte[] bytes) {
        final char[] chars = new char[bytes.length];

        for (int n = 0; n < bytes.length; n++) {
            chars[n] = (char) bytes[n];
        }

        return chars;
    }


}
