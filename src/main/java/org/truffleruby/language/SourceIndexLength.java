/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public final class SourceIndexLength {

    public static final int UNAVAILABLE = -1;
    public static final SourceIndexLength UNAVAILABLE_POSITION = new SourceIndexLength();

    /** -1 indicates no source section, see {@link org.truffleruby.language.RubyNode#hasSource}. Never true for
     * SourceIndexLength. */
    private final int charIndex;
    /** -1 indicates unavailable source section, only for usages of UNAVAILABLE_POSITION and unavailable SourceSections
     * converted to SourceIndexLength. */
    private final int length;

    public static SourceIndexLength fromSourceSection(SourceSection sourceSection) {
        if (!sourceSection.isAvailable()) {
            return UNAVAILABLE_POSITION;
        }
        return new SourceIndexLength(sourceSection.getCharIndex(), sourceSection.getCharLength());
    }

    private SourceIndexLength() {
        this(0, UNAVAILABLE);
    }

    public SourceIndexLength(int charIndex, int length) {
        assert (charIndex >= 0 && length >= 0) || (charIndex == 0 && length == UNAVAILABLE);
        this.charIndex = charIndex;
        this.length = length;
    }

    @TruffleBoundary
    public SourceSection toSourceSection(Source source) {
        if (length == UNAVAILABLE) {
            return source.createUnavailableSection();
        } else {
            return source.createSection(charIndex, length);
        }
    }

    public boolean isAvailable() {
        return length != UNAVAILABLE;
    }

    public int getCharIndex() {
        return charIndex;
    }

    public int getLength() {
        return length;
    }

    public int getCharEnd() {
        return charIndex + length;
    }

}
