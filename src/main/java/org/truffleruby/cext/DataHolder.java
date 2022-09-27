/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.RubyLanguage;
import org.truffleruby.interop.TranslateInteropExceptionNode;

/** This object represents a struct pointer in C held by a Ruby object. */
@ExportLibrary(InteropLibrary.class)
public final class DataHolder implements TruffleObject {

    private Object pointer;
    private Object marker;

    public DataHolder(Object pointer, Object marker) {
        this.pointer = pointer;
        this.marker = marker;
    }

    public Object getPointer() {
        return pointer;
    }

    public void setPointer(Object pointer) {
        this.pointer = pointer;
    }

    public Object getMarker() {
        return marker;
    }

    public void setMarker(Object marker) {
        this.marker = marker;
    }

    @ExportMessage
    protected boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    protected Class<RubyLanguage> getLanguage() {
        return RubyLanguage.class;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return pointer.toString();
    }

    @TruffleBoundary
    @ExportMessage
    protected String toDisplayString(boolean allowSideEffects) {
        final InteropLibrary interop = InteropLibrary.getUncached();
        try {
            return "DATA_HOLDER: " + interop.asString(interop.toDisplayString(pointer, allowSideEffects));
        } catch (UnsupportedMessageException e) {
            throw TranslateInteropExceptionNode.getUncached().execute(e);
        }
    }
}
