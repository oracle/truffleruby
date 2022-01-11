/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.library.RubyLibrary;

@ExportLibrary(RubyLibrary.class)
public abstract class RubyRange extends RubyDynamicObject {

    public boolean excludedEnd;
    public boolean frozen;

    public RubyRange(RubyClass rubyClass, Shape shape, boolean excludedEnd, boolean frozen) {
        super(rubyClass, shape);
        this.excludedEnd = excludedEnd;
        this.frozen = frozen;
    }

    @ExportMessage
    protected void freeze() {
        frozen = true;
    }

    @ExportMessage
    protected boolean isFrozen() {
        return frozen;
    }
}
