/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.nio.charset.UnsupportedCharsetException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.source.Source;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.ISO8859_1Encoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.Nil;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

public final class TRegexCache {

    // atStart=false
    private Object usAsciiRegex;
    private Object latin1Regex;
    private Object utf8Regex;
    private Object binaryRegex;

    // atStart=true
    private Object usAsciiRegexAtStart;
    private Object latin1RegexAtStart;
    private Object utf8RegexAtStart;
    private Object binaryRegexAtStart;

    public Object getUSASCIIRegex(boolean atStart) {
        return atStart ? usAsciiRegex : usAsciiRegexAtStart;
    }

    public Object getLatin1Regex(boolean atStart) {
        return atStart ? latin1Regex : latin1RegexAtStart;
    }

    public Object getUTF8Regex(boolean atStart) {
        return atStart ? utf8Regex : utf8RegexAtStart;
    }

    public Object getBinaryRegex(boolean atStart) {
        return atStart ? binaryRegex : binaryRegexAtStart;
    }

    @TruffleBoundary
    public Object compile(RubyContext context, RubyRegexp regexp, boolean atStart, Encoding encoding) {
        Object tregex = compileTRegex(context, regexp, atStart, encoding);
        if (tregex == null) {
            tregex = Nil.INSTANCE;
            if (context.getOptions().WARN_TRUFFLE_REGEX_COMPILE_FALLBACK) {
                DispatchNode.getUncached().call(
                        context.getCoreLibrary().truffleRegexpOperationsModule,
                        "warn_fallback_regex",
                        regexp,
                        atStart,
                        context.getEncodingManager().getRubyEncoding(encoding));
            }
        }

        if (encoding == USASCIIEncoding.INSTANCE) {
            if (atStart) {
                usAsciiRegex = tregex;
            } else {
                usAsciiRegexAtStart = tregex;
            }
        } else if (encoding == ISO8859_1Encoding.INSTANCE) {
            if (atStart) {
                latin1Regex = tregex;
            } else {
                latin1RegexAtStart = tregex;
            }
        } else if (encoding == UTF8Encoding.INSTANCE) {
            if (atStart) {
                utf8Regex = tregex;
            } else {
                utf8RegexAtStart = tregex;
            }
        } else if (encoding == ASCIIEncoding.INSTANCE) {
            if (atStart) {
                binaryRegex = tregex;
            } else {
                binaryRegexAtStart = tregex;
            }
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        return tregex;
    }

    public static String toTRegexEncoding(Encoding encoding) {
        if (encoding == UTF8Encoding.INSTANCE) {
            return "UTF-8";
        } else if (encoding == USASCIIEncoding.INSTANCE || encoding == ISO8859_1Encoding.INSTANCE) {
            return "LATIN-1";
        } else if (encoding == ASCIIEncoding.INSTANCE) {
            return "BYTES";
        } else {
            return null;
        }
    }

    @TruffleBoundary
    private static Object compileTRegex(RubyContext context, RubyRegexp regexp, boolean atStart, Encoding enc) {
        String processedRegexpSource;
        Encoding[] fixedEnc = new Encoding[]{ null };
        final RopeBuilder ropeBuilder;
        try {
            ropeBuilder = ClassicRegexp
                    .preprocess(
                            regexp.source,
                            enc,
                            fixedEnc,
                            RegexpSupport.ErrorMode.RAISE);
        } catch (DeferredRaiseException dre) {
            throw dre.getException(context);
        }
        Rope rope = ropeBuilder.toRope();
        try {
            processedRegexpSource = RopeOperations.decodeRope(rope);
        } catch (CannotConvertBinaryRubyStringToJavaString | UnsupportedCharsetException e) {
            // Some strings cannot be converted to Java strings, e.g. strings with the
            // BINARY encoding containing characters higher than 127.
            // Also, some charsets might not be supported on the JVM and therefore
            // a conversion to j.l.String might be impossible.
            return null;
        }

        String flags = optionsToFlags(regexp.options, atStart);

        String tRegexEncoding = TRegexCache.toTRegexEncoding(enc);
        if (tRegexEncoding == null) {
            return null;
        }

        String regex = "Flavor=Ruby,Encoding=" + tRegexEncoding + "/" + processedRegexpSource + "/" + flags;
        Source regexSource = Source
                .newBuilder("regex", regex, "Regexp")
                .mimeType("application/tregex")
                .internal(true)
                .build();
        Object compiledRegex = context.getEnv().parseInternal(regexSource).call();
        if (InteropLibrary.getUncached().isNull(compiledRegex)) {
            return null;
        } else {
            return compiledRegex;
        }
    }

    public static String optionsToFlags(RegexpOptions options, boolean atStart) {
        StringBuilder flags = new StringBuilder(4);
        if (options.isMultiline()) {
            flags.append('m');
        }
        if (options.isIgnorecase()) {
            flags.append('i');
        }
        if (options.isExtended()) {
            flags.append('x');
        }
        if (atStart) {
            flags.append('y');
        }
        return flags.toString();
    }
}
