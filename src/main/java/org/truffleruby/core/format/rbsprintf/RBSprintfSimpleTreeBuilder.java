/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.rbsprintf;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.LiteralFormatNode;
import org.truffleruby.core.format.SharedTreeBuilder;
import org.truffleruby.core.format.convert.ToDoubleWithCoercionNodeGen;
import org.truffleruby.core.format.convert.ToIntegerNodeGen;
import org.truffleruby.core.format.convert.ToStringNodeGen;
import org.truffleruby.core.format.format.FormatCharacterNodeGen;
import org.truffleruby.core.format.format.FormatEFloatNodeGen;
import org.truffleruby.core.format.format.FormatFFloatNodeGen;
import org.truffleruby.core.format.format.FormatFloatNodeGen;
import org.truffleruby.core.format.format.FormatGFloatNodeGen;
import org.truffleruby.core.format.format.FormatIntegerBinaryNodeGen;
import org.truffleruby.core.format.format.FormatIntegerNodeGen;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.format.read.array.ReadArgumentIndexValueNodeGen;
import org.truffleruby.core.format.read.array.ReadIntegerNodeGen;
import org.truffleruby.core.format.read.array.ReadCStringNodeGen;
import org.truffleruby.core.format.read.array.ReadCValueNodeGen;
import org.truffleruby.core.format.read.array.ReadValueNodeGen;
import org.truffleruby.core.format.write.bytes.WriteBytesNodeGen;
import org.truffleruby.core.format.write.bytes.WritePaddedBytesNodeGen;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.RopeConstants;

public class RBSprintfSimpleTreeBuilder {

    private final RubyLanguage language;
    private final List<FormatNode> sequence = new ArrayList<>();
    private final List<RBSprintfConfig> configs;
    private final Object stringReader;

    public static final int DEFAULT = -1;

    private static final LeafRope EMPTY_ROPE = RopeConstants.EMPTY_US_ASCII_ROPE;

    public RBSprintfSimpleTreeBuilder(RubyLanguage language, List<RBSprintfConfig> configs, Object stringReader) {
        this.language = language;
        this.configs = configs;
        this.stringReader = stringReader;
    }

    private void buildTree() {
        for (RBSprintfConfig config : configs) {
            final FormatNode node;
            if (config.isLiteral()) {
                node = WriteBytesNodeGen.create(new LiteralFormatNode(config.getLiteralBytes()));
            } else {
                final FormatNode valueNode;

                if (config.getAbsoluteArgumentIndex() != null) {
                    valueNode = ReadArgumentIndexValueNodeGen
                            .create(config.getAbsoluteArgumentIndex(), new SourceNode());
                } else {
                    valueNode = ReadValueNodeGen.create(new SourceNode());
                }

                final FormatNode widthNode;
                if (config.isWidthStar()) {
                    widthNode = ReadIntegerNodeGen.create(new SourceNode());
                } else if (config.isArgWidth()) {
                    widthNode = ReadArgumentIndexValueNodeGen.create(config.getWidth(), new SourceNode());
                } else {
                    widthNode = new LiteralFormatNode(config.getWidth() == null ? -1 : config.getWidth());
                }

                final FormatNode precisionNode;
                if (config.isPrecisionStar()) {
                    precisionNode = ReadIntegerNodeGen.create(new SourceNode());
                } else if (config.isPrecisionArg()) {
                    precisionNode = ReadArgumentIndexValueNodeGen.create(config.getPrecision(), new SourceNode());
                } else {
                    precisionNode = new LiteralFormatNode(config.getPrecision() == null ? -1 : config.getPrecision());
                }


                switch (config.getFormatType()) {
                    case POINTER:
                    case INTEGER:
                        final char format;
                        switch (config.getFormat()) {
                            case 'b':
                            case 'B':
                            case 'p':
                            case 'x':
                            case 'X':
                                format = config.getFormat();
                                break;
                            case 'd':
                            case 'i':
                            case 'u':
                                format = 'd';
                                break;
                            case 'o':
                                format = 'o';
                                break;
                            default:
                                throw CompilerDirectives.shouldNotReachHere(String.valueOf(config.getFormat()));
                        }

                        switch (config.getFormat()) {
                            case 'b':
                            case 'B':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatIntegerBinaryNodeGen
                                                        .create(
                                                                format,
                                                                config.isPlus(),
                                                                config.isFsharp(),
                                                                config.isMinus(),
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToIntegerNodeGen.create(valueNode)));
                                break;
                            case 'p':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatIntegerNodeGen
                                                        .create(
                                                                format,
                                                                config.isPlus(),
                                                                config.isFsharp(),
                                                                config.isMinus(),
                                                                config.isHasSpace(),
                                                                true,
                                                                widthNode,
                                                                precisionNode,
                                                                ToIntegerNodeGen.create(valueNode)));
                                break;
                            default:
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatIntegerNodeGen
                                                        .create(
                                                                format,
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                config.isPlus(),
                                                                config.isMinus(),
                                                                config.isFsharp(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToIntegerNodeGen.create(valueNode)));
                        }
                        break;
                    case FLOAT:
                        switch (config.getFormat()) {
                            case 'a':
                            case 'A':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatFloatNodeGen
                                                        .create(
                                                                config.getFormat(),
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                config.isPlus(),
                                                                config.isMinus(),
                                                                config.isFsharp(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToDoubleWithCoercionNodeGen
                                                                        .create(
                                                                                valueNode)));
                                break;
                        case 'e':
                        case 'E':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatEFloatNodeGen
                                                        .create(
                                                                config.getFormat(),
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                config.isPlus(),
                                                                config.isMinus(),
                                                                config.isFsharp(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToDoubleWithCoercionNodeGen
                                                                        .create(
                                                                                valueNode)));
                                break;
                        case 'g':
                        case 'G':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatGFloatNodeGen
                                                        .create(
                                                                config.getFormat(),
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                config.isPlus(),
                                                                config.isMinus(),
                                                                config.isFsharp(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToDoubleWithCoercionNodeGen
                                                                        .create(
                                                                                valueNode)));
                                break;
                        case 'f':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatFFloatNodeGen
                                                        .create(
                                                                config.isHasSpace(),
                                                                config.isZero(),
                                                                config.isPlus(),
                                                                config.isMinus(),
                                                                config.isFsharp(),
                                                                widthNode,
                                                                precisionNode,
                                                                ToDoubleWithCoercionNodeGen
                                                                        .create(
                                                                                valueNode)));
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                        break;
                    case OTHER:
                        switch (config.getFormat()) {
                            case 'c':
                                node = WriteBytesNodeGen
                                        .create(
                                                FormatCharacterNodeGen
                                                        .create(
                                                                config.isMinus(),
                                                                widthNode,
                                                                valueNode));
                                break;
                            case 's':
                                final FormatNode conversionNode;

                                conversionNode = ReadCStringNodeGen.create(stringReader, valueNode);

                                if (config.getWidth() != null || config.isWidthStar() ||
                                        config.getPrecision() != null || config.isPrecisionStar()) {
                                    node = WritePaddedBytesNodeGen
                                            .create(config.isMinus(), widthNode, precisionNode, conversionNode);
                                } else {
                                    node = WriteBytesNodeGen.create(conversionNode);
                                }
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                        break;
                    case RUBY_VALUE: {
                        final String conversionMethodName = config.isPlus() ? "inspect" : "to_s";
                        final FormatNode conversionNode;
                        conversionNode = ToStringNodeGen
                                .create(
                                        true,
                                        conversionMethodName,
                                        false,
                                        EMPTY_ROPE,
                                        config.isPlus(),
                                        (config.getAbsoluteArgumentIndex() == null)
                                                ? (ReadCValueNodeGen
                                                        .create(ReadIntegerNodeGen.create(new SourceNode())))
                                                : ReadCValueNodeGen.create(valueNode));
                        if (config.getWidth() != null || config.isWidthStar() || config.getPrecision() != null ||
                                config.isPrecisionStar()) {
                            node = WritePaddedBytesNodeGen
                                    .create(config.isMinus(), widthNode, precisionNode, conversionNode);
                        } else {
                            node = WriteBytesNodeGen.create(conversionNode);
                        }
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException(
                                "unsupported type: " + config.getFormatType().toString());
                }

            }
            sequence.add(node);
        }


    }


    public FormatNode getNode() {
        buildTree();
        return SharedTreeBuilder.createSequence(sequence.toArray(FormatNode.EMPTY_ARRAY));
    }

}
