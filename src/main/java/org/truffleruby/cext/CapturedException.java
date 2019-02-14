/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = CapturedException.class)
public class CapturedException implements TruffleObject {

    private final Throwable exception;

    public CapturedException(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    @CanResolve
    public abstract static class IsInstance extends Node {

        protected boolean test(TruffleObject receiver) {
            return receiver instanceof CapturedException;
        }
    }

    @Resolve(message = "IS_NULL")
    public static abstract class IsNullNode extends Node {

        @CompilationFinal private ContextReference<RubyContext> contextReference;

        protected Object access(CapturedException object) {
            return false;
        }
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return CapturedExceptionForeign.ACCESS;
    }

}
