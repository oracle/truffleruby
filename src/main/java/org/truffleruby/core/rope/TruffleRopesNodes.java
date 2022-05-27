/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.cext.CExtNodes;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.library.RubyStringLibrary;

@CoreModule("Truffle::Ropes")
public abstract class TruffleRopesNodes {

    @CoreMethod(names = "dump_string", onSingleton = true, required = 1)
    public abstract static class DumpStringNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)")
        protected RubyString dumpString(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            final StringBuilder builder = new StringBuilder();

            final Rope rope = strings.getRope(string);

            for (int i = 0; i < rope.byteLength(); i++) {
                builder.append(StringUtils.format("\\x%02x", rope.get(i)));
            }

            return makeStringNode.executeMake(builder.toString(), Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "debug_print_rope", onSingleton = true, required = 1, optional = 1)
    public abstract static class DebugPrintRopeNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.DebugPrintRopeNode debugPrintRopeNode = RopeNodesFactory.DebugPrintRopeNodeGen
                .create();

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)")
        protected Object debugPrintDefault(Object string, NotProvided printString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            return debugPrint(string, true, strings);
        }

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)")
        protected Object debugPrint(Object string, boolean printString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            System.err.println("Legend: ");
            System.err.println("BN = Bytes Null? (byte[] not yet populated)");
            System.err.println("BL = Byte Length");
            System.err.println("CL = Character Length");
            System.err.println("CR = Code Range");
            System.err.println("O = Byte Offset (SubstringRope only)");
            System.err.println("T = Times (RepeatingRope only)");
            System.err.println("V = Value (LazyIntRope only)");
            System.err.println("E = Encoding");
            System.err.println("P = Native Pointer (NativeRope only)");
            System.err.println("S = Native Size (NativeRope only)");

            return debugPrintRopeNode.executeDebugPrint(strings.getRope(string), 0, printString);
        }
    }

    /** The returned string (when evaluated) will create a string with the same Rope structure as the string which is
     * passed as argument. */
    @CoreMethod(names = "debug_get_structure_creation", onSingleton = true, required = 1)
    public abstract static class DebugGetStructureCreationNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)")
        protected RubyString getStructure(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            Rope rope = strings.getRope(string);
            String result = getStructure(rope);
            byte[] bytes = StringOperations.encodeBytes(result, UTF8Encoding.INSTANCE);
            return makeStringNode.executeMake(
                    bytes,
                    strings.getEncoding(string),
                    CodeRange.CR_7BIT);
        }

        protected static String getStructure(Rope rope) {
            if (rope instanceof LeafRope) {
                return getStructure((LeafRope) rope);
            } else {
                return "(unknown rope class: " + rope.getClass() + ")";
            }
        }

        private static String getStructure(LeafRope rope) {
            return RopeOperations.escape(rope);
        }

    }

    @CoreMethod(names = "flatten_rope", onSingleton = true, required = 1)
    public abstract static class FlattenRopeNode extends CoreMethodArrayArgumentsNode {

        // Also flattens the original String, but that one might still have an offset
        @TruffleBoundary
        @Specialization(guards = "libString.isRubyString(string)")
        protected RubyString flattenRope(Object string,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            final RubyEncoding rubyEncoding = libString.getEncoding(string);
            var tstring = libString.getTString(string);
            // Use GetInternalByteArrayNode as a way to flatten the TruffleString.
            // Ensure the result has offset = 0 and length = byte[].length for image build time checks
            byte[] byteArray = TStringUtils.getBytesOrCopy(tstring, rubyEncoding);
            return createString(fromByteArrayNode, byteArray, rubyEncoding);
        }

    }

    @CoreMethod(names = "convert_to_native", onSingleton = true, required = 1)
    public abstract static class NativeRopeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)")
        protected Object nativeRope(Object string,
                @Cached CExtNodes.StringToNativeNode toNativeNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            toNativeNode.executeToNative(string);
            return string;
        }

    }

    /* Truffle.create_simple_string creates a string 'test' without any part of the string escaping. Useful for testing
     * compilation of String because most other ways to construct a string can currently escape. */
    @CoreMethod(names = "create_simple_string", onSingleton = true)
    public abstract static class CreateSimpleStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString createSimpleString() {
            return createString(
                    new AsciiOnlyLeafRope(new byte[]{ 't', 'e', 's', 't' }, UTF8Encoding.INSTANCE),
                    Encodings.UTF_8);
        }
    }

}
