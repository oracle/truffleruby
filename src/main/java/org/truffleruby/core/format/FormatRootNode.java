/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format;

import java.util.List;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.RubyBaseRootNode;
import org.truffleruby.language.backtrace.InternalRootNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

/** The node at the root of a pack expression. */
public class FormatRootNode extends RubyBaseRootNode implements InternalRootNode {

    private final RubyLanguage language;
    private final FormatEncoding encoding;

    @Child private FormatNode child;

    @CompilationFinal private int expectedLength = 0;

    public FormatRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FormatEncoding encoding,
            FormatNode child) {
        super(language, FormatFrameDescriptor.FRAME_DESCRIPTOR, sourceSection);
        this.language = language;
        this.encoding = encoding;
        this.child = child;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(VirtualFrame frame) {
        frame.setObject(FormatFrameDescriptor.SOURCE_SLOT, frame.getArguments()[0]);
        frame.setInt(FormatFrameDescriptor.SOURCE_LENGTH_SLOT, (int) frame.getArguments()[1]);
        frame.setInt(FormatFrameDescriptor.SOURCE_POSITION_SLOT, 0);
        frame.setObject(FormatFrameDescriptor.OUTPUT_SLOT, new byte[expectedLength]);
        frame.setInt(FormatFrameDescriptor.OUTPUT_POSITION_SLOT, 0);
        frame.setInt(FormatFrameDescriptor.STRING_LENGTH_SLOT, 0);
        frame.setInt(FormatFrameDescriptor.STRING_CODE_RANGE_SLOT, CodeRange.CR_UNKNOWN.toInt());
        frame.setObject(FormatFrameDescriptor.ASSOCIATED_SLOT, null);

        child.execute(frame);

        final int outputLength;

        try {
            outputLength = frame.getInt(FormatFrameDescriptor.OUTPUT_POSITION_SLOT);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        if (outputLength > expectedLength) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            /* Don't over-compensate and allocate 2x or something like that for next time, as we have to copy the byte[]
             * at the end if it's too big to make it fit its contents. In the ideal case the byte[] is exactly the right
             * size. If we have to keep making it bigger in the slow-path, we can live with that. */

            expectedLength = outputLength;
        }

        final byte[] output;

        try {
            output = (byte[]) frame.getObject(FormatFrameDescriptor.OUTPUT_SLOT);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        final int stringLength;

        if (encoding == FormatEncoding.UTF_8) {
            try {
                stringLength = frame.getInt(FormatFrameDescriptor.STRING_LENGTH_SLOT);
            } catch (FrameSlotTypeException e) {
                throw new IllegalStateException(e);
            }
        } else {
            stringLength = outputLength;
        }

        final CodeRange stringCodeRange;

        try {
            stringCodeRange = CodeRange.fromInt(frame.getInt(FormatFrameDescriptor.STRING_CODE_RANGE_SLOT));
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        final List<Pointer> associated;

        try {
            associated = (List<Pointer>) frame.getObject(FormatFrameDescriptor.ASSOCIATED_SLOT);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        final Pointer[] associatedArray;

        if (associated != null) {
            associatedArray = associatedToArray(associated);
        } else {
            associatedArray = null;
        }

        return new BytesResult(output, outputLength, stringLength, stringCodeRange, encoding, associatedArray);
    }

    @TruffleBoundary
    private Pointer[] associatedToArray(List<Pointer> associated) {
        return associated.toArray(Pointer.EMPTY_ARRAY);
    }

    @Override
    public boolean isCloningAllowed() {
        return true;
    }

    @Override
    public String getName() {
        return "format";
    }

    @Override
    public String toString() {
        return getName();
    }
}
