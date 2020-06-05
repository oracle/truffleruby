/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.bytes;

import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("value")
@ImportStatic(ArrayGuards.class)
public abstract class TaintFromSourceNode extends FormatNode {

    @Specialization(guards = "!isSourceTainted(frame)")
    protected Object noTaintNeeded(VirtualFrame frame, Object object) {
        return object;
    }

    @Specialization(guards = "isSourceTainted(frame)", limit = "getRubyLibraryCacheLimit()")
    protected Object taintNeeded(VirtualFrame frame, Object object,
            @CachedLibrary("object") RubyLibrary rubyLibrary) {
        rubyLibrary.taint(object);
        return object;
    }

}
