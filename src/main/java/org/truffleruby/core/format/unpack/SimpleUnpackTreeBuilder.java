/*
 * Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.unpack;

import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.SharedTreeBuilder;
import org.truffleruby.core.format.control.AdvanceSourcePositionNode;
import org.truffleruby.core.format.control.ReverseSourcePositionNode;
import org.truffleruby.core.format.control.SetSourcePositionNode;
import org.truffleruby.core.format.convert.BytesToInteger16BigNodeGen;
import org.truffleruby.core.format.convert.BytesToInteger16LittleNodeGen;
import org.truffleruby.core.format.convert.BytesToInteger32BigNodeGen;
import org.truffleruby.core.format.convert.BytesToInteger32LittleNodeGen;
import org.truffleruby.core.format.convert.BytesToInteger64BigNodeGen;
import org.truffleruby.core.format.convert.BytesToInteger64LittleNodeGen;
import org.truffleruby.core.format.convert.ReinterpretByteAsIntegerNodeGen;
import org.truffleruby.core.format.convert.ReinterpretIntegerAsFloatNodeGen;
import org.truffleruby.core.format.convert.ReinterpretLongAsDoubleNodeGen;
import org.truffleruby.core.format.pack.SimplePackListener;
import org.truffleruby.core.format.pack.SimplePackParser;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.format.read.bytes.ReadBERNodeGen;
import org.truffleruby.core.format.read.bytes.ReadBase64StringNodeGen;
import org.truffleruby.core.format.read.bytes.ReadBinaryStringNodeGen;
import org.truffleruby.core.format.read.bytes.ReadBitStringNodeGen;
import org.truffleruby.core.format.read.bytes.ReadByteNodeGen;
import org.truffleruby.core.format.read.bytes.ReadBytesNodeGen;
import org.truffleruby.core.format.read.bytes.ReadHexStringNodeGen;
import org.truffleruby.core.format.read.bytes.ReadMIMEStringNodeGen;
import org.truffleruby.core.format.read.bytes.ReadStringPointerNodeGen;
import org.truffleruby.core.format.read.bytes.ReadUTF8CharacterNodeGen;
import org.truffleruby.core.format.read.bytes.ReadUUStringNodeGen;
import org.truffleruby.core.format.write.OutputNode;
import org.truffleruby.core.format.write.array.WriteValueNodeGen;
import org.truffleruby.language.control.DeferredRaiseException;

import com.oracle.truffle.api.nodes.Node;

public final class SimpleUnpackTreeBuilder implements SimplePackListener {

    private final Node currentNode;

    private final SharedTreeBuilder sharedTreeBuilder;

    private final Deque<List<FormatNode>> sequenceStack = new ArrayDeque<>();

    public SimpleUnpackTreeBuilder(RubyLanguage language, Node currentNode) {
        this.currentNode = currentNode;
        sharedTreeBuilder = new SharedTreeBuilder(language);
        pushSequence();
    }

    public void enterSequence() {
        pushSequence();
    }

    public void exitSequence() {
        final List<FormatNode> sequence = sequenceStack.pop();
        appendNode(SharedTreeBuilder.createSequence(sequence.toArray(FormatNode.EMPTY_ARRAY)));
    }

    @Override
    public void integer(int size, boolean signed, ByteOrder byteOrder, int count) {
        if (size == 8) {
            if (signed) {
                appendNode(sharedTreeBuilder.applyCount(
                        count,
                        WriteValueNodeGen.create(
                                new OutputNode(),
                                ReinterpretByteAsIntegerNodeGen.create(
                                        true,
                                        ReadByteNodeGen.create(new SourceNode())))));
            } else {
                appendNode(sharedTreeBuilder.applyCount(
                        count,
                        WriteValueNodeGen.create(
                                new OutputNode(),
                                ReinterpretByteAsIntegerNodeGen.create(
                                        false,
                                        ReadByteNodeGen.create(new SourceNode())))));
            }
        } else {
            appendIntegerNode(size, byteOrder, count, signed);
        }
    }

    @Override
    public void floatingPoint(int size, ByteOrder byteOrder, int count) {
        appendFloatNode(size, byteOrder, count);
    }

    @Override
    public void utf8Character(int count) {
        appendNode(sharedTreeBuilder.applyCount(
                count,
                WriteValueNodeGen.create(
                        new OutputNode(),
                        ReadUTF8CharacterNodeGen.create(new SourceNode()))));
    }

    @Override
    public void berInteger(int count) {
        appendNode(sharedTreeBuilder.applyCount(
                count,
                WriteValueNodeGen.create(
                        new OutputNode(),
                        ReadBERNodeGen.create(new SourceNode()))));
    }

    @Override
    public void binaryStringSpacePadded(int count) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (count == SimplePackParser.COUNT_NONE) {
            readNode = ReadBinaryStringNodeGen.create(false, false, 1, true, true, false, source);
        } else if (count == SimplePackParser.COUNT_STAR) {
            readNode = ReadBinaryStringNodeGen.create(true, false, -1, true, true, false, source);
        } else {
            readNode = ReadBinaryStringNodeGen.create(false, false, count, true, true, false, source);
        }

        appendNode(WriteValueNodeGen.create(new OutputNode(), readNode));
    }

    @Override
    public void binaryStringNullPadded(int count) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (count == SimplePackParser.COUNT_NONE) {
            readNode = ReadBinaryStringNodeGen.create(false, false, 1, false, false, false, source);
        } else if (count == SimplePackParser.COUNT_STAR) {
            readNode = ReadBinaryStringNodeGen.create(true, false, -1, false, false, false, source);
        } else {
            readNode = ReadBinaryStringNodeGen.create(false, false, count, false, false, false, source);
        }

        appendNode(WriteValueNodeGen.create(new OutputNode(), readNode));
    }

    @Override
    public void binaryStringNullStar(int count) {
        final SourceNode source = new SourceNode();
        final FormatNode readNode;

        if (count == SimplePackParser.COUNT_NONE) {
            readNode = ReadBinaryStringNodeGen.create(false, true, 1, false, true, true, source);
        } else if (count == SimplePackParser.COUNT_STAR) {
            readNode = ReadBinaryStringNodeGen.create(true, true, -1, false, true, true, source);
        } else {
            readNode = ReadBinaryStringNodeGen.create(false, false, count, false, true, true, source);
        }

        appendNode(WriteValueNodeGen.create(new OutputNode(), readNode));
    }

    @Override
    public void bitStringMSBFirst(int count) {
        bitString(ByteOrder.BIG_ENDIAN, count);
    }

    @Override
    public void bitStringMSBLast(int count) {
        bitString(ByteOrder.LITTLE_ENDIAN, count);
    }

    @Override
    public void hexStringHighFirst(int count) {
        hexString(ByteOrder.BIG_ENDIAN, count);
    }

    @Override
    public void hexStringLowFirst(int count) {
        hexString(ByteOrder.LITTLE_ENDIAN, count);
    }

    @Override
    public void uuString(int count) {
        appendNode(
                WriteValueNodeGen.create(
                        new OutputNode(),
                        ReadUUStringNodeGen.create(new SourceNode())));
    }

    @Override
    public void mimeString(int count) {
        appendNode(WriteValueNodeGen.create(
                new OutputNode(),
                ReadMIMEStringNodeGen.create(new SourceNode())));
    }

    @Override
    public void base64String(int count) {
        appendNode(WriteValueNodeGen.create(
                new OutputNode(),
                ReadBase64StringNodeGen.create(count, new SourceNode())));
    }

    @Override
    public void pointer(int count, int limit) {
        appendNode(sharedTreeBuilder.applyCount(
                count,
                WriteValueNodeGen.create(
                        new OutputNode(),
                        ReadStringPointerNodeGen.create(
                                limit,
                                readBytesAsInteger(64, ByteOrder.nativeOrder(), false, false)))));
    }

    @Override
    public void at(int position) {
        if (position == SimplePackParser.COUNT_NONE) {
            position = 0;
        } else if (position == SimplePackParser.COUNT_STAR) {
            return;
        }

        appendNode(new SetSourcePositionNode(position));
    }

    @Override
    public void back(int count) {
        if (count == SimplePackParser.COUNT_STAR) {
            appendNode(new ReverseSourcePositionNode(true));
        } else if (count == 1) {
            appendNode(new ReverseSourcePositionNode(false));
        } else {
            appendNode(sharedTreeBuilder.applyCount(count, new ReverseSourcePositionNode(false)));
        }
    }

    @Override
    public void nullByte(int count) {
        if (count == SimplePackParser.COUNT_STAR) {
            appendNode(new AdvanceSourcePositionNode(true));
        } else if (count == 1) {
            appendNode(new AdvanceSourcePositionNode(false));
        } else {
            appendNode(sharedTreeBuilder.applyCount(count, new AdvanceSourcePositionNode(false)));
        }
    }

    @Override
    public void startSubSequence() {
        pushSequence();
    }

    @Override
    public void finishSubSequence(int count) {
        appendNode(sharedTreeBuilder.finishSubSequence(sequenceStack, count));
    }

    @Override
    public void error(String message) throws DeferredRaiseException {
        // TODO CS 29-Oct-16 make this a node so that side effects from previous directives happen
        throw new DeferredRaiseException(c -> c.getCoreExceptions().argumentError(message, currentNode));
    }

    @Override
    public void warn(String message) {
        // Please implement this method in case it's needed.
        // The `warn` method is needed in the SimplePackParser class and consequantly was added to the SimplePackListener interface.
        throw CompilerDirectives.shouldNotReachHere();
    }

    @Override
    public String packListenerMode() {
        return "unpack";
    }

    public FormatNode getNode() {
        return sequenceStack.peek().get(0);
    }

    private void pushSequence() {
        sequenceStack.push(new ArrayList<>());
    }

    private void appendNode(FormatNode node) {
        sequenceStack.peek().add(node);
    }

    private boolean consumePartial(int count) {
        return count == SimplePackParser.COUNT_STAR;
    }

    private void appendIntegerNode(int size, ByteOrder byteOrder, int count, boolean signed) {
        final FormatNode readNode = ReadBytesNodeGen.create(size / 8, consumePartial(count), new SourceNode());
        final FormatNode convertNode = createIntegerDecodeNode(size, byteOrder, signed, readNode);
        appendNode(sharedTreeBuilder.applyCount(count, WriteValueNodeGen.create(new OutputNode(), convertNode)));
    }

    private void appendFloatNode(int size, ByteOrder byteOrder, int count) {
        final FormatNode readNode = readBytesAsInteger(size, byteOrder, consumePartial(count), true);
        final FormatNode decodeNode;

        switch (size) {
            case 32:
                decodeNode = ReinterpretIntegerAsFloatNodeGen.create(readNode);
                break;
            case 64:
                decodeNode = ReinterpretLongAsDoubleNodeGen.create(readNode);
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }

        final FormatNode writeNode = WriteValueNodeGen.create(new OutputNode(), decodeNode);
        appendNode(sharedTreeBuilder.applyCount(count, writeNode));
    }

    private FormatNode readBytesAsInteger(int size, ByteOrder byteOrder, boolean consumePartial, boolean signed) {
        final FormatNode readNode = ReadBytesNodeGen.create(size / 8, consumePartial, new SourceNode());
        return createIntegerDecodeNode(size, byteOrder, signed, readNode);
    }

    private FormatNode createIntegerDecodeNode(int size, ByteOrder byteOrder, boolean signed, FormatNode readNode) {
        FormatNode decodeNode;

        switch (size) {
            case 16:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = BytesToInteger16LittleNodeGen.create(signed, readNode);
                } else {
                    decodeNode = BytesToInteger16BigNodeGen.create(signed, readNode);
                }
                break;
            case 32:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = BytesToInteger32LittleNodeGen.create(signed, readNode);
                } else {
                    decodeNode = BytesToInteger32BigNodeGen.create(signed, readNode);
                }
                break;
            case 64:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    decodeNode = BytesToInteger64LittleNodeGen.create(signed, readNode);
                } else {
                    decodeNode = BytesToInteger64BigNodeGen.create(signed, readNode);
                }
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }

        return decodeNode;
    }

    private void bitString(ByteOrder byteOrder, int count) {
        final SharedTreeBuilder.StarLength starLength = sharedTreeBuilder.parseCountContext(count);

        appendNode(WriteValueNodeGen.create(
                new OutputNode(),
                ReadBitStringNodeGen.create(
                        byteOrder,
                        starLength.isStar(),
                        starLength.getLength(),
                        new SourceNode())));
    }

    private void hexString(ByteOrder byteOrder, int count) {
        final SharedTreeBuilder.StarLength starLength = sharedTreeBuilder.parseCountContext(count);

        appendNode(WriteValueNodeGen.create(
                new OutputNode(),
                ReadHexStringNodeGen.create(
                        byteOrder,
                        starLength.isStar(),
                        starLength.getLength(),
                        new SourceNode())));

    }

}
