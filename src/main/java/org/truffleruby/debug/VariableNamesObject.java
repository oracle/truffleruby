/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(InteropLibrary.class)
public class VariableNamesObject implements TruffleObject {

    private final String[] names;

    public VariableNamesObject(String[] names) {
        this.names = names;
    }

    @ExportMessage
    protected boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    protected long getArraySize() {
        return names.length;
    }

    @ExportMessage
    protected Object readArrayElement(long index,
            @Cached BranchProfile errorProfile) throws InvalidArrayIndexException {
        if (isArrayElementReadable(index)) {
            return names[(int) index];
        } else {
            errorProfile.enter();
            throw InvalidArrayIndexException.create(index);
        }
    }

    @ExportMessage
    protected boolean isArrayElementReadable(long index) {
        return index >= 0 && index < names.length;
    }

}
