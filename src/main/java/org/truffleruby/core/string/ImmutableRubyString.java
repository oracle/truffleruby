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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

/** All ImmutableRubyString are interned and must be created through
 * {@link FrozenStringLiterals#getFrozenStringLiteral(Rope)}. */
@ExportLibrary(InteropLibrary.class)
@ExportLibrary(RubyStringLibrary.class)
public class ImmutableRubyString extends ImmutableRubyObject implements TruffleObject {

    public final LeafRope rope;
    //    public RubyEncoding encoding;

    public ImmutableRubyString(LeafRope rope) {
        this.rope = rope;
        //        this.encoding = null;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return rope.toString();
    }

    // region RubyStringLibrary messages
    @ExportMessage
    public RubyEncoding getEncoding(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        // TODO
        return context.getEncodingManager().getRubyEncoding(rope.encoding);
    }

    @ExportMessage
    protected boolean isRubyString() {
        return true;
    }

    @ExportMessage
    protected Rope getRope() {
        return rope;
    }

    @ExportMessage
    protected String getJavaString() {
        return RopeOperations.decodeRope(rope);
    }
    // endregion

    // region InteropLibrary messages
    @ExportMessage
    protected Object toDisplayString(boolean allowSideEffects,
            @Cached DispatchNode dispatchNode,
            @Cached KernelNodes.ToSNode kernelToSNode) {
        if (allowSideEffects) {
            return dispatchNode.call(this, "inspect");
        } else {
            return kernelToSNode.executeToS(this);
        }
    }

    @ExportMessage
    protected boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    protected RubyClass getMetaObject(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().stringClass;
    }
    // endregion

    // region String messages
    @ExportMessage
    protected boolean isString() {
        return true;
    }

    @ExportMessage
    protected String asString(
            @Cached ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(this);
    }
    // endregion

}
