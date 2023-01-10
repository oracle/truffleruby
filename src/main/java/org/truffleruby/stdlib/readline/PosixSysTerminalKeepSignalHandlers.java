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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.graalvm.shadowed.org.jline.terminal.impl.AbstractPosixTerminal;
import org.graalvm.shadowed.org.jline.terminal.impl.AbstractTerminal;
import org.graalvm.shadowed.org.jline.terminal.spi.Pty;
import org.graalvm.shadowed.org.jline.utils.NonBlocking;
import org.graalvm.shadowed.org.jline.utils.NonBlockingInputStream;
import org.graalvm.shadowed.org.jline.utils.NonBlockingReader;

/** PosixSysTerminal but without overriding JVM signals (Ruby already sets up SIGINT) */
public class PosixSysTerminalKeepSignalHandlers extends AbstractPosixTerminal {

    protected final NonBlockingInputStream input;
    protected final OutputStream output;
    protected final NonBlockingReader reader;
    // Checkstyle: stop
    protected final PrintWriter writer;

    public PosixSysTerminalKeepSignalHandlers(
            String name,
            String type,
            Pty pty,
            Charset encoding) throws IOException {
        super(name, type, pty, encoding, SignalHandler.SIG_IGN);
        this.input = NonBlocking.nonBlocking(getName(), pty.getSlaveInput());
        this.output = pty.getSlaveOutput();
        this.reader = NonBlocking.nonBlocking(getName(), input, encoding());
        this.writer = new PrintWriter(new OutputStreamWriter(output, encoding()));
        parseInfoCmp();
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public NonBlockingReader reader() {
        return reader;
    }

    @Override
    public PrintWriter writer() {
        return writer;
    }
    // Checkstyle: start

    /** {@link AbstractTerminal#close()} is final in JLine 3.14+ */
    public void customClose() throws IOException {
        close();
        // Do not call reader.close()
        reader.shutdown();
    }
}
