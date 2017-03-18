/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * Contains code modified from JRuby's RubyConverter.java
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.Encoding;
import org.jcodings.Ptr;
import org.jcodings.transcode.EConv;
import org.jcodings.transcode.EConvResult;
import org.jcodings.transcode.Transcoder;
import org.jcodings.transcode.TranscodingManager;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.truffleruby.core.string.StringOperations.rope;

@CoreClass("Encoding::Converter")
public abstract class EncodingConverterNodes {

    @NonStandard
    @CoreMethod(names = "initialize_jruby", required = 2, optional = 1, lowerFixnum = 3, visibility = Visibility.PRIVATE)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubyEncoding(source)", "isRubyEncoding(destination)" })
        public DynamicObject initialize(DynamicObject self, DynamicObject source, DynamicObject destination, int options) {
            // Adapted from RubyConverter - see attribution there
            //
            // This method should only be called after the Encoding::Converter instance has already been initialized
            // by Rubinius.  Rubinius will do the heavy lifting of parsing the options hash and setting the `@options`
            // ivar to the resulting int for EConv flags.

            Encoding sourceEncoding = Layouts.ENCODING.getEncoding(source);
            Encoding destinationEncoding = Layouts.ENCODING.getEncoding(destination);

            final EConv econv = TranscodingManager.create(sourceEncoding, destinationEncoding, options);
            econv.sourceEncoding = sourceEncoding;
            econv.destinationEncoding = destinationEncoding;

            Layouts.ENCODING_CONVERTER.setEconv(self, econv);

            return nil();
        }

    }

    @NonStandard
    @CoreMethod(names = "each_transcoder", onSingleton = true, needsBlock = true)
    public abstract static class EachTranscoderNode extends YieldingCoreMethodNode {

        @Specialization
        public Object transcodingMap(VirtualFrame frame, DynamicObject block) {
            for (Map.Entry<String, Map<String, Transcoder>> sourceEntry : TranscodingManager.allTranscoders.entrySet()) {
                final DynamicObject source = getContext().getSymbolTable().getSymbol(sourceEntry.getKey());
                final int size = sourceEntry.getValue().size();
                final Object[] destinations = new Object[size];

                int i = 0;
                for (Map.Entry<String, Transcoder> destinationEntry : sourceEntry.getValue().entrySet()) {
                    destinations[i++] = getContext().getSymbolTable().getSymbol(destinationEntry.getKey());
                }

                yield(frame, block, source, createArray(destinations, size));
            }

            return nil();
        }
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            Object econv = null;
            return allocateNode.allocate(rubyClass, econv);
        }

    }

    @Primitive(name = "encoding_converter_primitive_convert")
    public static abstract class PrimitiveConvertNode extends PrimitiveArrayArgumentsNode {

        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();

        @Specialization(guards = {"isRubyString(source)", "isRubyString(target)", "isRubyHash(options)"})
        public Object encodingConverterPrimitiveConvert(DynamicObject encodingConverter, DynamicObject source,
                                                        DynamicObject target, int offset, int size, DynamicObject options) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Specialization(guards = {"isNil(source)", "isRubyString(target)"})
        public Object primitiveConvertNilSource(DynamicObject encodingConverter, DynamicObject source,
                                                DynamicObject target, int offset, int size, int options) {
            return primitiveConvertHelper(encodingConverter, source, target, offset, size, options);
        }

        @Specialization(guards = {"isRubyString(source)", "isRubyString(target)"})
        public Object encodingConverterPrimitiveConvert(DynamicObject encodingConverter, DynamicObject source,
                                                        DynamicObject target, int offset, int size, int options) {

            // Taken from org.jruby.RubyConverter#primitive_convert.

            return primitiveConvertHelper(encodingConverter, source, target, offset, size, options);
        }

        @TruffleBoundary
        private Object primitiveConvertHelper(DynamicObject encodingConverter, DynamicObject source,
                                              DynamicObject target, int offset, int size, int options) {
            // Taken from org.jruby.RubyConverter#primitive_convert.

            final boolean nonNullSource = source != nil();
            Rope sourceRope = nonNullSource ? rope(source) : RopeConstants.EMPTY_UTF8_ROPE;
            final Rope targetRope = rope(target);
            final RopeBuilder outBytes = RopeOperations.toByteListCopy(targetRope);

            final Ptr inPtr = new Ptr();
            final Ptr outPtr = new Ptr();

            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);

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
                            coreExceptions().argumentError("output offset too big", this)
                    );
                }

                long outputByteEnd = offset + size;

                if (outputByteEnd > Integer.MAX_VALUE) {
                    // overflow check
                    throw new RaiseException(
                            coreExceptions().argumentError("output offset + bytesize too big", this)
                    );
                }

                outBytes.unsafeEnsureSpace((int) outputByteEnd);

                inPtr.p = 0;
                outPtr.p = offset;
                int os = outPtr.p + size;
                EConvResult res = TranscodingManager.convert(ec, sourceRope.getBytes(), inPtr, sourceRope.byteLength() + inPtr.p, outBytes.getUnsafeBytes(), outPtr, os, options);

                outBytes.setLength(outPtr.p);

                if (nonNullSource) {
                    sourceRope = makeSubstringNode.executeMake(sourceRope, inPtr.p, sourceRope.byteLength() - inPtr.p);
                    StringOperations.setRope(source, sourceRope);
                }

                if (growOutputBuffer && res == EConvResult.DestinationBufferFull) {
                    if (Integer.MAX_VALUE / 2 < size) {
                        throw new RaiseException(
                                coreExceptions().argumentError("too long conversion result", this)
                        );
                    }
                    size *= 2;
                    continue;
                }

                if (ec.destinationEncoding != null) {
                    outBytes.setEncoding(ec.destinationEncoding);
                }

                StringOperations.setRope(target, RopeOperations.ropeFromByteList(outBytes));

                return getSymbol(res.symbolicName());
            }
        }

    }

    @Primitive(name = "encoding_converter_putback")
    public static abstract class EncodingConverterPutbackNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject encodingConverterPutback(DynamicObject encodingConverter, int maxBytes) {
            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);
            final int putbackable = ec.putbackable();

            return putback(encodingConverter, putbackable < maxBytes ? putbackable : maxBytes);
        }

        @Specialization
        public DynamicObject encodingConverterPutback(DynamicObject encodingConverter, NotProvided maxBytes) {
            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);

            return putback(encodingConverter, ec.putbackable());
        }

        private DynamicObject putback(DynamicObject encodingConverter, int n) {
            assert RubyGuards.isRubyEncodingConverter(encodingConverter);

            // Taken from org.jruby.RubyConverter#putback.

            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);

            final RopeBuilder bytes = RopeBuilder.createRopeBuilder(n);
            ec.putback(bytes.getUnsafeBytes(), 0, n);
            bytes.setLength(n);

            if (ec.sourceEncoding != null) {
                bytes.setEncoding(ec.sourceEncoding);
            }

            return createString(bytes);
        }
    }

    @Primitive(name = "encoding_converter_last_error")
    public static abstract class EncodingConverterLastErrorNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object encodingConverterLastError(DynamicObject encodingConverter) {
            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);
            final EConv.LastError lastError = ec.lastError;

            if (lastError.getResult() != EConvResult.InvalidByteSequence &&
                    lastError.getResult() != EConvResult.IncompleteInput &&
                    lastError.getResult() != EConvResult.UndefinedConversion) {
                return nil();
            }

            final boolean readAgain = lastError.getReadAgainLength() != 0;
            final int size = readAgain ? 5 : 4;
            final Object[] store = new Object[size];

            store[0] = eConvResultToSymbol(lastError.getResult());
            store[1] = createString(RopeBuilder.createRopeBuilder(lastError.getSource()));
            store[2] = createString(RopeBuilder.createRopeBuilder(lastError.getDestination()));
            store[3] = createString(RopeBuilder.createRopeBuilder(lastError.getErrorBytes(),
                    lastError.getErrorBytesP(), lastError.getErrorBytesP() + lastError.getErrorBytesLength()));

            if (readAgain) {
                store[4] = createString(RopeBuilder.createRopeBuilder(lastError.getErrorBytes(),
                    lastError.getErrorBytesLength() + lastError.getErrorBytesP(),
                    lastError.getReadAgainLength()));
            }

            return createArray(store, size);
        }

        private DynamicObject eConvResultToSymbol(EConvResult result) {
            switch(result) {
                case InvalidByteSequence: return getSymbol("invalid_byte_sequence");
                case UndefinedConversion: return getSymbol("undefined_conversion");
                case DestinationBufferFull: return getSymbol("destination_buffer_full");
                case SourceBufferEmpty: return getSymbol("source_buffer_empty");
                case Finished: return getSymbol("finished");
                case AfterOutput: return getSymbol("after_output");
                case IncompleteInput: return getSymbol("incomplete_input");
            }

            throw new UnsupportedOperationException(StringUtils.format("Unknown EConv result: %s", result));
        }

    }

    @Primitive(name = "encoding_converter_primitive_errinfo")
    public static abstract class EncodingConverterErrinfoNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object encodingConverterLastError(DynamicObject encodingConverter) {
            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);

            final Object[] ret = { getSymbol(ec.lastError.getResult().symbolicName()), nil(), nil(), nil(), nil() };

            if (ec.lastError.getSource() != null) {
                ret[1] = createString(RopeBuilder.createRopeBuilder(ec.lastError.getSource()));
            }

            if (ec.lastError.getDestination() != null) {
                ret[2] = createString(RopeBuilder.createRopeBuilder(ec.lastError.getDestination()));
            }

            if (ec.lastError.getErrorBytes() != null) {
                ret[3] = createString(RopeBuilder.createRopeBuilder(ec.lastError.getErrorBytes(), ec.lastError.getErrorBytesP(), ec.lastError.getErrorBytesLength()));
                ret[4] = createString(RopeBuilder.createRopeBuilder(ec.lastError.getErrorBytes(), ec.lastError.getErrorBytesP() + ec.lastError.getErrorBytesLength(), ec.lastError.getReadAgainLength()));
            }

            return createArray(ret, ret.length);
        }

    }

    @CoreMethod(names = "replacement")
    public abstract static class EncodingConverterReplacementNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject getReplacement(DynamicObject encodingConverter) {
            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);

            final int ret = ec.makeReplacement();
            if (ret == -1) {
                throw new RaiseException(getContext().getCoreExceptions().encodingUndefinedConversionError(this));
            }

            final byte[] bytes = ArrayUtils.extractRange(ec.replacementString, 0, ec.replacementLength);
            final String encodingName = new String(ec.replacementEncoding, StandardCharsets.US_ASCII);
            final DynamicObject encoding = getContext().getEncodingManager().getRubyEncoding(encodingName);

            return createString(bytes, Layouts.ENCODING.getEncoding(encoding));
        }

    }

    @CoreMethod(names = "replacement=", required = 1)
    @NodeChildren({
        @NodeChild(value = "encodingConverter", type = RubyNode.class),
        @NodeChild(value = "replacement", type = RubyNode.class)
    })
    public abstract static class EncodingConverterSetReplacementNode extends CoreMethodNode {

        @CreateCast("replacement")
        public RubyNode coerceReplacementToString(RubyNode replacement) {
            return ToStrNodeGen.create(replacement);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject setReplacement(DynamicObject encodingConverter, DynamicObject replacement) {
            final EConv ec = Layouts.ENCODING_CONVERTER.getEconv(encodingConverter);
            final Rope rope = StringOperations.rope(replacement);
            final Encoding encoding = rope.getEncoding();

            final int ret = ec.setReplacement(rope.getBytes(), 0, rope.byteLength(), encoding.getName());

            if (ret == -1) {
                throw new RaiseException(getContext().getCoreExceptions().encodingUndefinedConversionError(this));
            }

            return replacement;
        }

    }

}
