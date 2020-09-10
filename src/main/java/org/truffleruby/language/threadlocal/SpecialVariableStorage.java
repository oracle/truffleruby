/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.RubyContext;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.Nil;

@ExportLibrary(InteropLibrary.class)
public class SpecialVariableStorage implements TruffleObject {

    private ThreadAndFrameLocalStorage regexpResult;
    private ThreadAndFrameLocalStorage ioResult;

    public Object getRegexpResult(ConditionProfile unsetProfile, ConditionProfile sameThreadProfilew) {
        if (unsetProfile.profile(regexpResult == null)) {
            return Nil.INSTANCE;
        } else {
            return regexpResult.get(sameThreadProfilew);
        }
    }

    public void setRegexpResult(Object lastMatch, RubyContext context, ConditionProfile unsetProfile,
            ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(this.regexpResult == null)) {
            this.regexpResult = new ThreadAndFrameLocalStorage(context);
        }
        this.regexpResult.set(lastMatch, sameThreadProfile);
    }

    public Object getIOResult(ConditionProfile unsetProfile, ConditionProfile sameThreadProfilew) {
        if (unsetProfile.profile(ioResult == null)) {
            return Nil.INSTANCE;
        } else {
            return ioResult.get(sameThreadProfilew);
        }
    }

    public void setIOResult(Object lastMatch, RubyContext context, ConditionProfile unsetProfile,
            ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(this.ioResult == null)) {
            this.ioResult = new ThreadAndFrameLocalStorage(context);
        }
        this.ioResult.set(lastMatch, sameThreadProfile);
    }

    @TruffleBoundary
    @ExportMessage
    protected String toDisplayString(boolean allowSideEffects) {
        String result = "";
        if (regexpResult != null) {
            final InteropLibrary interop = InteropLibrary.getUncached();
            try {
                result = result + "$~: " + interop.asString(
                        interop.toDisplayString(regexpResult.get(ConditionProfile.getUncached()), allowSideEffects)) +
                        ", ";
            } catch (UnsupportedMessageException e) {
                throw TranslateInteropExceptionNode.getUncached().execute(e);
            }
        } else {
            result = result + "$~: nil, ";
        }

        if (regexpResult != null) {
            final InteropLibrary interop = InteropLibrary.getUncached();
            try {
                result = result + "$_: " + interop.asString(
                        interop.toDisplayString(ioResult.get(ConditionProfile.getUncached()), allowSideEffects));
            } catch (UnsupportedMessageException e) {
                throw TranslateInteropExceptionNode.getUncached().execute(e);
            }
        } else {
            result = result + "$_: nil";
        }
        return result;
    }
}
