/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.readline;

import java.io.OutputStream;

import org.graalvm.shadowed.org.jline.utils.NonBlockingInputStream;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.adapters.InputStreamAdapter;
import org.truffleruby.core.adapters.OutputStreamAdapter;
import org.truffleruby.core.support.RubyIO;

/** A simple file descriptor -> IO stream class.
 *
 * STDIO FDs will map to the polyglot STDIO streams. For all other IO, a valid Ruby IO object must be provided. */
public class IoStream {

    private final RubyContext context;
    private final RubyLanguage language;
    private final int fd;
    private final RubyIO io;
    private NonBlockingInputStream in;
    private OutputStream out;

    public IoStream(RubyContext context, RubyLanguage language, int fd, RubyIO io) {
        this.context = context;
        this.language = language;
        this.fd = fd;
        this.io = io;
    }

    public int getFd() {
        return fd;
    }

    public RubyIO getIo() {
        return io;
    }

    public NonBlockingInputStream getIn() {
        if (in == null) {
            // Always use the InputStreamAdapter as reading from System.in with
            // FileInputStream.readBytes() is not interruptible.
            in = new InputStreamAdapter(io);
        }

        return in;
    }

    public OutputStream getOut() {
        if (out == null) {
            switch (fd) {
                case 1:
                    out = context.getEnv().out();
                    break;

                case 2:
                    out = context.getEnv().err();
                    break;

                default:
                    out = new OutputStreamAdapter(
                            context,
                            language,
                            io,
                            context.getEncodingManager().getDefaultExternalEncoding());
            }
        }

        return out;
    }

}
