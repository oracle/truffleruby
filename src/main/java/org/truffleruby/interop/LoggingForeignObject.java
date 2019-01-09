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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = LoggingForeignObject.class)
public class LoggingForeignObject implements TruffleObject {

    private final StringBuilder log = new StringBuilder();

    public synchronized void log(String message, Object... args) {
        log.append(String.format(message, args));
        log.append("\n");
    }

    public synchronized String getLog() {
        return log.toString();
    }


    @CanResolve
    public abstract static class Check extends Node {

        @TruffleBoundary
        protected static boolean test(TruffleObject receiver) {
            return receiver instanceof LoggingForeignObject;
        }

    }

    @Resolve(message = "IS_NULL")
    public static abstract class ForeignIsNullNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("IS_NULL");
            return false;
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class ForeignHasSizeNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("HAS_SIZE");
            return false;
        }

    }

    @Resolve(message = "GET_SIZE")
    public static abstract class ForeignGetSizeNode extends Node {

        @TruffleBoundary
        protected int access(LoggingForeignObject log) {
            log.log("GET_SIZE");
            return 0;
        }

    }

    @Resolve(message = "IS_BOXED")
    public static abstract class ForeignIsBoxedNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("IS_BOXED");
            return false;
        }

    }

    @Resolve(message = "UNBOX")
    public static abstract class ForeignUnboxNode extends Node {

        @TruffleBoundary
        protected int access(LoggingForeignObject log) {
            log.log("UNBOX");
            return 0;
        }

    }

    @Resolve(message = "IS_POINTER")
    public static abstract class ForeignIsPointerNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("IS_POINTER");
            return false;
        }

    }

    @Resolve(message = "AS_POINTER")
    public static abstract class ForeignAsPointerNode extends Node {

        @TruffleBoundary
        protected int access(LoggingForeignObject log) {
            log.log("AS_POINTER");
            return 0;
        }

    }

    @Resolve(message = "TO_NATIVE")
    public static abstract class ForeignToNativeNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log) {
            log.log("TO_NATIVE");
            return log;
        }

    }

    @Resolve(message = "READ")
    public static abstract class ForeignReadNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, Object name) {
            log.log("READ(%s)", name);
            return log;
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class ForeignWriteNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, Object name, Object value) {
            log.log("WRITE(%s, %s)", name, value);
            return log;
        }

    }

    @Resolve(message = "REMOVE")
    public static abstract class ForeignRemoveNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log, Object name) {
            log.log("REMOVE(%s)", name);
            return false;
        }

    }

    @Resolve(message = "HAS_KEYS")
    public static abstract class ForeignHasKeysNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("HAS_KEYS");
            return false;
        }

    }

    @Resolve(message = "KEYS")
    public static abstract class ForeignKeysNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, boolean internal) {
            log.log("KEYS(%s)", internal);
            return log;
        }

    }

    @Resolve(message = "KEY_INFO")
    public static abstract class ForeignKeyInfoNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, Object name) {
            log.log("KEY_INFO(%s)", name);
            return log;
        }

    }

    @Resolve(message = "IS_EXECUTABLE")
    public static abstract class ForeignIsExecutableNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("IS_EXECUTABLE");
            return false;
        }

    }

    @Resolve(message = "EXECUTE")
    public static abstract class ForeignExecuteNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, Object[] arguments) {
            log.log("EXECUTE(...)");
            return log;
        }

    }

    @Resolve(message = "INVOKE")
    public static abstract class ForeignInvokeNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, String name, Object[] arguments) {
            if (name.toString().equals("log")) {
                return log.getLog();
            } else {
                log.log("INVOKE(%s, ...)", name);
                return log;
            }
        }

    }

    @Resolve(message = "IS_INSTANTIABLE")
    public static abstract class ForeignIsInstantiableNode extends Node {

        @TruffleBoundary
        protected boolean access(LoggingForeignObject log) {
            log.log("IS_INSTANTIABLE");
            return false;
        }

    }

    @Resolve(message = "NEW")
    public static abstract class ForeignNewNode extends Node {

        @TruffleBoundary
        protected Object access(LoggingForeignObject log, Object[] arguments) {
            log.log("NEW(...)");
            return log;
        }

    }

    @Override
    public ForeignAccess getForeignAccess() {
        return LoggingForeignObjectForeign.ACCESS;
    }

}
