/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import static org.truffleruby.language.RubyBaseNode.nil;

public class ThreadLocalGlobals {

    private Object lastException; // $!
    public Object processStatus; // $?

    public ThreadLocalGlobals() {
        this.lastException = nil;
        this.processStatus = nil;
    }

    public Object getLastException() {
        return lastException;
    }

    public void setLastException(Object exception) {
        assert !(exception instanceof TerminationException) : "$? should never be a TerminationException: " + exception;
        assert !(exception instanceof RaiseException) : "$? should never be a RaiseException: " + exception;
        assert exception == nil || exception instanceof RubyException || exception instanceof AbstractTruffleException;
        this.lastException = exception;
    }
}
