/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.library.RubyStringLibrary;

@ExportLibrary(RubyLibrary.class)
@ExportLibrary(InteropLibrary.class)
@ExportLibrary(RubyStringLibrary.class)
public class RubyString extends RubyDynamicObject {

    public boolean frozen;
    public Rope rope;

    public RubyString(RubyClass rubyClass, Shape shape, boolean frozen, Rope rope) {
        super(rubyClass, shape);
        this.frozen = frozen;
        this.rope = rope;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return rope.toString();
    }

    // region RubyStringLibrary messages
    @ExportMessage
    public boolean isRubyString() {
        return true;
    }

    @ExportMessage
    protected Rope getRope() {
        return rope;
    }

    @ExportMessage
    public String getJavaString() {
        return RopeOperations.decodeRope(rope);
    }
    // endregion

    // region RubyLibrary messages
    @ExportMessage
    public void freeze() {
        frozen = true;
    }

    @ExportMessage
    public boolean isFrozen() {
        return frozen;
    }
    // endregion

    // region String messages
    @ExportMessage
    public boolean isString() {
        return true;
    }

    @ExportMessage
    public String asString(
            @Cached ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(this);
    }
    // endregion

}
