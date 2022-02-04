/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.frame.MaterializedFrame;

import org.truffleruby.language.threadlocal.SpecialVariableStorage;

public final class FrameAndVariables {

    public final SpecialVariableStorage variables;
    public final MaterializedFrame frame;

    public FrameAndVariables(SpecialVariableStorage variables, MaterializedFrame frame) {
        this.variables = variables;
        this.frame = frame;
    }
}
