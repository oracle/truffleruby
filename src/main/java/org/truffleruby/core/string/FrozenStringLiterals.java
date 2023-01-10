/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: should rename to ImmutableStrings
public class FrozenStringLiterals {

    private static final List<ImmutableRubyString> STRINGS_TO_CACHE = new ArrayList<>();

    private final TStringCache tstringCache;
    private final WeakValueCache<TStringWithEncoding, ImmutableRubyString> values = new WeakValueCache<>();

    public FrozenStringLiterals(TStringCache tStringCache) {
        this.tstringCache = tStringCache;
        for (ImmutableRubyString name : STRINGS_TO_CACHE) {
            addFrozenStringToCache(name);
        }
    }

    @TruffleBoundary
    public ImmutableRubyString getFrozenStringLiteral(TruffleString tstring, RubyEncoding encoding) {
        if (tstring.isNative()) {
            throw CompilerDirectives.shouldNotReachHere();
        }

        return getFrozenStringLiteral(TStringUtils.getBytesOrCopy(tstring, encoding), encoding);
    }

    @TruffleBoundary
    public ImmutableRubyString getFrozenStringLiteral(byte[] bytes, RubyEncoding encoding) {
        // Ensure all ImmutableRubyString have a TruffleString from the TStringCache
        var cachedTString = tstringCache.getTString(bytes, encoding);
        var tstringWithEncoding = new TStringWithEncoding(cachedTString, encoding);

        final ImmutableRubyString string = values.get(tstringWithEncoding);
        if (string != null) {
            return string;
        } else {
            return values.addInCacheIfAbsent(tstringWithEncoding,
                    new ImmutableRubyString(cachedTString, encoding));
        }
    }

    public static ImmutableRubyString createStringAndCacheLater(TruffleString name,
            RubyEncoding encoding) {
        final ImmutableRubyString string = new ImmutableRubyString(name, encoding);
        assert !STRINGS_TO_CACHE.contains(string);
        STRINGS_TO_CACHE.add(string);
        return string;
    }

    private void addFrozenStringToCache(ImmutableRubyString string) {
        var encoding = string.getEncodingUncached();
        var cachedTString = tstringCache.getTString(string.tstring, encoding);
        assert cachedTString == string.tstring;
        var tstringWithEncoding = new TStringWithEncoding(cachedTString, encoding);
        final ImmutableRubyString existing = values.addInCacheIfAbsent(tstringWithEncoding, string);
        if (existing != string) {
            throw CompilerDirectives
                    .shouldNotReachHere("Duplicate ImmutableRubyString in FrozenStringLiterals: " + existing);
        }
    }

    @TruffleBoundary
    public Collection<ImmutableRubyString> allFrozenStrings() {
        return values.values();
    }

}
