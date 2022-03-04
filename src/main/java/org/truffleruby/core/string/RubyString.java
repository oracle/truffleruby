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

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
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
    public boolean locked = false;
    public Rope rope;
    public RubyEncoding encoding;

    public RubyString(RubyClass rubyClass, Shape shape, boolean frozen, Rope rope, RubyEncoding rubyEncoding) {
        super(rubyClass, shape);
        assert rope.encoding == rubyEncoding.jcoding;
        this.frozen = frozen;
        this.rope = rope;
        this.encoding = rubyEncoding;
    }

    public void setRope(Rope rope) {
        assert rope.encoding == encoding.jcoding : rope.encoding.toString() + " does not equal " +
                encoding.jcoding.toString();
        this.rope = rope;
    }

    public void setRope(Rope rope, RubyEncoding encoding) {
        assert rope.encoding == encoding.jcoding;
        this.rope = rope;
        this.encoding = encoding;
    }

    /** should only be used for debugging */
    @Override
    public String toString() {
        return rope.toString();
    }

    public Encoding getJCoding() {
        assert encoding.jcoding == rope.encoding;
        return encoding.jcoding;
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
    protected boolean isString() {
        return true;
    }

    @ExportMessage
    public static class AsString {
        @Specialization(
                guards = "equalsNode.execute(string.rope, cachedRope)",
                limit = "getLimit()")
        protected static String asStringCached(RubyString string,
                @Cached("string.rope") Rope cachedRope,
                @Cached("string.getJavaString()") String javaString,
                @Cached RopeNodes.EqualNode equalsNode) {
            return javaString;
        }

        @Specialization(replaces = "asStringCached")
        protected static String asStringUncached(RubyString string,
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
