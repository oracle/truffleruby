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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.arguments.RubyArguments;

import java.util.Objects;

@ExportLibrary(InteropLibrary.class)
public final class SpecialVariableStorage implements TruffleObject {

    // This slot is not needed for all frames and could be lazily added via auxiliary slots,
    // but that would add a lot of complexity and significantly increase the footprint of frames which do need it.
    public static final int SLOT_INDEX = 1;
    public static final String SLOT_NAME = Layouts.TEMP_PREFIX + "$~_";
    public static final String ASSUMPTION_NAME = "does not need SpecialVariableStorage";

    public static Object get(Frame frame) {
        assert hasSpecialVariableStorageSlot(frame);
        return frame.getObject(SLOT_INDEX);
    }

    public static void set(Frame frame, SpecialVariableStorage variables) {
        assert hasSpecialVariableStorageSlot(frame);
        frame.setObject(SLOT_INDEX, variables);
    }

    public static Assumption getAssumption(FrameDescriptor descriptor) {
        assert hasSpecialVariableStorageSlot(descriptor);
        return Objects.requireNonNull((Assumption) descriptor.getInfo());
    }

    public static boolean hasSpecialVariableStorageSlot(Frame frame) {
        assert RubyArguments.getDeclarationFrame(frame) == null;
        return hasSpecialVariableStorageSlot(frame.getFrameDescriptor());
    }

    private static boolean hasSpecialVariableStorageSlot(FrameDescriptor descriptor) {
        assert SLOT_INDEX < descriptor.getNumberOfSlots();
        assert descriptor.getSlotName(SLOT_INDEX) == SLOT_NAME;
        Assumption assumption = (Assumption) descriptor.getInfo();
        return assumption.getName() == ASSUMPTION_NAME;
    }

    /** $~ */
    private ThreadAndFrameLocalStorage lastMatch;
    /** $_ */
    private ThreadAndFrameLocalStorage lastLine;

    public Object getLastMatch(ConditionProfile unsetProfile, ConditionProfile sameThreadProfile) {
        if (unsetProfile.profile(lastMatch == null)) {
            return Nil.get();
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
            return Nil.get();
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
