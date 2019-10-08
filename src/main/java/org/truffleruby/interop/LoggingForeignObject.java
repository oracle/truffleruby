/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.Message;
import com.oracle.truffle.api.library.ReflectionLibrary;

@ExportLibrary(ReflectionLibrary.class)
public class LoggingForeignObject implements TruffleObject {

    private final static Message INVOKE_MEMBER = Message.resolve(InteropLibrary.class, "invokeMember");
    private final static Message IS_STRING = Message.resolve(InteropLibrary.class, "isString");
    private final static Message AS_STRING = Message.resolve(InteropLibrary.class, "asString");
    private final StringBuilder log = new StringBuilder();

    public synchronized void log(String message, Object... args) {
        log.append(String.format(message, args));
        log.append("\n");
    }

    public synchronized String getLog() {
        return log.toString();
    }

    @ExportMessage
    @TruffleBoundary
    Object send(Message message, Object[] args) throws Exception {
        final Object[] flatArgs;
        if (message == INVOKE_MEMBER) {
            Object[] invokeArg = (Object[]) args[1];
            Object[] newArgs = new Object[1 + invokeArg.length];
            newArgs[0] = args[0];
            System.arraycopy(invokeArg, 0, newArgs, 1, invokeArg.length);
            flatArgs = newArgs;
        } else {
            flatArgs = args;
        }

        String[] a = new String[flatArgs.length];
        Arrays.fill(a, "%s");

        log(message.getSimpleName() + "(" + String.join(", ", a) + ")", flatArgs);

        if (message == IS_STRING) {
            return true;
        }

        if (message == AS_STRING) {
            return getLog();
        }

        if (message.getReturnType() == boolean.class) {
            return false;
        }

        throw UnsupportedMessageException.create();
    }

}
