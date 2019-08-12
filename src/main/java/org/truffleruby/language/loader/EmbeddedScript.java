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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

/*
 * An embedded script is one with some other language first, and then a Ruby shebang, and then a Ruby program. They're
 * run with the -x option, or automatically if a file contains a shebang line. This class detects embedded scripts, and
 * transforms them so they're valid Ruby programs which we can execute.
 */
public class EmbeddedScript {

    private final RubyContext context;

    private static final byte[] PREFIX_COMMENT = "# line ignored by Ruby: ".getBytes(StandardCharsets.US_ASCII);

    public EmbeddedScript(RubyContext context) {
        this.context = context;
    }

    public boolean shouldTransform(byte[] sourceBytes) {
        return isShebangLine(sourceBytes) || context.getOptions().IGNORE_LINES_BEFORE_RUBY_SHEBANG;
    }

    public byte[] transformForExecution(RubyNode currentNode, byte[] sourceBytes, String path) throws IOException {
        // Guess the transformed output will be no more than twice as long as the input
        final ByteArrayOutputStream transformed = new ByteArrayOutputStream(sourceBytes.length * 2);

        int n = 0;

        // We're going to iterate over lines, but without encoding so at the byte level

        while (true) {
            // If we reached the end of the script before finding a Ruby shebang that's an error

            if (n == sourceBytes.length) {
                throw new RaiseException(context,
                        context.getCoreExceptions().loadError("no Ruby script found in input", path, currentNode));
            }

            // Read a line

            final int lineStart = n;

            while (n < sourceBytes.length && sourceBytes[n] != '\n') {
                n++;
            }

            // Include the line terminator if there is one - there might not be at the end of the file

            if (n < sourceBytes.length && sourceBytes[n] == '\n') {
                n++;
            }

            final int lineLength = n - lineStart;

            // Until the Ruby shebang, lines are commented out so they aren't executed

            final boolean rubyShebangLine = isRubyShebangLine(sourceBytes, lineStart, lineLength);

            if (!rubyShebangLine) {
                transformed.write(PREFIX_COMMENT);
            }

            transformed.write(sourceBytes, lineStart, lineLength);

            // We stop iterating after the Ruby shebang

            if (rubyShebangLine) {
                break;
            }
        }

        // Just copy the rest

        transformed.write(sourceBytes, n, sourceBytes.length - n);

        return transformed.toByteArray();
    }

    private boolean isShebangLine(byte[] bytes) {
        return isShebangLine(bytes, 0);
    }

    private boolean isRubyShebangLine(byte[] bytes, int lineStart, int lineLength) {
        return isShebangLine(bytes, lineStart) && lineContainsRuby(bytes, lineStart, lineLength);
    }

    private boolean isShebangLine(byte[] bytes, int lineStart) {
        return bytes.length - lineStart >= 2 && bytes[lineStart] == '#' && bytes[lineStart + 1] == '!';
    }

    static boolean lineContainsRuby(byte[] bytes, int lineStart, int lineLength) {
        if (lineLength < 4) {
            return false;
        }

        // Don't bother looking for 'ruby' with just three characters to go
        final int limit = lineStart + lineLength - 3;

        for (int n = lineStart; n < limit; n++) {
            if (bytes[n] == 'r' && bytes[n + 1] == 'u' && bytes[n + 2] == 'b' && bytes[n + 3] == 'y') {
                return true;
            }
        }

        return false;
    }

}
