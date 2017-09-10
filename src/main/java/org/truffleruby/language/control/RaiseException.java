/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.language.objects.ReadObjectFieldNode;

public class RaiseException extends ControlFlowException implements TruffleException {

    private static final long serialVersionUID = -4128190563044417424L;

    private final DynamicObject exception;

    public RaiseException(DynamicObject exception) {
        this.exception = exception;
    }

    public DynamicObject getException() {
        return exception;
    }

    @Override
    @TruffleBoundary
    public String getMessage() {
        final ModuleFields exceptionClass = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(exception));
        final String message = ExceptionOperations.messageToString(exceptionClass.getContext(), exception);
        return String.format("%s (%s)", message, exceptionClass.getName());
    }

    @Override
    public Node getLocation() {
        return Layouts.EXCEPTION.getBacktrace(exception).getLocation();
    }

    @Override
    public Object getExceptionObject() {
        return exception;
    }

    @Override
    public boolean isSyntaxError() {
        final RubyContext context = RubyLanguage.getCurrentContext();
        return isA(context, context.getCoreLibrary().getSyntaxErrorClass());
    }

    @Override
    public boolean isInternalError() {
        final RubyContext context = RubyLanguage.getCurrentContext();
        return isA(context, context.getCoreLibrary().getRubyTruffleErrorClass());
    }

    @Override
    public boolean isExit() {
        final RubyContext context = RubyLanguage.getCurrentContext();
        return isA(context, context.getCoreLibrary().getSystemExitClass());
    }

    @Override
    public int getExitStatus() {
        final Object status = ReadObjectFieldNode.read(exception, "@status", 1);

        if (status instanceof Integer) {
            return (int) status;
        }

        throw new UnsupportedOperationException(String.format("Ruby exit exception status is not an integer (%s)", status.getClass()));
    }

    private boolean isA(RubyContext context, DynamicObject rubyClass) {
        return context.send(exception, "is_a?", null, rubyClass) == Boolean.TRUE;
    }

}
