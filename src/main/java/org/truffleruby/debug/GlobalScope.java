/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import org.truffleruby.language.globals.GlobalVariableStorage;
import org.truffleruby.language.globals.GlobalVariables;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

public class GlobalScope {

    public static Scope getGlobalScope(GlobalVariables globalVariables) {
        /*
         * TODO CS 23-Apr-19 what really is the global scope of Ruby? Global variables? Top-level binding? The main
         * object? The Object class? All of them? In what order? I think we're supposed to include explicitly exported
         * symbols here.
         */

        return Scope.newBuilder("global", new GlobalVariablesObject(globalVariables)).build();
    }

    @ExportLibrary(InteropLibrary.class)
    public static class GlobalVariablesObject implements TruffleObject {

        private final GlobalVariables globalVariables;

        private GlobalVariablesObject(GlobalVariables globalVariables) {
            this.globalVariables = globalVariables;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean hasMembers() {
            return true;
        }

        @ExportMessage
        @TruffleBoundary
        protected Object readMember(String member) throws UnknownIdentifierException {
            final GlobalVariableStorage storage = globalVariables.getStorage(member);
            if (storage == null) {
                throw UnknownIdentifierException.create(member);
            } else {
                return storage.getValue();
            }
        }

        @ExportMessage
        @TruffleBoundary
        protected void writeMember(String member, Object value) throws UnsupportedMessageException, UnknownIdentifierException {
            final GlobalVariableStorage storage = globalVariables.getStorage(member);
            if (storage == null) {
                throw UnknownIdentifierException.create(member);
            } else {
                throw UnsupportedMessageException.create();
            }
        }

        @ExportMessage
        @TruffleBoundary
        protected Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
            return new VariableNamesObject(globalVariables.keys());
        }

        @ExportMessage
        @TruffleBoundary
        protected boolean isMemberReadable(String member) {
            return globalVariables.doesVariableExist(member);
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean isMemberModifiable(String member) {
            return false;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
            return false;
        }

    }

}
