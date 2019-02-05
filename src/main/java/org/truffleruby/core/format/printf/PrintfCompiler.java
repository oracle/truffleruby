/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.printf;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import org.truffleruby.RubyContext;
import org.truffleruby.core.format.FormatEncoding;
import org.truffleruby.core.format.FormatRootNode;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyNode;

import java.util.List;

public class PrintfCompiler {

    private final RubyContext context;
    private final RubyNode currentNode;

    public PrintfCompiler(RubyContext context, RubyNode currentNode) {
        this.context = context;
        this.currentNode = currentNode;
    }

    public RootCallTarget compile(Rope format, Object[] arguments, boolean isDebug) {
        final PrintfSimpleParser parser = new PrintfSimpleParser(bytesToChars(format.getBytes()), arguments, isDebug);
        final List<SprintfConfig> configs = parser.parse();
        final PrintfSimpleTreeBuilder builder = new PrintfSimpleTreeBuilder(context, configs);

        return Truffle.getRuntime().createCallTarget(
            new FormatRootNode(context, currentNode.getEncapsulatingSourceSection(),
                FormatEncoding.find(format.getEncoding()), builder.getNode()));
    }

    private static char[] bytesToChars(byte[] bytes) {
        final char[] chars = new char[bytes.length];

        for (int n = 0; n < bytes.length; n++) {
            chars[n] = (char) bytes[n];
        }

        return chars;
    }

}
