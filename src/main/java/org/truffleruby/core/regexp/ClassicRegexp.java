/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004-2005 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 David Corbin <dcorbin@users.sourceforge.net>
 * Copyright (C) 2006 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.truffleruby.core.regexp;

import static org.truffleruby.core.string.StringUtils.EMPTY_STRING_ARRAY;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleStringBuilder;
import org.jcodings.Encoding;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.JOniException;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.ATStringWithEncoding;
import org.truffleruby.core.string.TStringBuilder;
import org.truffleruby.core.string.TStringWithEncoding;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.ReOptions;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.parser.RubyDeferredWarnings;

public class ClassicRegexp implements ReOptions {
    private final RubyContext context;
    private final Regex pattern;
    private final TStringWithEncoding str;
    private RegexpOptions options;

    public void setLiteral() {
        options = options.setLiteral(true);
    }

    public Encoding getEncoding() {
        return pattern.getEncoding();
    }

    public static Regex makeRegexp(RubyDeferredWarnings rubyDeferredWarnings,
            TStringBuilder processedSource, RegexpOptions options,
            RubyEncoding enc, AbstractTruffleString source, Node currentNode) throws DeferredRaiseException {
        try {
            return new Regex(
                    processedSource.getUnsafeBytes(),
                    0,
                    processedSource.getLength(),
                    options.toJoniOptions(),
                    enc.jcoding,
                    Syntax.RUBY,
                    rubyDeferredWarnings == null
                            ? new RegexWarnCallback()
                            : new RegexWarnDeferredCallback(rubyDeferredWarnings));
        } catch (Exception e) {
            String errorMessage = getRegexErrorMessage(source, e, options);
            throw new DeferredRaiseException(c -> c.getCoreExceptions().regexpError(errorMessage, currentNode));
        }
    }

    public static String getRegexErrorMessage(AbstractTruffleString source, Exception e, RegexpOptions options) {
        return BacktraceFormatter.formatJavaThrowableMessage(e) + ": /" + source + "/" + options.toOptionsString();
    }

    private static Regex getRegexpFromCache(TStringBuilder bytes, RubyEncoding encoding, RegexpOptions options,
            AbstractTruffleString source) throws DeferredRaiseException {
        final Regex newRegex = makeRegexp(null, bytes, options, encoding, source, null);
        newRegex.setUserObject(bytes);
        return newRegex;
    }

    public ClassicRegexp(RubyContext context, TStringWithEncoding strEnc, RegexpOptions originalOptions)
            throws DeferredRaiseException {
        this.context = context;
        this.options = originalOptions;

        if (strEnc.encoding.jcoding.isDummy()) {
            throw new UnsupportedOperationException("can't make regexp with dummy encoding");
        }

        RegexpOptions[] optionsArray = new RegexpOptions[]{ originalOptions };
        RubyEncoding[] fixedEnc = new RubyEncoding[]{ null };
        TStringBuilder unescaped = preprocess(strEnc, strEnc.encoding, fixedEnc, RegexpSupport.ErrorMode.RAISE);
        final RubyEncoding computedEnc = computeRegexpEncoding(optionsArray, strEnc.encoding, fixedEnc);
        this.pattern = getRegexpFromCache(
                unescaped,
                computedEnc,
                options,
                strEnc.forceEncoding(computedEnc).tstring);
        this.options = optionsArray[0];
        this.str = strEnc;
    }

    @TruffleBoundary
    @SuppressWarnings("fallthrough")
    private static boolean unescapeNonAscii(TStringBuilder to, TStringWithEncoding str, RubyEncoding enc,
            RubyEncoding[] encp, RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        boolean hasProperty = false;
        byte[] buf = null;

        var byteArray = str.getInternalByteArray();
        final int offset = byteArray.getOffset();
        int p = offset;
        int end = byteArray.getEnd();
        final byte[] bytes = byteArray.getArray();

        var strInEnc = str.forceEncoding(enc);

        while (p < end) {
            final int cl = strInEnc.characterLength(p - offset);
            if (cl <= 0) {
                raisePreprocessError("invalid multibyte character", mode);
            }
            if (cl > 1 || (bytes[p] & 0x80) != 0) {
                if (to != null) {
                    to.append(bytes, p, cl);
                }
                p += cl;
                if (encp[0] == null) {
                    encp[0] = enc;
                } else if (encp[0] != enc) {
                    raisePreprocessError("non ASCII character in UTF-8 regexp", mode);
                }
                continue;
            }
            int c;
            switch (c = bytes[p++] & 0xff) {
                case '\\':
                    if (p == end) {
                        raisePreprocessError("too short escape sequence", mode);
                    }

                    switch (c = bytes[p++] & 0xff) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7': /* \O, \OO, \OOO or backref */
                            if (StringSupport.scanOct(bytes, p - 1, end - (p - 1)) <= 0177) {
                                if (to != null) {
                                    to.append('\\');
                                    to.append(c);
                                }
                                break;
                            }

                        case '0': /* \0, \0O, \0OO */
                        case 'x': /* \xHH */
                        case 'c': /* \cX, \c\M-X */
                        case 'C': /* \C-X, \C-\M-X */
                        case 'M': /* \M-X, \M-\C-X, \M-\cX */
                            p -= 2;
                            if (enc == Encodings.US_ASCII) {
                                if (buf == null) {
                                    buf = new byte[1];
                                }
                                int pbeg = p;
                                p = readEscapedByte(buf, 0, bytes, p, end, mode);
                                c = buf[0];
                                if (c == -1) {
                                    return false;
                                }
                                if (to != null) {
                                    to.append(bytes, pbeg, p - pbeg);
                                }
                            } else {
                                p = unescapeEscapedNonAscii(to, bytes, p, end, enc, encp, mode);
                            }
                            break;

                        case 'u':
                            if (p == end) {
                                raisePreprocessError("too short escape sequence", mode);
                            }
                            if (bytes[p] == (byte) '{') { /* \\u{H HH HHH HHHH HHHHH HHHHHH ...} */
                                p++;
                                p = unescapeUnicodeList(to, bytes, p, end, encp, mode);
                                if (p == end || bytes[p++] != (byte) '}') {
                                    raisePreprocessError("invalid Unicode list", mode);
                                }
                            } else { /* \\uHHHH */
                                p = unescapeUnicodeBmp(to, bytes, p, end, encp, mode);
                            }
                            break;
                        case 'p': /* \p{Hiragana} */
                            if (encp[0] == null) {
                                hasProperty = true;
                            }
                            if (to != null) {
                                to.append('\\');
                                to.append(c);
                            }
                            break;

                        default:
                            if (to != null) {
                                to.append('\\');
                                to.append(c);
                            }
                            break;
                    } // inner switch
                    break;

                default:
                    if (to != null) {
                        to.append(c);
                    }
            } // switch
        } // while
        return hasProperty;
    }

    private static int unescapeUnicodeBmp(TStringBuilder to, byte[] bytes, int p, int end,
            RubyEncoding[] encp, RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        if (p + 4 > end) {
            raisePreprocessError("invalid Unicode escape", mode);
        }
        int code = StringSupport.scanHex(bytes, p, 4);
        int len = StringSupport.hexLength(bytes, p, 4);
        if (len != 4) {
            raisePreprocessError("invalid Unicode escape", mode);
        }
        appendUtf8(to, code, encp, mode);
        return p + 4;
    }

    private static int unescapeUnicodeList(TStringBuilder to, byte[] bytes, int p, int end,
            RubyEncoding[] encp, RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        while (p < end && StringSupport.isAsciiSpace(bytes[p] & 0xff)) {
            p++;
        }

        boolean hasUnicode = false;
        while (true) {
            int code = StringSupport.scanHex(bytes, p, end - p);
            int len = StringSupport.hexLength(bytes, p, end - p);
            if (len == 0) {
                break;
            }
            if (len > 6) {
                raisePreprocessError("invalid Unicode range", mode);
            }
            p += len;
            if (to != null) {
                appendUtf8(to, code, encp, mode);
            }
            hasUnicode = true;
            while (p < end && StringSupport.isAsciiSpace(bytes[p] & 0xff)) {
                p++;
            }
        }

        if (!hasUnicode) {
            raisePreprocessError("invalid Unicode list", mode);
        }
        return p;
    }

    private static void appendUtf8(TStringBuilder to, int code, RubyEncoding[] enc,
            RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        checkUnicodeRange(code, mode);

        if (code < 0x80) {
            if (to != null) {
                to.append(StringUtils.formatASCIIBytes("\\x%02X", code));
            }
        } else {
            if (to != null) {
                to.unsafeEnsureSpace(to.getLength() + 6);
                to.setLength(to.getLength() + utf8Decode(to.getUnsafeBytes(), to.getLength(), code));
            }
            if (enc[0] == null) {
                enc[0] = Encodings.UTF_8;
            } else if (!(enc[0].jcoding.isUTF8())) {
                raisePreprocessError("UTF-8 character in non UTF-8 regexp", mode);
            }
        }
    }

    public static int utf8Decode(byte[] to, int p, int code) {
        if (code <= 0x7f) {
            to[p] = (byte) code;
            return 1;
        } else if (code <= 0x7ff) {
            to[p + 0] = (byte) (((code >>> 6) & 0xff) | 0xc0);
            to[p + 1] = (byte) ((code & 0x3f) | 0x80);
            return 2;
        } else if (code <= 0xffff) {
            to[p + 0] = (byte) (((code >>> 12) & 0xff) | 0xe0);
            to[p + 1] = (byte) (((code >>> 6) & 0x3f) | 0x80);
            to[p + 2] = (byte) ((code & 0x3f) | 0x80);
            return 3;
        } else if (code <= 0x1fffff) {
            to[p + 0] = (byte) (((code >>> 18) & 0xff) | 0xf0);
            to[p + 1] = (byte) (((code >>> 12) & 0x3f) | 0x80);
            to[p + 2] = (byte) (((code >>> 6) & 0x3f) | 0x80);
            to[p + 3] = (byte) ((code & 0x3f) | 0x80);
            return 4;
        } else if (code <= 0x3ffffff) {
            to[p + 0] = (byte) (((code >>> 24) & 0xff) | 0xf8);
            to[p + 1] = (byte) (((code >>> 18) & 0x3f) | 0x80);
            to[p + 2] = (byte) (((code >>> 12) & 0x3f) | 0x80);
            to[p + 3] = (byte) (((code >>> 6) & 0x3f) | 0x80);
            to[p + 4] = (byte) ((code & 0x3f) | 0x80);
            return 5;
        } else { // code <= 0x7fffffff = max int
            to[p + 0] = (byte) (((code >>> 30) & 0xff) | 0xfc);
            to[p + 1] = (byte) (((code >>> 24) & 0x3f) | 0x80);
            to[p + 2] = (byte) (((code >>> 18) & 0x3f) | 0x80);
            to[p + 3] = (byte) (((code >>> 12) & 0x3f) | 0x80);
            to[p + 4] = (byte) (((code >>> 6) & 0x3f) | 0x80);
            to[p + 5] = (byte) ((code & 0x3f) | 0x80);
            return 6;
        }
    }

    private static void checkUnicodeRange(int code, RegexpSupport.ErrorMode mode)
            throws DeferredRaiseException {
        // Unicode is can be only 21 bits long, int is enough
        if ((0xd800 <= code && code <= 0xdfff) /* Surrogates */ || 0x10ffff < code) {
            raisePreprocessError("invalid Unicode range", mode);
        }
    }

    private static int unescapeEscapedNonAscii(TStringBuilder to, byte[] bytes, int p, int end,
            RubyEncoding enc, RubyEncoding[] encp, RegexpSupport.ErrorMode mode)
            throws DeferredRaiseException {
        byte[] chBuf = new byte[enc.jcoding.maxLength()];
        int chLen = 0;

        p = readEscapedByte(chBuf, chLen++, bytes, p, end, mode);
        while (chLen < enc.jcoding.maxLength() &&
                StringSupport.MBCLEN_NEEDMORE_P(StringSupport.characterLength(enc, chBuf, 0, chLen))) {
            p = readEscapedByte(chBuf, chLen++, bytes, p, end, mode);
        }

        int cl = StringSupport.characterLength(enc, chBuf, 0, chLen);
        if (cl == -1) {
            raisePreprocessError("invalid multibyte escape", mode); // MBCLEN_INVALID_P
        }

        if (chLen > 1 || (chBuf[0] & 0x80) != 0) {
            if (to != null) {
                to.append(chBuf, 0, chLen);
            }

            if (encp[0] == null) {
                encp[0] = enc;
            } else if (encp[0] != enc) {
                raisePreprocessError("escaped non ASCII character in UTF-8 regexp", mode);
            }
        } else {
            if (to != null) {
                to.append(StringUtils.formatASCIIBytes("\\x%02X", chBuf[0] & 0xff));
            }
        }
        return p;
    }

    public static int raisePreprocessError(String err, RegexpSupport.ErrorMode mode)
            throws DeferredRaiseException {
        switch (mode) {
            case RAISE:
                throw new DeferredRaiseException(context -> context.getCoreExceptions().regexpError(err, null));
            case PREPROCESS:
                throw new DeferredRaiseException(context -> context
                        .getCoreExceptions()
                        .argumentError("regexp preprocess failed: " + err, null));
            case DESC:
                // silent ?
        }
        return 0;
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings("SF")
    public static int readEscapedByte(byte[] to, int toP, byte[] bytes, int p, int end,
            RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        if (p == end || bytes[p++] != (byte) '\\') {
            raisePreprocessError("too short escaped multibyte character", mode);
        }

        boolean metaPrefix = false, ctrlPrefix = false;
        int code = 0;
        while (true) {
            if (p == end) {
                raisePreprocessError("too short escape sequence", mode);
            }

            switch (bytes[p++]) {
                case '\\':
                    code = '\\';
                    break;
                case 'n':
                    code = '\n';
                    break;
                case 't':
                    code = '\t';
                    break;
                case 'r':
                    code = '\r';
                    break;
                case 'f':
                    code = '\f';
                    break;
                case 'v':
                    code = '\013';
                    break;
                case 'a':
                    code = '\007';
                    break;
                case 'e':
                    code = '\033';
                    break;

                /* \OOO */
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    p--;
                    int olen = end < p + 3 ? end - p : 3;
                    code = StringSupport.scanOct(bytes, p, olen);
                    p += StringSupport.octLength(bytes, p, olen);
                    break;

                case 'x': /* \xHH */
                    int hlen = end < p + 2 ? end - p : 2;
                    code = StringSupport.scanHex(bytes, p, hlen);
                    int len = StringSupport.hexLength(bytes, p, hlen);
                    if (len < 1) {
                        raisePreprocessError("invalid hex escape", mode);
                    }
                    p += len;
                    break;

                case 'M': /* \M-X, \M-\C-X, \M-\cX */
                    if (metaPrefix) {
                        raisePreprocessError("duplicate meta escape", mode);
                    }
                    metaPrefix = true;
                    if (p + 1 < end && bytes[p++] == (byte) '-' && (bytes[p] & 0x80) == 0) {
                        if (bytes[p] == (byte) '\\') {
                            p++;
                            continue;
                        } else {
                            code = bytes[p++] & 0xff;
                            break;
                        }
                    }
                    raisePreprocessError("too short meta escape", mode);

                case 'C': /* \C-X, \C-\M-X */
                    if (p == end || bytes[p++] != (byte) '-') {
                        raisePreprocessError("too short control escape", mode);
                    }

                case 'c': /* \cX, \c\M-X */
                    if (ctrlPrefix) {
                        raisePreprocessError("duplicate control escape", mode);
                    }
                    ctrlPrefix = true;
                    if (p < end && (bytes[p] & 0x80) == 0) {
                        if (bytes[p] == (byte) '\\') {
                            p++;
                            continue;
                        } else {
                            code = bytes[p++] & 0xff;
                            break;
                        }
                    }
                    raisePreprocessError("too short control escape", mode);
                default:
                    raisePreprocessError("unexpected escape sequence", mode);
            } // switch

            if (code < 0 || code > 0xff) {
                raisePreprocessError("invalid escape code", mode);
            }

            if (ctrlPrefix) {
                code &= 0x1f;
            }
            if (metaPrefix) {
                code |= 0x80;
            }

            to[toP] = (byte) code;
            return p;
        } // while
    }

    public static void preprocessCheck(TStringWithEncoding ropeWithEncoding) throws DeferredRaiseException {
        preprocess(
                ropeWithEncoding,
                ropeWithEncoding.getEncoding(),
                new RubyEncoding[]{ null },
                RegexpSupport.ErrorMode.RAISE);
    }

    public static TStringBuilder preprocess(TStringWithEncoding str, RubyEncoding enc, RubyEncoding[] fixedEnc,
            RegexpSupport.ErrorMode mode) throws DeferredRaiseException {
        TStringBuilder to = TStringBuilder.create(str.byteLength());

        if (enc.jcoding.isAsciiCompatible()) {
            fixedEnc[0] = null;
        } else {
            fixedEnc[0] = enc;
            to.setEncoding(enc);
        }

        boolean hasProperty = unescapeNonAscii(to, str, enc, fixedEnc, mode);
        if (hasProperty && fixedEnc[0] == null) {
            fixedEnc[0] = enc;
        }
        if (fixedEnc[0] != null) {
            to.setEncoding(fixedEnc[0]);
        }
        return to;
    }

    private static void preprocessLight(TStringWithEncoding str, RubyEncoding enc, RubyEncoding[] fixedEnc)
            throws DeferredRaiseException {
        if (enc.jcoding.isAsciiCompatible()) {
            fixedEnc[0] = null;
        } else {
            fixedEnc[0] = enc;
        }

        boolean hasProperty = unescapeNonAscii(null, str, enc, fixedEnc,
                RegexpSupport.ErrorMode.PREPROCESS);
        if (hasProperty && fixedEnc[0] == null) {
            fixedEnc[0] = enc;
        }
    }

    @TruffleBoundary
    public static TStringWithEncoding preprocessDRegexp(RubyContext context, TStringWithEncoding[] strings,
            RegexpOptions options) throws DeferredRaiseException {
        assert strings.length > 0;

        ByteArrayBuilder builder = ByteArrayBuilder.create(strings[0].getInternalByteArray());

        RubyEncoding regexpEnc = processDRegexpElement(context, options, null, strings[0]);

        for (int i = 1; i < strings.length; i++) {
            var str = strings[i];
            regexpEnc = processDRegexpElement(context, options, regexpEnc, str);
            builder.append(str);
        }

        if (options.isEncodingNone()) {
            if (!all7Bit(builder.getBytes())) {
                regexpEnc = Encodings.BINARY;
            } else {
                regexpEnc = Encodings.US_ASCII;
            }
        }

        if (regexpEnc == null) {
            regexpEnc = strings[0].getEncoding();
        }

        return new TStringWithEncoding(builder.toTString(regexpEnc), regexpEnc);
    }

    @TruffleBoundary
    private static RubyEncoding processDRegexpElement(RubyContext context, RegexpOptions options,
            RubyEncoding regexpEnc, TStringWithEncoding str) throws DeferredRaiseException {
        RubyEncoding strEnc = str.getEncoding();

        if (options.isEncodingNone() && strEnc != Encodings.BINARY) {
            if (!str.isAsciiOnly()) {
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().regexpError(
                                "/.../n has a non escaped non ASCII character in non ASCII-8BIT script",
                                null));
            }
            strEnc = Encodings.BINARY;
        }

        // This used to call preprocess, but the resulting rope builder was not
        // used. Since the preprocessing error-checking can be done without
        // creating a new rope builder, I added a "light" path.
        final RubyEncoding[] fixedEnc = new RubyEncoding[]{ null };
        ClassicRegexp.preprocessLight(str, strEnc, fixedEnc);

        if (fixedEnc[0] != null) {
            if (regexpEnc != null && regexpEnc != fixedEnc[0]) {
                throw new RaiseException(
                        context,
                        context
                                .getCoreExceptions()
                                .regexpError(
                                        "encoding mismatch in dynamic regexp: " + regexpEnc + " and " + fixedEnc[0],
                                        null));
            }
            regexpEnc = fixedEnc[0];
        }
        return regexpEnc;
    }

    private static boolean all7Bit(byte[] bytes) {
        for (int n = 0; n < bytes.length; n++) {
            if (bytes[n] < 0) {
                return false;
            }

            if (bytes[n] == '\\' && n + 1 < bytes.length && bytes[n + 1] == 'x') {
                final String num;
                final boolean isSecondHex = n + 3 < bytes.length && Character.digit(bytes[n + 3], 16) != -1;
                if (isSecondHex) {
                    num = new String(Arrays.copyOfRange(bytes, n + 2, n + 4), StandardCharsets.UTF_8);
                } else {
                    num = new String(Arrays.copyOfRange(bytes, n + 2, n + 3), StandardCharsets.UTF_8);
                }

                int b = Integer.parseInt(num, 16);

                if (b > 0x7F) {
                    return false;
                }

                if (isSecondHex) {
                    n += 3;
                } else {
                    n += 2;
                }

            }
        }

        return true;
    }

    /** \v */
    private static final int QUOTED_V = 11;

    /** rb_reg_quote */
    @TruffleBoundary
    public static TStringWithEncoding quote19(ATStringWithEncoding bs) {
        final boolean asciiOnly = bs.isAsciiOnly();
        boolean metaFound = false;

        var iterator = bs.createCodePointIterator();
        while (iterator.hasNext()) {
            final int c = iterator.nextUncached();

            switch (c) {
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case '|':
                case '-':
                case '*':
                case '.':
                case '\\':
                case '?':
                case '+':
                case '^':
                case '$':
                case ' ':
                case '#':
                case '\t':
                case '\f':
                case QUOTED_V:
                case '\n':
                case '\r':
                    metaFound = true;
                    break;
            }
        }

        if (!metaFound) {
            if (asciiOnly) {
                return bs.forceEncoding(Encodings.US_ASCII);
            } else {
                return bs.asImmutable();
            }
        }

        var resultEncoding = asciiOnly ? Encodings.US_ASCII : bs.encoding;
        var builder = TruffleStringBuilder.create(resultEncoding.tencoding, bs.byteLength() * 2);

        iterator = bs.createCodePointIterator();
        while (iterator.hasNext()) {
            final int c = iterator.nextUncached();

            if (!(c >= 0 && Encoding.isAscii(c))) {
                builder.appendCodePointUncached(c);
                continue;
            }

            switch (c) {
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case '|':
                case '-':
                case '*':
                case '.':
                case '\\':
                case '?':
                case '+':
                case '^':
                case '$':
                case '#':
                case ' ':
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached(c);
                    break;
                case '\t':
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached('t');
                    break;
                case '\n':
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached('n');
                    break;
                case '\r':
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached('r');
                    break;
                case '\f':
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached('f');
                    break;
                case QUOTED_V:
                    builder.appendCodePointUncached('\\');
                    builder.appendCodePointUncached('v');
                    break;
                default:
                    builder.appendCodePointUncached(c);
                    break;
            }
        }

        return new TStringWithEncoding(builder.toStringUncached(), resultEncoding);
    }

    /** WARNING: This mutates options, so the caller should make sure it's a copy */
    static RubyEncoding computeRegexpEncoding(RegexpOptions[] options, RubyEncoding enc, RubyEncoding[] fixedEnc)
            throws DeferredRaiseException {
        if (fixedEnc[0] != null) {
            if ((fixedEnc[0] != enc && options[0].isFixed()) ||
                    (fixedEnc[0] != Encodings.BINARY && options[0].isEncodingNone())) {
                throw new DeferredRaiseException(context -> context
                        .getCoreExceptions()
                        .regexpError("incompatible character encoding", null));
            }
            if (fixedEnc[0] != Encodings.BINARY) {
                options[0] = options[0].setFixed(true);
                enc = fixedEnc[0];
            }
        } else if (!options[0].isFixed()) {
            enc = Encodings.US_ASCII;
        }

        if (fixedEnc[0] != null) {
            options[0] = options[0].setFixed(true);
        }

        // This needs to return the modified options in some way. Sigh.
        return enc;
    }

    public static void appendOptions(TStringBuilder to, RegexpOptions options) {
        if (options.isMultiline()) {
            to.append((byte) 'm');
        }
        if (options.isIgnorecase()) {
            to.append((byte) 'i');
        }
        if (options.isExtended()) {
            to.append((byte) 'x');
        }
    }

    @SuppressWarnings("unused")
    public ByteArrayBuilder toByteArrayBuilder() {
        RegexpOptions newOptions = (RegexpOptions) options.clone();
        var byteArray = str.getInternalByteArray();
        int p = 0;
        int len = byteArray.getLength();

        TStringBuilder result = TStringBuilder.create(len);
        result.append((byte) '(');
        result.append((byte) '?');

        again: do {
            if (len >= 4 && byteArray.get(p) == '(' && byteArray.get(p + 1) == '?') {
                boolean err = true;
                p += 2;
                if ((len -= 2) > 0) {
                    do {
                        if (byteArray.get(p) == 'm') {
                            newOptions = newOptions.setMultiline(true);
                        } else if (byteArray.get(p) == 'i') {
                            newOptions = newOptions.setIgnorecase(true);
                        } else if (byteArray.get(p) == 'x') {
                            newOptions = newOptions.setExtended(true);
                        } else {
                            break;
                        }
                        p++;
                    } while (--len > 0);
                }
                if (len > 1 && byteArray.get(p) == '-') {
                    ++p;
                    --len;
                    do {
                        if (byteArray.get(p) == 'm') {
                            newOptions = newOptions.setMultiline(false);
                        } else if (byteArray.get(p) == 'i') {
                            newOptions = newOptions.setIgnorecase(false);
                        } else if (byteArray.get(p) == 'x') {
                            newOptions = newOptions.setExtended(false);
                        } else {
                            break;
                        }
                        p++;
                    } while (--len > 0);
                }

                if (byteArray.get(p) == ')') {
                    --len;
                    ++p;
                    continue again;
                }

                if (byteArray.get(p) == ':' && byteArray.get(p + len - 1) == ')') {
                    p++;
                    try {
                        new Regex(
                                byteArray.getArray(),
                                p + byteArray.getOffset(),
                                p + byteArray.getOffset() + (len -= 2),
                                Option.DEFAULT,
                                str.encoding.jcoding,
                                Syntax.DEFAULT,
                                new RegexWarnCallback());
                        err = false;
                    } catch (JOniException e) {
                        err = true;
                    }
                }

                if (err) {
                    newOptions = options;
                    p = 0;
                    len = str.byteLength();
                }
            }

            appendOptions(result, newOptions);

            if (!newOptions.isEmbeddable()) {
                result.append((byte) '-');
                if (!newOptions.isMultiline()) {
                    result.append((byte) 'm');
                }
                if (!newOptions.isIgnorecase()) {
                    result.append((byte) 'i');
                }
                if (!newOptions.isExtended()) {
                    result.append((byte) 'x');
                }
            }
            result.append((byte) ':');
            appendRegexpString(result, str, p, len);

            result.append((byte) ')');
            result.setEncoding(Encodings.getBuiltInEncoding(getEncoding()));
            return result;
            //return RubyString.newString(getRuntime(), result, getEncoding()).infectBy(this);
        } while (true);
    }

    @TruffleBoundary
    public void appendRegexpString(TStringBuilder to, TStringWithEncoding fullStr, int start, int len) {
        var str = fullStr.substring(start, len);

        final var enc = str.encoding.jcoding;
        var iterator = str.createCodePointIterator();

        boolean needEscape = false;
        while (iterator.hasNext()) {
            final int c = iterator.nextUncached();
            if ((c >= 0 && Encoding.isAscii(c)) && (c == '/' || !enc.isPrint(c))) {
                needEscape = true;
                break;
            }
        }

        if (!needEscape) {
            to.append(str);
        } else {
            iterator = str.createCodePointIterator();
            while (iterator.hasNext()) {
                final int p = iterator.getByteIndex();
                final int c = iterator.nextUncached();

                if (c == '\\' && iterator.hasNext()) {
                    iterator.nextUncached();
                    to.append(str, p, iterator.getByteIndex() - p);
                } else if (c == '/') {
                    to.append((byte) '\\');
                    to.append(str, p, iterator.getByteIndex() - p);
                } else if (!(c >= 0 && Encoding.isAscii(c))) {
                    if (c == -1) {
                        to.append(StringUtils.formatASCIIBytes("\\x%02X", c));
                    } else {
                        to.append(str, p, iterator.getByteIndex() - p);
                    }
                } else if (enc.isPrint(c)) {
                    to.append(str, p, iterator.getByteIndex() - p);
                } else if (!enc.isSpace(c)) {
                    to.append(StringUtils.formatASCIIBytes("\\x%02X", c));
                } else {
                    to.append(str, p, iterator.getByteIndex() - p);
                }
            }
        }
    }

    public String[] getNames() {
        int nameLength = pattern.numberOfNames();
        if (nameLength == 0) {
            return EMPTY_STRING_ARRAY;
        }

        String[] names = new String[nameLength];
        int j = 0;
        for (Iterator<NameEntry> i = pattern.namedBackrefIterator(); i.hasNext();) {
            NameEntry e = i.next();
            //intern() to improve footprint
            names[j++] = new String(e.name, e.nameP, e.nameEnd - e.nameP, pattern.getEncoding().getCharset()).intern();
        }

        return names;
    }

}
