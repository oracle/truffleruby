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

import com.oracle.truffle.api.CompilerAsserts;
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
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.CExtNodes;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.ImmutableRubyObjectCopyable;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

/** All ImmutableRubyString are interned and must be created through
 * {@link FrozenStringLiterals#getFrozenStringLiteral}. */
@ExportLibrary(InteropLibrary.class)
@ExportLibrary(RubyStringLibrary.class)
public final class ImmutableRubyString extends ImmutableRubyObjectCopyable implements TruffleObject {

    public final TruffleString tstring;
    public final RubyEncoding encoding;
    private Pointer nativeString = null;

    ImmutableRubyString(TruffleString tstring, RubyEncoding encoding) {
        assert tstring.isCompatibleTo(encoding.tencoding);
        assert tstring.isManaged();
        this.tstring = tstring;
        this.encoding = encoding;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return tstring.toString();
    }

    public TruffleString asTruffleStringUncached() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring;
    }

    public String getJavaString() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return TStringUtils.toJavaStringOrThrow(tstring, encoding);
    }

    public boolean isNative() {
        return nativeString != null;
    }

    public Pointer getNativeString(RubyLanguage language) {
        if (nativeString == null) {
            return createNativeString(language);
        }
        return nativeString;
    }

    @TruffleBoundary
    private synchronized Pointer createNativeString(RubyLanguage language) {
        if (nativeString == null) {
            var tencoding = encoding.tencoding;
            int byteLength = tstring.byteLength(tencoding);
            nativeString = CExtNodes.StringToNativeNode.allocateAndCopyToNative(tstring, tencoding, byteLength,
                    TruffleString.CopyToNativeMemoryNode.getUncached(), language);
        }
        return nativeString;
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
    protected AbstractTruffleString getTString() {
        return tstring;
    }

    @ExportMessage
    public int byteLength() {
        return tstring.byteLength(encoding.tencoding);
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
    protected TruffleString asTruffleString(
            @Cached TruffleString.AsTruffleStringNode asTruffleStringNode) {
        return asTruffleStringNode.execute(tstring, encoding.tencoding);
    }

    @ExportMessage
    public static class AsString {
        @Specialization(
                guards = "equalNode.execute(string.tstring, string.encoding, cachedTString, cachedEncoding)",
                limit = "getLimit()")
        protected static String asStringCached(ImmutableRubyString string,
                @Cached("string.tstring") TruffleString cachedTString,
                @Cached("string.encoding") RubyEncoding cachedEncoding,
                @Cached("string.getJavaString()") String javaString,
                @Cached StringHelperNodes.EqualNode equalNode) {
            return javaString;
        }

        @Specialization(replaces = "asStringCached")
        protected static String asStringUncached(ImmutableRubyString string,
                @Cached TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Cached TruffleString.ToJavaStringNode toJavaStringNode,
                @Cached ConditionProfile binaryNonAsciiProfile) {
            if (binaryNonAsciiProfile.profile(string.encoding == Encodings.BINARY &&
                    !StringGuards.is7Bit(string.tstring, string.encoding, codeRangeNode))) {
                return getJavaStringBoundary(string);
            } else {
                return toJavaStringNode.execute(string.tstring);
            }
        }

        @TruffleBoundary
        private static String getJavaStringBoundary(ImmutableRubyString string) {
            return string.getJavaString();
        }

        protected static int getLimit() {
            return RubyLanguage.getCurrentLanguage().options.INTEROP_CONVERT_CACHE;
        }
    }
    // endregion

}
