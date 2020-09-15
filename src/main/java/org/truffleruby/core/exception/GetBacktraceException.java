/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.SuppressFBWarnings;

@SuppressFBWarnings("Se")
public class GetBacktraceException extends RuntimeException implements TruffleException {

    private static final long serialVersionUID = 2633487517169337464L;

    public static final int UNLIMITED = -1;

    private final Node location;
    private final int limit;

    public GetBacktraceException(Node location, int limit) {
        this.location = location;
        this.limit = limit;
    }

    @SuppressWarnings("sync-override")
    @Override
    public final Throwable fillInStackTrace() {
        return null;
    }

    @Override
    public Node getLocation() {
        return location;
    }

    @Override
    public int getStackTraceElementLimit() {
        return limit;
    }

}
