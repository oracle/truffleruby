/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

public class LoggingForeignObject implements TruffleObject {

    private final StringBuilder log = new StringBuilder();

    @Override
    public ForeignAccess getForeignAccess() {
        return LoggingMessageResolutionForeign.ACCESS;
    }

    public synchronized void log(String message, Object... args) {
        log.append(String.format(message, args));
        log.append("\n");
    }

    public synchronized String getLog() {
        return log.toString();
    }

}
