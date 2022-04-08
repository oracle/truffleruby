/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

/** All ImmutableRubyString are interned and must be created through
 * {@link FrozenStringLiterals#getFrozenStringLiteral(Rope)}. */
@ExportLibrary(InteropLibrary.class)
@ExportLibrary(RubyStringLibrary.class)
public class ImmutableRubyString extends ImmutableRubyObject implements TruffleObject {

    public final LeafRope rope;
    public final RubyEncoding encoding;
    private NativeRope nativeRope = null;

    ImmutableRubyString(LeafRope rope, RubyEncoding encoding) {
        assert rope.encoding == encoding.jcoding;
        this.rope = rope;
        this.encoding = encoding;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return rope.toString();
    }

    public boolean isNative() {
        return nativeRope != null;
    }

    public synchronized NativeRope getNativeRope(RubyLanguage language) {
        if (nativeRope == null) {
            nativeRope = createNativeRope(language);
        }
        return nativeRope;
    }

    @TruffleBoundary
    private NativeRope createNativeRope(RubyLanguage language) {
        return new NativeRope(rope.getBytes(), rope.getEncoding(), rope.characterLength(), rope.getCodeRange(), language);
    }

    // region RubyStringLibrary messages
    @ExportMessage
    public RubyEncoding getEncoding() {
        return encoding;
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
            @CachedLibrary("this") InteropLibrary node) {
        return RubyContext.get(node).getCoreLibrary().stringClass;
    }
    // endregion

    // region String messages
    @ExportMessage
    protected boolean isString() {
        return true;
    }

    @ExportMessage
    public static class AsString {
        @Specialization(
                guards = "equalsNode.execute(string.rope, cachedRope)",
                limit = "getLimit()")
        protected static String asStringCached(ImmutableRubyString string,
                @Cached("string.rope") Rope cachedRope,
                @Cached("string.getJavaString()") String javaString,
                @Cached RopeNodes.EqualNode equalsNode) {
            return javaString;
        }

        @Specialization(replaces = "asStringCached")
        protected static String asStringUncached(ImmutableRubyString string,
                @Cached ConditionProfile asciiOnlyProfile,
                @Cached RopeNodes.AsciiOnlyNode asciiOnlyNode,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Rope rope = string.rope;
            final byte[] bytes = bytesNode.execute(rope);

            if (asciiOnlyProfile.profile(asciiOnlyNode.execute(rope))) {
                return RopeOperations.decodeAscii(bytes);
            } else {
                return RopeOperations.decodeNonAscii(rope.getEncoding(), bytes, 0, bytes.length);
            }
        }

        protected static int getLimit() {
            return RubyLanguage.getCurrentLanguage().options.INTEROP_CONVERT_CACHE;
        }
    }
    // endregion

}
