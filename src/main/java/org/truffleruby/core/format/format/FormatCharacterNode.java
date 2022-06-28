/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.format;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToIntNodeGen;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.LiteralFormatNode;
import org.truffleruby.core.format.convert.ToStringNode;
import org.truffleruby.core.format.convert.ToStringNodeGen;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;
import org.truffleruby.core.format.write.bytes.WriteByteNodeGen;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.library.RubyStringLibrary;

@NodeChild("width")
@NodeChild("value")
public abstract class FormatCharacterNode extends FormatNode {

    private final boolean hasMinusFlag;

    @Child private ToIntNode toIntegerNode;
    @Child private ToStringNode toStringNode;

    public FormatCharacterNode(boolean hasMinusFlag) {
        this.hasMinusFlag = hasMinusFlag;
    }

    @Specialization(guards = { "width == cachedWidth" }, limit = "getLimit()")
    protected byte[] formatCached(int width, Object value,
            @Cached("width") int cachedWidth,
            @Cached("makeFormatString(width)") String cachedFormatString,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
        final String charString = getCharString(value, libString);
        return StringUtils.formatASCIIBytes(cachedFormatString, charString);
    }

    @Specialization(replaces = "formatCached")
    protected byte[] format(int width, Object value,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
        final String charString = getCharString(value, libString);
        return StringUtils.formatASCIIBytes(makeFormatString(width), charString);
    }

    @TruffleBoundary
    protected String getCharString(Object value, RubyStringLibrary libString) {
        if (toStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStringNode = insert(ToStringNodeGen.create(
                    false,
                    "to_str",
                    false,
                    null,
                    WriteByteNodeGen.create(new LiteralFormatNode(value))));
        }
        Object toStrResult;
        try {
            toStrResult = toStringNode.executeToString(value);
        } catch (NoImplicitConversionException e) {
            toStrResult = null;
        }

        final String charString;
        if (toStrResult == null || RubyGuards.isNil(toStrResult)) {
            if (toIntegerNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntegerNode = insert(ToIntNodeGen.create(null));
            }
            final int charValue = toIntegerNode.execute(value);
            // TODO BJF check char length is > 0
            charString = Character.toString((char) charValue);
        } else if (libString.isRubyString(toStrResult)) {
            final String resultString = RubyGuards.getJavaString(toStrResult);
            final int size = resultString.length();
            if (size > 1) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentErrorCharacterRequired(this));
            }
            charString = resultString;
        } else {
            var tstring = (TruffleString) toStrResult;
            charString = tstring.toJavaStringUncached();
            if (charString.length() > 1) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentErrorCharacterRequired(this));
            }
        }
        return charString;
    }

    @TruffleBoundary
    protected String makeFormatString(int width) {
        final boolean leftJustified = hasMinusFlag || width < 0;
        if (width == PrintfSimpleTreeBuilder.DEFAULT) {
            width = 1;
        }
        if (width < 0) {
            width = -width;
        }
        return "%" + (leftJustified ? "-" : "") + width + "." + width + "s";
    }

    protected int getLimit() {
        return getLanguage().options.PACK_CACHE;
    }

}
