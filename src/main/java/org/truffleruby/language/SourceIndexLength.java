/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
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

    private final int charIndex;
    private final int length; // -1 indicates unavailable, which is the same encoding as SourceSection

    private SourceIndexLength() {
        this(UNAVAILABLE, UNAVAILABLE);
    }

    public SourceIndexLength(int charIndex, int length) {
        this.charIndex = charIndex;
        this.length = length;
    }

    public SourceIndexLength(SourceSection sourceSection) {
        this(sourceSection.getCharIndex(), sourceSection.isAvailable() ? sourceSection.getCharLength() : UNAVAILABLE);
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
