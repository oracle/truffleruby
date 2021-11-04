/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Objects;

import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.TStringWithEncoding;
import org.truffleruby.language.control.DeferredRaiseException;

public final class RegexpCacheKey {

    public static RegexpCacheKey calculate(TStringWithEncoding source, RegexpOptions options)
            throws DeferredRaiseException {
        RubyEncoding fixedEnc[] = new RubyEncoding[]{ null };
        RopeBuilder processed = ClassicRegexp.preprocess(source, source.getEncoding(), fixedEnc,
                RegexpSupport.ErrorMode.RAISE);
        RegexpOptions optionsArray[] = new RegexpOptions[]{ options };
        RubyEncoding enc = ClassicRegexp.computeRegexpEncoding(optionsArray, source.getEncoding(), fixedEnc);

        return new RegexpCacheKey(processed.toRope(), enc, optionsArray[0]);
    }

    public final Rope rope;
    public final RubyEncoding encoding;
    public final RegexpOptions options;

    private RegexpCacheKey(Rope rope, RubyEncoding encoding, RegexpOptions options) {
        assert !(rope instanceof NativeRope);
        this.rope = rope;
        this.encoding = encoding;
        this.options = options;
    }

    public RegexpOptions getOptions() {
        return options;
    }

    public Rope getRope() {
        return rope;
    }

    public RubyEncoding getEncoding() {
        return encoding;
    }

    public int getJoniOptions() {
        return options.toJoniOptions();
    }

    @Override
    public int hashCode() {
        return Objects.hash(rope, encoding, options);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RegexpCacheKey) {
            final RegexpCacheKey other = (RegexpCacheKey) o;
            return rope.equals(other.rope) && encoding == other.encoding && options.equals(other.options);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return '/' + RopeOperations.decodeOrEscapeBinaryRope(rope) + '/' +
                options.toOptionsString() +
                " -- " + RopeOperations.decodeOrEscapeBinaryRope(encoding.name.rope);
    }
}
