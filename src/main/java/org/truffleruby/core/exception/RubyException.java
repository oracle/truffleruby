/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.VMPrimitiveNodes.VMRaiseExceptionNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.ImmutableRubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(InteropLibrary.class)
public class RubyException extends RubyDynamicObject implements ObjectGraphNode {

    public Object message;
    public Backtrace backtrace;
    public Object cause;

    public RubyProc formatter = null;
    public RubyArray backtraceStringArray = null;
    /** null (not yet computed), RubyArray, or nil (empty) */
    public Object backtraceLocations = null;
    /** null (not set), RubyArray of Strings, or nil (empty) */
    public Object customBacktrace = null;

    public RubyException(RubyClass rubyClass, Shape shape, Object message, Backtrace backtrace, Object cause) {
        super(rubyClass, shape);
        // TODO (eregon, 9 Aug 2020): it should probably be null or RubyString, not nil, and the field can then be typed
        assert message == null || message == Nil.INSTANCE || message instanceof RubyString ||
                message instanceof ImmutableRubyString;
        assert cause != null;
        this.message = message;
        this.backtrace = backtrace;
        this.cause = cause;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return message.toString();
    }

    public Node getLocation() {
        final Backtrace backtrace = this.backtrace;
        if (backtrace == null) {
            // The backtrace could be null if for example a user backtrace was passed to Kernel#raise
            return null;
        } else {
            return backtrace.getLocation();
        }
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, message);
        ObjectGraph.addProperty(reachable, cause);
    }

    // region Exception interop
    @ExportMessage
    public boolean isException() {
        return true;
    }

    @ExportMessage
    public RuntimeException throwException(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw VMRaiseExceptionNode.reRaiseException(context, this);
    }

    // TODO (eregon, 01 Nov 2020): these message implementations would be nicer with separate subclasses for SystemExit and SyntaxError.

    @ExportMessage
    public ExceptionType getExceptionType(
            @Cached IsANode isANode) {
        // @CachedContext does not work here when running "mx tck"
        final RubyContext context = getMetaClass().fields.getContext();
        if (isANode.executeIsA(this, context.getCoreLibrary().systemExitClass)) {
            return ExceptionType.EXIT;
        } else if (isANode.executeIsA(this, context.getCoreLibrary().syntaxErrorClass)) {
            return ExceptionType.PARSE_ERROR;
        } else {
            return ExceptionType.RUNTIME_ERROR;
        }
    }

    @ExportMessage
    public int getExceptionExitStatus(
            @CachedLibrary("this") InteropLibrary interopLibrary,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary) throws UnsupportedMessageException {
        if (interopLibrary.getExceptionType(this) == ExceptionType.EXIT) {
            return (int) objectLibrary.getOrDefault(this, "@status", 1);
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public boolean isExceptionIncompleteSource(
            @CachedLibrary("this") InteropLibrary interopLibrary) throws UnsupportedMessageException {
        if (interopLibrary.getExceptionType(this) == ExceptionType.PARSE_ERROR) {
            return false; // Unknown
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @TruffleBoundary
    @ExportMessage
    public boolean hasSourceLocation(
            @CachedLibrary("this") InteropLibrary interopLibrary) {
        try {
            if (interopLibrary.getExceptionType(this) == ExceptionType.PARSE_ERROR &&
                    backtrace != null &&
                    backtrace.getSourceLocation() != null) {
                return true;
            } else {
                final Node location = getLocation();
                return location != null && location.getEncapsulatingSourceSection() != null;
            }
        } catch (UnsupportedMessageException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    @TruffleBoundary
    @ExportMessage
    public SourceSection getSourceLocation(
            @CachedLibrary("this") InteropLibrary interopLibrary) throws UnsupportedMessageException {
        if (interopLibrary.getExceptionType(this) == ExceptionType.PARSE_ERROR &&
                backtrace != null &&
                backtrace.getSourceLocation() != null) {
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
