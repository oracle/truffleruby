/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class TStringCache {

    private final WeakValueCache<TBytesKey, TruffleString> bytesToTString = new WeakValueCache<>();

    private int byteArrayReusedCount;
    private int tstringsReusedCount;
    private int tstringBytesSaved;

    public TStringCache(CoreSymbols coreSymbols) {
        addTStringConstants();
        addCoreSymbolTStrings(coreSymbols);
        addFrozenStrings();
    }

    private void addFrozenStrings() {
        for (var tstring : FrozenStrings.TSTRINGS) {
            register(tstring, Encodings.BINARY);
        }
    }

    private void addTStringConstants() {
        for (var tstring : TStringConstants.UTF8_SINGLE_BYTE_TSTRINGS) {
            register(tstring, Encodings.UTF_8);
        }
        for (var tstring : TStringConstants.US_ASCII_SINGLE_BYTE_TSTRINGS) {
            register(tstring, Encodings.US_ASCII);
        }
        for (var tstring : TStringConstants.BINARY_SINGLE_BYTE_TSTRINGS) {
            register(tstring, Encodings.BINARY);
        }
        for (var tstring : TStringConstants.TSTRING_CONSTANTS.values()) {
            register(tstring, Encodings.US_ASCII);
        }
    }

    private void addCoreSymbolTStrings(CoreSymbols coreSymbols) {
        for (RubySymbol symbol : coreSymbols.CORE_SYMBOLS) {
            register(symbol.tstring, symbol.encoding);
        }
    }

    private void register(TruffleString tstring, RubyEncoding encoding) {
        final TBytesKey key = new TBytesKey(TStringUtils.getBytesOrFail(tstring, encoding), encoding);
        final TruffleString existing = bytesToTString.put(key, tstring);
        if (existing != null && existing != tstring) {
            throw CompilerDirectives.shouldNotReachHere("Duplicate TruffleString in TStringCache: " + existing);
        }
    }

    public TruffleString getTString(TruffleString string, RubyEncoding encoding) {
        return getTString(TStringUtils.getBytesOrCopy(string, encoding), encoding);
    }

    @TruffleBoundary
    public TruffleString getTString(byte[] bytes, RubyEncoding rubyEncoding) {
        assert rubyEncoding != null;

        final TBytesKey key = new TBytesKey(bytes, rubyEncoding);

        final TruffleString tstring = bytesToTString.get(key);
        if (tstring != null) {
            ++tstringsReusedCount;
            tstringBytesSaved += tstring.byteLength(rubyEncoding.tencoding);

            return tstring;
        }

        // At this point, we were unable to find a TruffleString with the same bytes and encoding (i.e., a direct match).
        // However, there may still be a TruffleString with the same byte[] and sharing a direct byte[] can still allow some
        // reference equality optimizations. So, do another search but with a marker encoding. The only guarantee
        // we can make about the resulting TruffleString is that it would have the same logical byte[], but that's good enough
        // for our purposes.
        TBytesKey keyNoEncoding = new TBytesKey(bytes, null);
        final TruffleString tstringWithSameBytesButDifferentEncoding = bytesToTString.get(keyNoEncoding);

        final TruffleString newTString;
        if (tstringWithSameBytesButDifferentEncoding != null) {
            var prevEncoding = keyNoEncoding.getMatchedEncoding().tencoding;
            newTString = tstringWithSameBytesButDifferentEncoding.forceEncodingUncached(prevEncoding,
                    rubyEncoding.tencoding);

            ++byteArrayReusedCount;
            tstringBytesSaved += newTString.byteLength(rubyEncoding.tencoding);
        } else {
            newTString = TStringUtils.fromByteArray(bytes, rubyEncoding);
        }

        // Use the new TruffleString bytes in the cache, so we do not keep bytes alive unnecessarily.
        final TBytesKey newKey = new TBytesKey(TStringUtils.getBytesOrCopy(newTString, rubyEncoding), rubyEncoding);
        return bytesToTString.addInCacheIfAbsent(newKey, newTString);
    }

    public boolean contains(TruffleString string, RubyEncoding encoding) {
        final TBytesKey key = new TBytesKey(TStringUtils.getBytesOrCopy(string, encoding), encoding);

        return bytesToTString.get(key) != null;
    }

    public int getByteArrayReusedCount() {
        return byteArrayReusedCount;
    }

    public int getTStringsReusedCount() {
        return tstringsReusedCount;
    }

    public int getTStringBytesSaved() {
        return tstringBytesSaved;
    }

    public int totalTStrings() {
        return bytesToTString.size();
    }

}
