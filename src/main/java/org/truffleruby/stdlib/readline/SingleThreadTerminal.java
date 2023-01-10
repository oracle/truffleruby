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

import org.graalvm.shadowed.org.jline.terminal.Attributes;
import org.graalvm.shadowed.org.jline.terminal.Size;
import org.graalvm.shadowed.org.jline.terminal.impl.AbstractTerminal;
import org.graalvm.shadowed.org.jline.utils.NonBlocking;
import org.graalvm.shadowed.org.jline.utils.NonBlockingInputStream;
import org.graalvm.shadowed.org.jline.utils.NonBlockingReader;

/** A virtual Terminal which reads and writes from the same thread as the thread calling readLine() */
public class SingleThreadTerminal extends AbstractTerminal {

    private final NonBlockingInputStream input;
    private final NonBlockingReader reader;
    private final OutputStream output;
    // Checkstyle: stop
    private final PrintWriter writer;

    private final Attributes attributes;
    private final Size size;

    public SingleThreadTerminal(
            String name,
            String type,
            NonBlockingInputStream input,
            OutputStream output,
            Charset encoding,
            Attributes attributes,
            Size size) throws IOException {
        super(name, type, encoding, SignalHandler.SIG_DFL);
        this.input = input;
        this.reader = NonBlocking.nonBlocking(getName(), input, encoding());
        this.output = output;
        this.writer = new PrintWriter(new OutputStreamWriter(output, encoding()));
        this.attributes = attributes;
        this.size = size;
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public NonBlockingReader reader() {
        return reader;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public PrintWriter writer() {
        return writer;
    }
    // Checkstyle: start

    @Override
    public Attributes getAttributes() {
        Attributes copy = new Attributes();
        copy.copy(this.attributes);
        return copy;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        this.attributes.copy(attributes);
    }

    @Override
    public Size getSize() {
        Size copy = new Size();
        copy.copy(this.size);
        return copy;
    }

    @Override
    public void setSize(Size size) {
        this.size.copy(size);
    }

}
