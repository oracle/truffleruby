/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 *
 * Some of the code in this class is modified from org.jruby.runtime.Helpers and org.jruby.util.StringSupport,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 *
 * Contains code modified from ByteList's ByteList.java
 *
 * Copyright (C) 2007-2010 JRuby Community
 * Copyright (C) 2007 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 */
package org.truffleruby.core.rope;

import static org.truffleruby.core.rope.CodeRange.CR_7BIT;
import static org.truffleruby.core.rope.CodeRange.CR_BROKEN;
import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
import static org.truffleruby.core.rope.CodeRange.CR_VALID;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

import com.oracle.truffle.api.strings.InternalByteArray;
import org.jcodings.Encoding;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.collections.IntStack;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class RopeOperations {

    @TruffleBoundary
    public static LeafRope create(byte[] bytes, Encoding encoding, CodeRange codeRange) {
        int characterLength = -1;

        if (codeRange == CR_UNKNOWN) {
            final StringAttributes attributes = calculateCodeRangeAndLength(encoding, bytes, 0, bytes.length);

            codeRange = attributes.getCodeRange();
            characterLength = attributes.getCharacterLength();
        } else if (codeRange == CR_VALID || codeRange == CR_BROKEN) {
            characterLength = strLength(encoding, bytes, 0, bytes.length);
        }

        switch (codeRange) {
            case CR_7BIT:
                return new AsciiOnlyLeafRope(bytes, encoding);
            case CR_VALID:
                return new ValidLeafRope(bytes, encoding, characterLength);
            case CR_BROKEN:
                return new InvalidLeafRope(bytes, encoding, characterLength);
            default: {
                throw new RuntimeException(StringUtils.format("Unknown code range type: %d", codeRange));
            }
        }
    }

    public static LeafRope encodeAscii(String value, Encoding encoding) {
        return create(StringOperations.encodeAsciiBytes(value), encoding, CR_7BIT);
    }

    @TruffleBoundary
    public static String decodeNonAscii(Encoding encoding, byte[] bytes, int byteOffset, int byteLength) {
        final Charset charset;

        if (encoding == ASCIIEncoding.INSTANCE) {
            for (int i = 0; i < byteLength; i++) {
                if (bytes[byteOffset + i] < 0) {
                    throw new CannotConvertBinaryRubyStringToJavaString(bytes[byteOffset + i] & 0xFF);
                }
            }

            // Don't misinterpret non-ASCII bytes, use the replacement character to show the loss
            charset = StandardCharsets.US_ASCII;
        } else {
            charset = EncodingManager.charsetForEncoding(encoding);
        }


        return decode(charset, bytes, byteOffset, byteLength);
    }

    // Replace by TruffleString#toString(), NOT toJavaStringUncached()
    public static String decodeOrEscapeBinaryRope(Rope rope) {
        return decodeOrEscapeBinaryRope(rope, rope.getBytes());
    }

    /** Overload to avoid calling getBytes() and mutate the Rope "bytes" field. */
    @TruffleBoundary
    public static String decodeOrEscapeBinaryRope(Rope rope, byte[] bytes) {
        if (rope.isAsciiOnly() || rope.getEncoding() != ASCIIEncoding.INSTANCE) {
            return decodeRopeSegment(rope, bytes, 0, bytes.length);
        } else {
            return escapeBinaryRope(bytes);
        }
    }

    private static String escapeBinaryRope(byte[] bytes) {
        // A Rope with BINARY encoding cannot be converted faithfully to a Java String.
        // (ISO_8859_1 would just show random characters for bytes above 128)
        // Therefore we convert non-US-ASCII characters to "\xNN".
        // MRI Symbol#inspect for binary symbols is similar: "\xff".b.to_sym => :"\xFF"
        final StringBuilder builder = new StringBuilder(bytes.length);
        for (final byte c : bytes) {
            if (c >= 0) { // US-ASCII character
                builder.append((char) (c & 0xFF));
            } else {
                builder.append("\\x").append(String.format("%02X", c & 0xFF));
            }
        }
        return builder.toString();
    }

    /** NOTE: replacement is TStringUtils.toJavaStringOrThrow() */
    public static String decodeRope(Rope value) {
        return decodeRopeSegment(value, 0, value.byteLength());
    }

    @TruffleBoundary
    public static String decodeRopeSegment(Rope value, int byteOffset, int byteLength) {
        return decodeRopeSegment(value, value.getBytes(), byteOffset, byteLength);
    }

    private static String decodeRopeSegment(Rope value, byte[] bytes, int byteOffset, int byteLength) {
        if (value.isAsciiOnly()) {
            return StringOperations.decodeAscii(bytes, byteOffset, byteLength);
        } else {
            return decodeNonAscii(value.getEncoding(), bytes, byteOffset, byteLength);
        }
    }

    /** This method has no side effects, because it does not even have access to the Rope - for debugging only. */
    @TruffleBoundary
    public static String decode(Encoding encoding, byte[] bytes) {
        if (encoding == ASCIIEncoding.INSTANCE) {
            return escapeBinaryRope(bytes);
        } else {
            final Charset charset = EncodingManager.charsetForEncoding(encoding);
            return decode(charset, bytes, 0, bytes.length);
        }
    }

    private static String decode(Charset charset, byte[] bytes, int byteOffset, int byteLength) {
        return new String(bytes, byteOffset, byteLength, charset);
    }

    // MRI: rb_enc_strlen_cr
    @TruffleBoundary
    public static StringAttributes calculateCodeRangeAndLength(Encoding encoding, byte[] bytes, int start, int end) {
        if (bytes.length == 0) {
            return new StringAttributes(0, encoding.isAsciiCompatible() ? CR_7BIT : CR_VALID);
        } else if (encoding == ASCIIEncoding.INSTANCE) {
            return strLengthWithCodeRangeBinaryString(bytes, start, end);
        } else if (encoding.isAsciiCompatible()) {
            return StringSupport.strLengthWithCodeRangeAsciiCompatible(encoding, bytes, start, end);
        } else {
            return StringSupport.strLengthWithCodeRangeNonAsciiCompatible(encoding, bytes, start, end);
        }
    }

    @TruffleBoundary
    public static int strLength(Encoding enc, byte[] bytes, int p, int end) {
        return StringSupport.strLength(enc, bytes, p, end);
    }

    private static StringAttributes strLengthWithCodeRangeBinaryString(byte[] bytes, int start, int end) {
        CodeRange codeRange = CR_7BIT;

        for (int i = start; i < end; i++) {
            if (bytes[i] < 0) {
                codeRange = CR_VALID;
                break;
            }
        }

        return new StringAttributes(end - start, codeRange);
    }

    /** This method should not be used directly, because it does not cache the result in the Rope. Use
     * {@link RopeNodes.BytesNode} or {@link Rope#getBytes()} instead.
     *
     * Performs an iterative depth first search of the Rope tree to calculate its byte[] without needing to populate the
     * byte[] for each level beneath. Every LeafRope has its byte[] populated by definition. The goal is to determine
     * which descendant LeafRopes contribute bytes to the top-most Rope's logical byte[] and how many bytes they should
     * contribute. Then each such LeafRope copies the appropriate range of bytes to a shared byte[].
     *
     * Rope trees can be very deep. An iterative algorithm is preferable to recursion because it removes the overhead of
     * stack frame management. Additionally, a recursive algorithm will eventually overflow the stack if the Rope tree
     * is too deep. */
    @TruffleBoundary
    public static byte[] flattenBytes(Rope rope) {
        if (rope.getRawBytes() != null) {
            return rope.getRawBytes();
        }

        int bufferPosition = 0;
        int byteOffset = 0;

        final byte[] buffer = new byte[rope.byteLength()];

        // As we traverse the rope tree, we need to keep track of any bounded lengths of SubstringRopes. LeafRopes always
        // provide their full byte[]. ConcatRope always provides the full byte[] of each of its children. SubstringRopes,
        // in contrast, may bound the length of their children. Since we may have SubstringRopes of SubstringRopes, we
        // need to track each SubstringRope's bounded length and how much that bounded length contributes to the total
        // byte[] for any ancestor (e.g., a SubstringRope of a ConcatRope with SubstringRopes for each of its children).
        // Because we need to track multiple levels, we can't use a single updated int.
        final IntStack substringLengths = new IntStack();

        final Deque<Rope> workStack = new ArrayDeque<>();
        workStack.push(rope);

        while (!workStack.isEmpty()) {
            final Rope current = workStack.pop();

            // An empty rope trivially cannot contribute to filling the output buffer.
            if (current.isEmpty()) {
                continue;
            }

            final byte[] rawBytes = current.getRawBytes();

            if (rawBytes != null) {
                // In the absence of any SubstringRopes, we always take the full contents of the current rope.
                if (substringLengths.isEmpty()) {
                    System.arraycopy(rawBytes, byteOffset, buffer, bufferPosition, current.byteLength());
                    bufferPosition += current.byteLength();
                } else {
                    int bytesToCopy = substringLengths.pop();
                    final int currentBytesToCopy;

                    // If we reach here, this rope is a descendant of a SubstringRope at some level. Based on
                    // the currently calculated byte[] offset and the number of bytes to extract, determine how many
                    // bytes we can copy to the buffer.
                    if (bytesToCopy > (current.byteLength() - byteOffset)) {
                        currentBytesToCopy = current.byteLength() - byteOffset;
                    } else {
                        currentBytesToCopy = bytesToCopy;
                    }

                    System.arraycopy(rawBytes, byteOffset, buffer, bufferPosition, currentBytesToCopy);
                    bufferPosition += currentBytesToCopy;
                    bytesToCopy -= currentBytesToCopy;

                    // If this rope wasn't able to satisfy the remaining byte count from the ancestor SubstringRope,
                    // update the byte count for the next item in the work queue.
                    if (bytesToCopy > 0) {
                        substringLengths.push(bytesToCopy);
                    }
                }

                // By definition, offsets only affect the start of the rope. Once we've copied bytes out of a rope,
                // we need to reset the offset or subsequent items in the work queue will copy from the wrong location.
                //
                // NB: In contrast to the number of bytes to extract, the offset can be shared and updated by multiple
                // levels of SubstringRopes. Thus, we do not need to maintain offsets in a stack and it is appropriate
                // to clear the offset after the first time we use it, since it will have been updated accordingly at
                // each SubstringRope encountered for this SubstringRope ancestry chain.
                byteOffset = 0;

                continue;
            }

            throw new UnsupportedOperationException(
                    "Don't know how to flatten rope of type: " + current.getClass().getName());
        }

        return buffer;
    }

    @TruffleBoundary
    public static int hashForRange(Rope rope, int startingHashCode, int offset, int length) {
        class Params {
            final Rope rope;
            final int startingHashCode;
            final int offset;
            final int length;
            final boolean readResult;

            Params(Rope rope, int startingHashCode, int offset, int length, boolean readResult) {
                this.rope = rope;
                this.startingHashCode = startingHashCode;
                this.offset = offset;
                this.length = length;
                this.readResult = readResult;
            }
        }

        final Deque<Params> workStack = new ArrayDeque<>();
        workStack.push(new Params(rope, startingHashCode, offset, length, false));
        int resultHash = 0;

        while (!workStack.isEmpty()) {
            final Params params = workStack.pop();
            rope = params.rope;
            startingHashCode = params.readResult ? resultHash : params.startingHashCode;
            offset = params.offset;
            length = params.length;
            final byte[] bytes = rope.getRawBytes();

            if (bytes != null) {
                resultHash = Hashing.stringHash(bytes, startingHashCode, offset, length);
            } else {
                resultHash = Hashing.stringHash(rope.getBytes(), startingHashCode, offset, length);
            }
        }

        return resultHash;
    }

    @TruffleBoundary
    public static int caseInsensitiveCmp(InternalByteArray value, InternalByteArray other) {
        // Taken from org.jruby.util.ByteList#caseInsensitiveCmp.
        final int size = value.getLength();
        final int len = Math.min(size, other.getLength());

        for (int offset = -1; ++offset < len;) {
            int myCharIgnoreCase = AsciiTables.ToLowerCaseTable[value.get(offset) & 0xff] & 0xff;
            int otherCharIgnoreCase = AsciiTables.ToLowerCaseTable[other.get(offset) & 0xff] & 0xff;
            if (myCharIgnoreCase < otherCharIgnoreCase) {
                return -1;
            } else if (myCharIgnoreCase > otherCharIgnoreCase) {
                return 1;
            }
        }

        return size == other.getLength() ? 0 : size == len ? -1 : 1;
    }

    public static Rope ropeFromRopeBuilder(RopeBuilder builder) {
        return create(builder.getBytes(), builder.getEncoding(), CR_UNKNOWN);
    }

    public static boolean isAsciiOnly(byte[] bytes, Encoding encoding) {
        if (!encoding.isAsciiCompatible()) {
            return false;
        }

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] < 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInvalid(byte[] bytes, Encoding encoding) {
        final StringAttributes attributes = calculateCodeRangeAndLength(encoding, bytes, 0, bytes.length);

        return attributes.getCodeRange() == CR_BROKEN;
    }

    public static boolean anyChildContains(Rope rope, String value) {
        //        if (rope instanceof SubstringRope) {
        //            return anyChildContains(((SubstringRope) rope).getChild(), value);
        //        }
        // NOTE(norswap, 18 Dec 2020): We do not treat ConcatRopes specially: `decodeRope` will flatten them
        //   If we just search left and right, we risk missing the case where the value straddles the two children.
        //
        //   Because of the flattening, the references to the children ropes will be nulled, so we do not need
        //   to worry about the risk of retaining a substring rope whose child contains the value.
        return rope.byteLength() >= value.length() && RopeOperations.decodeRope(rope).contains(value);
    }

    public static String escape(Rope rope) {
        final StringBuilder builder = new StringBuilder();
        builder.append('"');

        for (int i = 0; i < rope.byteLength(); i++) {
            final byte character = rope.get(i);
            switch (character) {
                case '\\':
                    builder.append("\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                default:
                    if (character >= 32 && character <= 126) {
                        builder.append((char) character);
                    } else {
                        builder.append(StringUtils.format("\\x%02x", character));
                    }
                    break;
            }
        }

        builder.append('"');
        return builder.toString();
    }

}
