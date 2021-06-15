/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code modified from JRuby's RubyConverter.java
 */
package org.truffleruby.core.encoding;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import org.jcodings.Encoding;
import org.jcodings.Ptr;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.transcode.EConv;
import org.jcodings.transcode.EConvFlags;
import org.jcodings.transcode.EConvResult;
import org.jcodings.transcode.Transcoder;
import org.jcodings.transcode.TranscoderDB;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.EncodingUtils;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule(value = "Encoding::Converter", isClass = true)
public abstract class EncodingConverterNodes {

    @NonStandard
    @CoreMethod(
            names = "initialize_jcodings",
            required = 2,
            optional = 1,
            lowerFixnum = 3,
            visibility = Visibility.PRIVATE)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object initialize(
                RubyEncodingConverter self, RubyEncoding source, RubyEncoding destination, int options) {
            // Adapted from RubyConverter - see attribution there
            //
            // This method should only be called after the Encoding::Converter instance has already been initialized
            // by Rubinius.  Rubinius will do the heavy lifting of parsing the options hash and setting the `@options`
            // ivar to the resulting int for EConv flags.

            Encoding sourceEncoding = source.encoding;
            Encoding destinationEncoding = destination.encoding;

            final EConv econv = TranscoderDB
                    .open(sourceEncoding.getName(), destinationEncoding.getName(), toJCodingFlags(options));

            if (econv == null) {
                return nil;
            }

            econv.sourceEncoding = sourceEncoding;
            econv.destinationEncoding = destinationEncoding;

            self.econv = econv;

            // There are N-1 edges connecting N encodings on the path from source -> destination.
            // We need to include every encoding along the path in the return value.
            Object[] ret = new Object[econv.numTranscoders + 1];

            int retIndex = 0;
            for (int i = 0; i < econv.numTranscoders; i++) {
                final Transcoder transcoder = econv.elements[i].transcoding.transcoder;

                if (EncodingUtils.DECORATOR_P(transcoder.getSource(), transcoder.getDestination())) {
                    continue;
                }

                final byte[] segmentSource = transcoder.getSource();
                ret[retIndex++] = getSymbol(StringUtils.toUpperCase(RopeOperations.decodeAscii(segmentSource)));
            }

            final int retSize = retIndex + 1;
            if (retSize != ret.length) {
                // The decorated entry really isn't part of the transcoding path, but jcodings treats it as if it were,
                // so we need to reduce the returned array size accordingly.
                ret = ArrayUtils.extractRange(ret, 0, retSize);
            }

            final byte[] destinationName = destinationEncoding.getName();
            ret[retIndex] = getSymbol(StringUtils.toUpperCase(RopeOperations.decodeAscii(destinationName)));

            return createArray(ret);
        }

        /** We and JCodings process Encoding::Converter options flags differently. We split the processing between
         * initial setup and the replacement value setup, whereas JCodings handles them all during initial setup. We
         * figure out what flags JCodings additionally expects to be set and set them to satisfy EConv. */
        private int toJCodingFlags(int flags) {
            if ((flags & EConvFlags.XML_TEXT_DECORATOR) != 0) {
                flags |= EConvFlags.UNDEF_HEX_CHARREF;
            }

            if ((flags & EConvFlags.XML_ATTR_CONTENT_DECORATOR) != 0) {
                flags |= EConvFlags.UNDEF_HEX_CHARREF;
            }

            return flags;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyEncodingConverter allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().encodingConverterShape;
            final RubyEncodingConverter instance = new RubyEncodingConverter(rubyClass, shape, null);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @Primitive(name = "encoding_transcoders_from_encoding")
    public abstract static class TranscodersFromEncodingNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object search(RubySymbol source) {
            final Set<String> transcoders = TranscodingManager.allDirectTranscoderPaths.get(source.getString());
            if (transcoders == null) {
                return nil;
            }

            final Object[] destinations = new Object[transcoders.size()];
            int i = 0;

            for (String transcoder : transcoders) {
                destinations[i++] = getSymbol(transcoder);
            }

            return createArray(destinations);
        }

    }

    @Primitive(name = "encoding_converter_primitive_convert", lowerFixnum = { 3, 4, 5 })
    public abstract static class PrimitiveConvertNode extends PrimitiveArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        @TruffleBoundary
        @Specialization(guards = "stringsSource.isRubyString(source)")
        protected Object encodingConverterPrimitiveConvert(
                RubyEncodingConverter encodingConverter,
                Object source,
                RubyString target,
                int offset,
                int size,
                RubyHash options,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsSource) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Specialization
        protected Object primitiveConvertNilSource(
                RubyEncodingConverter encodingConverter,
                Nil source,
                RubyString target,
                int offset,
                int size,
                int options,
                @Cached DispatchNode destinationEncodingNode) {
            return primitiveConvertHelper(
                    encodingConverter,
                    source,
                    RopeConstants.EMPTY_UTF8_ROPE,
                    target,
                    offset,
                    size,
                    options,
                    destinationEncodingNode);
        }

        @Specialization(guards = "stringsSource.isRubyString(source)")
        protected Object encodingConverterPrimitiveConvert(
                RubyEncodingConverter encodingConverter,
                Object source,
                RubyString target,
                int offset,
                int size,
                int options,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsSource,
                @Cached DispatchNode destinationEncodingNode) {

            // Taken from org.jruby.RubyConverter#primitive_convert.

            return primitiveConvertHelper(
                    encodingConverter,
                    source,
                    stringsSource.getRope(source),
                    target,
                    offset,
                    size,
                    options,
                    destinationEncodingNode);
        }

        @TruffleBoundary
        private Object primitiveConvertHelper(RubyEncodingConverter encodingConverter, Object source, Rope sourceRope,
                RubyString target, int offset, int size, int options, DispatchNode destinationEncodingNode) {
            // Taken from org.jruby.RubyConverter#primitive_convert.

            Rope targetRope = target.rope;
            final boolean nonNullSource = source != nil;
            final RopeBuilder outBytes = RopeOperations.toRopeBuilderCopy(targetRope);

            final Ptr inPtr = new Ptr();
            final Ptr outPtr = new Ptr();

            final EConv ec = encodingConverter.econv;

            final boolean changeOffset = (offset == 0);
            final boolean growOutputBuffer = (size == -1);

            if (size == -1) {
                size = 16; // in MRI, this is RSTRING_EMBED_LEN_MAX

                if (nonNullSource) {
                    if (size < sourceRope.byteLength()) {
                        size = sourceRope.byteLength();
                    }
                }
            }

            while (true) {

                if (changeOffset) {
                    offset = outBytes.getLength();
                }

                if (outBytes.getLength() < offset) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError("output offset too big", this));
                }

                long outputByteEnd = offset + size;

                if (outputByteEnd > Integer.MAX_VALUE) {
                    // overflow check
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError("output offset + bytesize too big", this));
                }

                outBytes.unsafeEnsureSpace((int) outputByteEnd);

                inPtr.p = 0;
                outPtr.p = offset;
                int os = outPtr.p + size;
                EConvResult res = convert(
                        ec,
                        sourceRope.getBytes(),
                        inPtr,
                        sourceRope.byteLength() + inPtr.p,
                        outBytes.getUnsafeBytes(),
                        outPtr,
                        os,
                        options);

                outBytes.setLength(outPtr.p);

                if (nonNullSource) {
                    sourceRope = substringNode.executeSubstring(sourceRope, inPtr.p, sourceRope.byteLength() - inPtr.p);
                    ((RubyString) source).setRope(sourceRope);
                }

                if (growOutputBuffer && res == EConvResult.DestinationBufferFull) {
                    if (Integer.MAX_VALUE / 2 < size) {
                        throw new RaiseException(
                                getContext(),
                                coreExceptions().argumentError("too long conversion result", this));
                    }
                    size *= 2;
                    continue;
                }

                if (ec.destinationEncoding != null) {
                    outBytes.setEncoding(ec.destinationEncoding);
                }

                target.setRope(RopeOperations.ropeFromRopeBuilder(outBytes));
                target.setEncoding(
                        (RubyEncoding) destinationEncodingNode.call(encodingConverter, "destination_encoding"));

                return getSymbol(res.symbolicName());
            }
        }

        @TruffleBoundary
        private EConvResult convert(EConv ec, byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop,
                int flags) {
            return ec.convert(in, inPtr, inStop, out, outPtr, outStop, flags);
        }

    }

    @CoreMethod(names = "putback", optional = 1, lowerFixnum = 1)
    public abstract static class EncodingConverterPutbackNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString encodingConverterPutback(RubyEncodingConverter encodingConverter, int maxBytes) {
            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = encodingConverter.econv;
            final int putbackable = ec.putbackable();

            return putback(encodingConverter, putbackable < maxBytes ? putbackable : maxBytes);
        }

        @Specialization
        protected RubyString encodingConverterPutback(RubyEncodingConverter encodingConverter, NotProvided maxBytes) {
            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = encodingConverter.econv;

            return putback(encodingConverter, ec.putbackable());
        }

        private RubyString putback(RubyEncodingConverter encodingConverter, int n) {

            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = encodingConverter.econv;

            final byte[] bytes = new byte[n];
            ec.putback(bytes, 0, n);

            final Encoding encoding = ec.sourceEncoding != null ? ec.sourceEncoding : ASCIIEncoding.INSTANCE;

            return makeStringNode.executeMake(bytes, encoding, CodeRange.CR_UNKNOWN);
        }
    }

    @Primitive(name = "encoding_converter_last_error")
    public abstract static class EncodingConverterLastErrorNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object encodingConverterLastError(RubyEncodingConverter encodingConverter,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final EConv ec = encodingConverter.econv;
            final EConv.LastError lastError = ec.lastError;

            if (lastError.getResult() != EConvResult.InvalidByteSequence &&
                    lastError.getResult() != EConvResult.IncompleteInput &&
                    lastError.getResult() != EConvResult.UndefinedConversion) {
                return nil;
            }

            final boolean readAgain = lastError.getReadAgainLength() != 0;
            final int size = readAgain ? 5 : 4;
            final Object[] store = new Object[size];

            store[0] = eConvResultToSymbol(lastError.getResult());
            store[1] = makeStringNode.executeMake(lastError.getSource(), ASCIIEncoding.INSTANCE, CR_UNKNOWN);
            store[2] = makeStringNode.executeMake(lastError.getDestination(), ASCIIEncoding.INSTANCE, CR_UNKNOWN);
            store[3] = makeStringNode.fromBuilderUnsafe(RopeBuilder.createRopeBuilder(
                    lastError.getErrorBytes(),
                    lastError.getErrorBytesP(),
                    lastError.getErrorBytesP() + lastError.getErrorBytesLength()), CR_UNKNOWN);

            if (readAgain) {
                store[4] = makeStringNode.fromBuilderUnsafe(RopeBuilder.createRopeBuilder(
                        lastError.getErrorBytes(),
                        lastError.getErrorBytesLength() + lastError.getErrorBytesP(),
                        lastError.getReadAgainLength()), CR_UNKNOWN);
            }

            return createArray(store);
        }

        private RubySymbol eConvResultToSymbol(EConvResult result) {
            switch (result) {
                case InvalidByteSequence:
                    return getSymbol("invalid_byte_sequence");
                case UndefinedConversion:
                    return getSymbol("undefined_conversion");
                case DestinationBufferFull:
                    return getSymbol("destination_buffer_full");
                case SourceBufferEmpty:
                    return getSymbol("source_buffer_empty");
                case Finished:
                    return getSymbol("finished");
                case AfterOutput:
                    return getSymbol("after_output");
                case IncompleteInput:
                    return getSymbol("incomplete_input");
            }

            throw new UnsupportedOperationException(StringUtils.format("Unknown EConv result: %s", result));
        }

    }

    @CoreMethod(names = "primitive_errinfo")
    public abstract static class EncodingConverterErrinfoNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray encodingConverterLastError(RubyEncodingConverter encodingConverter,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final EConv ec = encodingConverter.econv;

            final Object[] ret = { getSymbol(ec.lastError.getResult().symbolicName()), nil, nil, nil, nil };

            if (ec.lastError.getSource() != null) {
                ret[1] = makeStringNode.executeMake(ec.lastError.getSource(), ASCIIEncoding.INSTANCE, CR_UNKNOWN);
            }

            if (ec.lastError.getDestination() != null) {
                ret[2] = makeStringNode.executeMake(ec.lastError.getDestination(), ASCIIEncoding.INSTANCE, CR_UNKNOWN);
            }

            if (ec.lastError.getErrorBytes() != null) {
                ret[3] = makeStringNode
                        .fromBuilderUnsafe(
                                RopeBuilder.createRopeBuilder(
                                        ec.lastError.getErrorBytes(),
                                        ec.lastError.getErrorBytesP(),
                                        ec.lastError.getErrorBytesLength()),
                                CR_UNKNOWN);
                ret[4] = makeStringNode.fromBuilderUnsafe(
                        RopeBuilder.createRopeBuilder(
                                ec.lastError.getErrorBytes(),
                                ec.lastError.getErrorBytesP() + ec.lastError.getErrorBytesLength(),
                                ec.lastError.getReadAgainLength()),
                        CR_UNKNOWN);
            }

            return createArray(ret);
        }

    }

    @CoreMethod(names = "replacement")
    public abstract static class EncodingConverterReplacementNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString getReplacement(RubyEncodingConverter encodingConverter) {
            final EConv ec = encodingConverter.econv;

            final int ret = ec.makeReplacement();
            if (ret == -1) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().encodingUndefinedConversionError(this));
            }

            final byte[] bytes = ArrayUtils.extractRange(ec.replacementString, 0, ec.replacementLength);
            final String encodingName = new String(ec.replacementEncoding, StandardCharsets.US_ASCII);
            final RubyEncoding encoding = getContext().getEncodingManager().getRubyEncoding(encodingName);

            return makeStringNode.executeMake(bytes, encoding.encoding, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "replacement=", required = 1)
    @NodeChild(value = "encodingConverter", type = RubyNode.class)
    @NodeChild(value = "replacement", type = RubyBaseNodeWithExecute.class)
    public abstract static class EncodingConverterSetReplacementNode extends CoreMethodNode {

        @CreateCast("replacement")
        protected ToStrNode coerceReplacementToString(RubyBaseNodeWithExecute replacement) {
            return ToStrNodeGen.create(replacement);
        }

        @Specialization(guards = "libReplacement.isRubyString(replacement)")
        protected Object setReplacement(RubyEncodingConverter encodingConverter, Object replacement,
                @Cached BranchProfile errorProfile,
                @Cached RopeNodes.BytesNode bytesNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libReplacement) {
            final EConv ec = encodingConverter.econv;
            final Rope rope = libReplacement.getRope(replacement);
            final Encoding encoding = rope.getEncoding();

            final int ret = setReplacement(ec, bytesNode.execute(rope), rope.byteLength(), encoding.getName());

            if (ret == -1) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().encodingUndefinedConversionError(this));
            }

            return replacement;
        }

        @TruffleBoundary
        private int setReplacement(EConv ec, byte[] string, int len, byte[] encodingName) {
            return ec.setReplacement(string, 0, len, encodingName);
        }

    }

}
