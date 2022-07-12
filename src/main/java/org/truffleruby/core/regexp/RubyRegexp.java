/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

import com.oracle.truffle.api.strings.TruffleString;
import org.joni.Regex;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.ImmutableRubyObjectNotCopyable;
import org.truffleruby.core.string.TStringWithEncoding;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

@ExportLibrary(InteropLibrary.class)
public final class RubyRegexp extends ImmutableRubyObjectNotCopyable implements TruffleObject, Comparable<RubyRegexp> {

    @TruffleBoundary
    public static RubyRegexp create(RubyLanguage language,
            TruffleString source,
            RubyEncoding sourceEncoding,
            RegexpOptions regexpOptions,
            Node currentNode) throws DeferredRaiseException {
        var strEnc = new TStringWithEncoding(source, sourceEncoding);
        if (regexpOptions.isEncodingNone()) {
            strEnc = strEnc.forceEncoding(Encodings.BINARY);
        }

        final RegexpCacheKey key = RegexpCacheKey.calculate(strEnc, regexpOptions);
        RubyRegexp regexp = language.getRegexp(key);
        if (regexp == null) {
            var optionsArray = new RegexpOptions[]{ regexpOptions };
            final Regex regex = TruffleRegexpNodes.compile(
                    null,
                    strEnc,
                    optionsArray,
                    currentNode);
            regexp = new RubyRegexp(regex, optionsArray[0]);
            language.addRegexp(key, regexp);

            if (language.options.REGEXP_INSTRUMENT_CREATION) {
                (regexpOptions.isLiteral()
                        ? TruffleRegexpNodes.LITERAL_REGEXPS
                        : TruffleRegexpNodes.DYNAMIC_REGEXPS).add(regexp);
            }

        }
        return regexp;
    }

    public final Regex regex;
    public final TruffleString source;
    public final RubyEncoding encoding;
    public final RegexpOptions options;
    public final EncodingCache cachedEncodings;
    public final TRegexCache tregexCache;

    private RubyRegexp(Regex regex, RegexpOptions options) {
        // The RegexpNodes.compile operation may modify the encoding of the source rope. This modified copy is stored
        // in the Regex object as the "user object". Since ropes are immutable, we need to take this updated copy when
        // constructing the final regexp.
        this.regex = regex;
        final TStringWithEncoding ropeWithEncoding = (TStringWithEncoding) regex.getUserObject();
        this.source = ropeWithEncoding.tstring;
        this.encoding = ropeWithEncoding.getEncoding();
        this.options = options;
        this.cachedEncodings = new EncodingCache();
        this.tregexCache = new TRegexCache();
    }

    // region InteropLibrary messages
    @ExportMessage
    protected Object toDisplayString(boolean allowSideEffects,
            @Cached DispatchNode dispatchNode,
            @Cached KernelNodes.ToSNode kernelToSNode) {
        if (allowSideEffects) {
            return dispatchNode.call(this, "inspect");
        } else {
            return kernelToSNode.executeToS(this);
        }
    }

    @ExportMessage
    protected boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    protected RubyClass getMetaObject(
            @CachedLibrary("this") InteropLibrary node) {
        return RubyContext.get(node).getCoreLibrary().regexpClass;
    }
    // endregion

    @Override
    public int compareTo(RubyRegexp o) {
        // Compare as binary as CRuby compares bytes regardless of the encodings
        var a = source.forceEncodingUncached(encoding.tencoding, Encodings.BINARY.tencoding);
        var b = o.source.forceEncodingUncached(encoding.tencoding, Encodings.BINARY.tencoding);

        final int sourceCompare = a.compareBytesUncached(b, Encodings.BINARY.tencoding);
        if (sourceCompare != 0) {
            return sourceCompare;
        } else {
            return options.compareTo(o.options);
        }
    }
}
