/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.nodes.Node;

public class GetBacktraceException extends AbstractTruffleException {

    private static final long serialVersionUID = 2633487517169337464L;

    public static final int UNLIMITED = AbstractTruffleException.UNLIMITED_STACK_TRACE;

    public GetBacktraceException(Node location, int limit) {
        super("<GetBacktraceException>", null, limit, location);
    }

}
