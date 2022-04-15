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
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.RubyLanguage;

/** This object represents a struct pointer in C held by a Ruby object. */
@ExportLibrary(InteropLibrary.class)
public final class DataHolder implements TruffleObject {

    private volatile Object address;

    public DataHolder(Object address) {
        this.address = address;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
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
        return address.toString();
    }

    @TruffleBoundary
    @ExportMessage
    protected String toDisplayString(boolean allowSideEffects) {
        final InteropLibrary interop = InteropLibrary.getUncached();
        return "DATA_HOLDER: " + interop.toDisplayString(address, allowSideEffects);
    }
}
