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
public final class SpecialVariableStorage implements TruffleObject {

    /** $~ */
    private ThreadAndFrameLocalStorage lastMatch;
    /** $_ */
    private ThreadAndFrameLocalStorage lastLine;

    public Object getLastMatch(ConditionProfile unsetProfile, ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(lastMatch == null)) {
            return Nil.INSTANCE;
        } else {
            return lastMatch.get(sameThreadProfile);
        }
    }

    public void setLastMatch(Object value, RubyContext context, ConditionProfile unsetProfile,
            ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(lastMatch == null)) {
            lastMatch = new ThreadAndFrameLocalStorage(context);
        }
        lastMatch.set(value, sameThreadProfile);
    }

    public Object getLastLine(ConditionProfile unsetProfile, ConditionProfile sameThreadProfilew) {
        if (unsetProfile.profile(lastLine == null)) {
            return Nil.INSTANCE;
        } else {
            return lastLine.get(sameThreadProfilew);
        }
    }

    public void setLastLine(Object value, RubyContext context, ConditionProfile unsetProfile,
            ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(lastLine == null)) {
            lastLine = new ThreadAndFrameLocalStorage(context);
        }
        lastLine.set(value, sameThreadProfile);
    }

    @TruffleBoundary
    @ExportMessage
    protected String toDisplayString(boolean allowSideEffects) {
        final InteropLibrary interop = InteropLibrary.getUncached();
        final ConditionProfile profile = ConditionProfile.getUncached();
        String result = "SpecialVariableStorage($~: ";
        if (lastMatch != null) {
            try {
                result += interop.asString(interop.toDisplayString(lastMatch.get(profile), allowSideEffects));
            } catch (UnsupportedMessageException e) {
                throw TranslateInteropExceptionNode.getUncached().execute(e);
            }
        } else {
            result += "nil";
        }

        result += ", $_: ";
        if (lastLine != null) {
            try {
                result += interop.asString(interop.toDisplayString(lastLine.get(profile), allowSideEffects));
            } catch (UnsupportedMessageException e) {
                throw TranslateInteropExceptionNode.getUncached().execute(e);
            }
        } else {
            result += "nil";
        }
        return result + ")";
    }

    @Override
    public String toString() {
        return toDisplayString(false);
    }

}
