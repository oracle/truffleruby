/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.stdlib.readline;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple file descriptor -> IO stream class.
 *
 * The IO stream is created on a best effort basis and may not work on particular platforms.
 * No attempt is made to track close status. If file descriptors wrap around it is possible
 * to have two instances representing ultimately different files.
 */
public class FileStreamer {

    private final int fd;
    private InputStream in;
    private OutputStream out;

    public FileStreamer(int fd) {
        this.fd = fd;
    }

    public int getFd() {
        return fd;
    }

    public InputStream getIn() {
        if (in == null) {
            try {
                in = new FileInputStream("/dev/fd/" + fd);
            } catch (FileNotFoundException e) {
                throw new UnsupportedOperationException(e);
            }
        }

        return in;
    }

    public OutputStream getOut() {
        if (out == null) {
            try {
                out = new FileOutputStream("/dev/fd/" + fd);
            } catch (FileNotFoundException e) {
                throw new UnsupportedOperationException(e);
            }
        }

        return out;
    }

}
