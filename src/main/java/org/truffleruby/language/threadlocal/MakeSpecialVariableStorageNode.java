/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.language.RubyContextSourceNode;

public class MakeSpecialVariableStorageNode extends RubyContextSourceNode {

    @CompilationFinal protected Assumption assumption;

    @Override
    public Object execute(VirtualFrame frame) {
        assert SpecialVariableStorage.hasSpecialVariableStorageSlot(frame);

        if (assumption == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            assumption = SpecialVariableStorage.getAssumption(frame.getFrameDescriptor());
        }

        if (!assumption.isValid()) {
            SpecialVariableStorage.set(frame, new SpecialVariableStorage());
        }

        return nil;
    }

}
