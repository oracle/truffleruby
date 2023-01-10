/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@SuppressWarnings("serial")
@ExportLibrary(InteropLibrary.class)
public abstract class TerminationException extends AbstractTruffleException {

    @TruffleBoundary
    private static RuntimeException javaStacktrace() {
        return new RuntimeException();
    }

    public TerminationException(String message, Node location) {
        // javaStacktrace() to help debugging if it happens in unexpected places
        super(message, javaStacktrace(), UNLIMITED_STACK_TRACE, location);
    }

    @ExportMessage
    protected ExceptionType getExceptionType() {
        return ExceptionType.INTERRUPT;
    }

}
