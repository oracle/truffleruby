/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.methods.InternalMethod;

public class MakeSpecialVariableStorageNode extends RubyNode {

    private int sourceCharIndex = NO_SOURCE;
    private int sourceLength;
    private byte flags;

    @CompilationFinal protected FrameSlot storageSlot;
    @CompilationFinal protected Assumption frameAssumption;

    @Override
    public Object execute(VirtualFrame frame) {
        if (frameAssumption == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            frameAssumption = frame.getFrameDescriptor().getVersion();
            storageSlot = frame.getFrameDescriptor().findOrAddFrameSlot(Layouts.SPECIAL_VARIABLLE_STORAGE);
        }

        if (storageSlot != null) {
            frame.setObject(storageSlot, new SpecialVariableStorage());
        }

        return nil;
    }

    @TruffleBoundary
    private static void debug(InternalMethod method) {
        System.err.printf("Starting to send foe %s.\n", method);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        return RubyNode.defaultIsDefined(context, this);
    }

    @Override
    protected byte getFlags() {
        return flags;
    }

    @Override
    protected void setFlags(byte flags) {
        this.flags = flags;
    }

    @Override
    protected int getSourceCharIndex() {
        return sourceCharIndex;
    }

    @Override
    protected void setSourceCharIndex(int sourceCharIndex) {
        this.sourceCharIndex = sourceCharIndex;
    }

    @Override
    protected int getSourceLength() {
        return sourceLength;
    }

    @Override
    protected void setSourceLength(int sourceLength) {
        this.sourceLength = sourceLength;
    }

}
