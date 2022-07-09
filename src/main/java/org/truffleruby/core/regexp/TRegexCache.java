/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.RubyContext;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.regexp.TruffleRegexpNodes.TRegexCompileNode;
import org.truffleruby.core.rope.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.rope.TStringBuilder;
import org.truffleruby.core.rope.TStringWithEncoding;
import org.truffleruby.interop.InteropNodes;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.control.DeferredRaiseException;

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
    public Object compile(RubyContext context, RubyRegexp regexp, boolean atStart, RubyEncoding encoding,
            TRegexCompileNode node) {
        Object tregex = compileTRegex(context, regexp, atStart, encoding);
        if (tregex == null) {
            tregex = Nil.INSTANCE;
            if (context.getOptions().WARN_TRUFFLE_REGEX_COMPILE_FALLBACK) {
                node.getWarnOnFallbackNode().call(
                        context.getCoreLibrary().truffleRegexpOperationsModule,
                        "warn_fallback_regex",
                        regexp,
                        atStart,
                        encoding);
            }
        } else if (isBacktracking(tregex)) {
            if (context.getOptions().WARN_TRUFFLE_REGEX_COMPILE_FALLBACK) {
                node.getWarnOnFallbackNode().call(
                        context.getCoreLibrary().truffleRegexpOperationsModule,
                        "warn_backtracking",
                        regexp,
                        atStart,
                        encoding);
            }
        }

        if (encoding == Encodings.US_ASCII) {
            if (atStart) {
                usAsciiRegex = tregex;
            } else {
                usAsciiRegexAtStart = tregex;
            }
        } else if (encoding == Encodings.ISO_8859_1) {
            if (atStart) {
                latin1Regex = tregex;
            } else {
                latin1RegexAtStart = tregex;
            }
        } else if (encoding == Encodings.UTF_8) {
            if (atStart) {
                utf8Regex = tregex;
            } else {
                utf8RegexAtStart = tregex;
            }
        } else if (encoding == Encodings.BINARY) {
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

    private static boolean isBacktracking(Object tregex) {
        return (boolean) InteropNodes.readMember(
                InteropLibrary.getUncached(),
                tregex,
                "isBacktracking",
                TranslateInteropExceptionNode.getUncached());
    }

    public static String toTRegexEncoding(RubyEncoding encoding) {
        if (encoding == Encodings.UTF_8) {
            return "UTF-8";
        } else if (encoding == Encodings.US_ASCII) {
            return "ASCII";
        } else if (encoding == Encodings.ISO_8859_1) {
            return "LATIN-1";
        } else if (encoding == Encodings.BINARY) {
            return "BYTES";
        } else {
            return null;
        }
    }

    @TruffleBoundary
    private static Object compileTRegex(RubyContext context, RubyRegexp regexp, boolean atStart, RubyEncoding enc) {
        String processedRegexpSource;
        RubyEncoding[] fixedEnc = new RubyEncoding[]{ null };
        final TStringBuilder tstringBuilder;
        try {
            TStringBuilder = ClassicRegexp
                    .preprocess(
                            new TStringWithEncoding(regexp.source, regexp.encoding),
                            enc,
                            fixedEnc,
                            RegexpSupport.ErrorMode.RAISE);
        } catch (DeferredRaiseException dre) {
            throw dre.getException(context);
        }
        var tstring = TStringBuilder.toTString();
        try {
            processedRegexpSource = TStringUtils.toJavaStringOrThrow(tstring, TStringBuilder.getRubyEncoding());
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

        String ignoreAtomicGroups = context.getOptions().TRUFFLE_REGEX_IGNORE_ATOMIC_GROUPS
                ? ",IgnoreAtomicGroups=true"
                : "";

        String regex = "Flavor=Ruby,Encoding=" + tRegexEncoding + ignoreAtomicGroups + "/" + processedRegexpSource +
                "/" + flags;
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
