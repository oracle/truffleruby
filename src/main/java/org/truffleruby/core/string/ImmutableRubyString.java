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
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.MutableTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.CExtNodes;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.ImmutableRubyObjectCopyable;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

/** All ImmutableRubyString are interned and must be created through
 * {@link FrozenStringLiterals#getFrozenStringLiteral}. */
@ExportLibrary(InteropLibrary.class)
@ExportLibrary(RubyStringLibrary.class)
public class ImmutableRubyString extends ImmutableRubyObjectCopyable implements TruffleObject {

    public final LeafRope rope;
    public final TruffleString tstring;
    public final RubyEncoding encoding;
    // TODO should be TruffleString but native empty TruffleString is not possible currently (GR-38902)
    private MutableTruffleString nativeTString = null;

    ImmutableRubyString(TruffleString tstring, LeafRope rope, RubyEncoding encoding) {
        assert tstring.isCompatibleTo(encoding.tencoding);
        this.rope = rope;
        this.tstring = tstring;
        this.encoding = encoding;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return rope.toString();
    }

    public boolean isNative() {
        return nativeTString != null;
    }

    public MutableTruffleString getNativeTString(RubyLanguage language) {
        if (nativeTString == null) {
            return createNativeTString(language);
        }
        return nativeTString;
    }

    @TruffleBoundary
    private synchronized MutableTruffleString createNativeTString(RubyLanguage language) {
        if (nativeTString == null) {
            var tencoding = encoding.tencoding;
            int byteLength = tstring.byteLength(tencoding);
            Pointer pointer = CExtNodes.StringToNativeNode.allocateAndCopyToNative(tstring, tencoding, byteLength,
                    TruffleString.CopyToNativeMemoryNode.getUncached(), language);

            nativeTString = MutableTruffleString.fromNativePointerUncached(pointer, 0, byteLength, tencoding, false);
            assert nativeTString.isNative();
        }
        return nativeTString;
    }

    // region RubyStringLibrary messages
    @ExportMessage
    public RubyEncoding getEncoding() {
        return encoding;
    }

    @ExportMessage
    public TruffleString.Encoding getTEncoding() {
        return encoding.tencoding;
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
    protected AbstractTruffleString getTString() {
        return tstring;
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
                @Cached TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Rope rope = string.rope;
            final byte[] bytes = bytesNode.execute(rope);

            if (asciiOnlyProfile.profile(StringGuards.is7Bit(string.tstring, string.encoding, codeRangeNode))) {
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
