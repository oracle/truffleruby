/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EmbeddedScript {

    private final RubyContext context;

    public EmbeddedScript(RubyContext context) {
        this.context = context;
    }

    public boolean shouldTransform(byte[] sourceBytes) {
        return isShebang(sourceBytes) || context.getOptions().IGNORE_LINES_BEFORE_RUBY_SHEBANG;
    }

    private boolean isShebang(byte[] bytes) {
        return bytes.length >= 2 && bytes[0] == '#' && bytes[1] == '!';
    }

    public byte[] transformForExecution(RubyNode currentNode, byte[] sourceBytes, String path) throws IOException {
        final ByteArrayOutputStream transformed = new ByteArrayOutputStream();

        int n = 0;

        while (true) {
            if (n == sourceBytes.length) {
                throw new RaiseException(context,
                        context.getCoreExceptions().loadError("no Ruby script found in input", path, currentNode));
            }

            final int startOfLine = n;

            while (n < sourceBytes.length && sourceBytes[n] != '\n') {
                n++;
            }

            if (n < sourceBytes.length && sourceBytes[n] == '\n') {
                n++;
            }

            final byte[] lineBytes = Arrays.copyOfRange(sourceBytes, startOfLine, n);
            final String line = new String(lineBytes, StandardCharsets.US_ASCII);

            final boolean rubyShebang = line.startsWith("#!") && line.contains("ruby");
            if (rubyShebang) {
                transformed.write(lineBytes);
                break;
            } else {
                transformed.write("# line ignored by Ruby:".getBytes(StandardCharsets.US_ASCII)); // prefix with a comment so it's ignored by parser
                transformed.write(lineBytes);
            }
        }

        transformed.write(sourceBytes, n, sourceBytes.length - n);

        return transformed.toByteArray();
    }

}
