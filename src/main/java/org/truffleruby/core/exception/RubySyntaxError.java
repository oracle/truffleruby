/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.backtrace.Backtrace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;

@ExportLibrary(InteropLibrary.class)
public class RubySyntaxError extends RubyException {

    public RubySyntaxError(RubyClass rubyClass, Shape shape, Object message, Backtrace backtrace, Object cause) {
        super(rubyClass, shape, message, backtrace, cause);
    }

    // region Exception interop
    @ExportMessage
    public ExceptionType getExceptionType() {
        return ExceptionType.PARSE_ERROR;
    }

    @ExportMessage
    public boolean isExceptionIncompleteSource() {
        return false; // Unknown
    }

    @TruffleBoundary
    @ExportMessage
    public boolean hasSourceLocation() {
        if (backtrace != null && backtrace.getSourceLocation() != null) {
            return true;
        } else {
            final Node location = getLocation();
            return location != null && location.getEncapsulatingSourceSection() != null;
        }
    }

    @TruffleBoundary
    @ExportMessage
    public SourceSection getSourceLocation() throws UnsupportedMessageException {
        if (backtrace != null && backtrace.getSourceLocation() != null) {
            return backtrace.getSourceLocation();
        } else {
            final Node location = getLocation();
            SourceSection sourceSection = location != null ? location.getEncapsulatingSourceSection() : null;
            if (sourceSection != null) {
                return sourceSection;
            } else {
                throw UnsupportedMessageException.create();
            }
        }
    }
    // endregion

}
