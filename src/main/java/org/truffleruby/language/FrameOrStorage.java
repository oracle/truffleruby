/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
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

public class FrameOrStorage {

    public final SpecialVariableStorage storage;
    public final MaterializedFrame frame;

    public FrameOrStorage(SpecialVariableStorage storage, MaterializedFrame frame) {
        this.storage = storage;
        this.frame = frame;
    }
}
