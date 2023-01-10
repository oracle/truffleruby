/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(InteropLibrary.class)
public class SingleMemberDescriptor implements TruffleObject {

    private final String member;
    private final Object value;

    public SingleMemberDescriptor(String member, Object value) {
        this.member = member;
        this.value = value;
    }

    @ExportMessage
    protected boolean hasMembers() {
        return true;
    }

    @ExportMessage
    protected Object getMembers(boolean includeInternal) {
        return new VariableNamesObject(new String[]{ member });
    }

    @ExportMessage
    protected boolean isMemberReadable(String member) {
        return this.member.equals(member);
    }

    @ExportMessage
    protected Object readMember(String member,
            @Cached BranchProfile errorProfile) throws UnknownIdentifierException {
        if (isMemberReadable(member)) {
            return value;
        } else {
            errorProfile.enter();
            throw UnknownIdentifierException.create(member);
        }
    }
}
