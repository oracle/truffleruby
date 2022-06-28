/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is modified from org.jruby.runtime.encoding.EncodingService,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */
package org.truffleruby.core.encoding;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.ProcessProperties;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.EncodingUtils;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import static org.truffleruby.core.encoding.Encodings.INITIAL_NUMBER_OF_ENCODINGS;

/** Always use {@link Encoding#getIndex()} for encoding indices. Never use
 * {@link org.jcodings.EncodingDB.Entry#getIndex()}. */
public class EncodingManager {

    private RubyEncoding[] ENCODING_LIST_BY_ENCODING_INDEX = new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];
    private final Map<String, RubyEncoding> LOOKUP = new ConcurrentHashMap<>();
    private final RubyContext context;
    private final RubyLanguage language;

    @CompilationFinal private RubyEncoding localeEncoding;
    private RubyEncoding defaultExternalEncoding;
    private RubyEncoding defaultInternalEncoding;

    public EncodingManager(RubyContext context, RubyLanguage language) {
        this.context = context;
        this.language = language;
    }

    public void defineEncodings() {
        final RubyClass encodingClass = context.getCoreLibrary().encodingClass;
        initializeEncodings(encodingClass);
        initializeEncodingAliases(encodingClass);
    }

    private void initializeEncodings(RubyClass encodingClass) {
        var iterator = EncodingDB.getEncodings().entryIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.value.getEncoding() == Encodings.DUMMY_ENCODING_BASE) {
                continue;
            }
            final RubyEncoding rubyEncoding = defineBuiltInEncoding(entry.value);
            for (String constName : EncodingUtils.encodingNames(entry.bytes, entry.p, entry.end)) {
                encodingClass.fields.setConstant(context, null, constName, rubyEncoding);
            }
        }
    }

    private void initializeEncodingAliases(RubyClass encodingClass) {
        var iterator = EncodingDB.getAliases().entryIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            final EncodingDB.Entry encodingEntry = entry.value;

            // The alias name should be exactly the one in the encodings DB.
            final Encoding encoding = encodingEntry.getEncoding();
            final RubyEncoding rubyEncoding = defineAlias(encoding,
                    StringOperations.decodeAscii(entry.bytes, entry.p, entry.end));

            // The constant names must be treated by the the <code>encodingNames</code> helper.
            for (String constName : EncodingUtils.encodingNames(entry.bytes, entry.p, entry.end)) {
                encodingClass.fields.setConstant(context, null, constName, rubyEncoding);
            }
        }
    }

    public void initializeDefaultEncodings(TruffleNFIPlatform nfi, NativeConfiguration nativeConfiguration) {
        initializeLocaleEncoding(nfi, nativeConfiguration);

        // External should always have a value, but Encoding.external_encoding{,=} will lazily setup
        final String externalEncodingName = context.getOptions().EXTERNAL_ENCODING;
        if (!externalEncodingName.isEmpty()) {
            final RubyEncoding loadedEncoding = getRubyEncoding(externalEncodingName);
            if (loadedEncoding == null) {
                // TODO (nirvdrum 28-Oct-16): This should just print a nice error message and exit
                // with a status code of 1 -- it's essentially an input validation error -- no need
                // to show the user a full trace.
                throw new RuntimeException("unknown encoding name - " + externalEncodingName);
            } else {
                setDefaultExternalEncoding(loadedEncoding);
            }
        } else {
            setDefaultExternalEncoding(getLocaleEncoding());
        }

        // The internal encoding is nil by default
        final String internalEncodingName = context.getOptions().INTERNAL_ENCODING;
        if (!internalEncodingName.isEmpty()) {
            final RubyEncoding rubyEncoding = getRubyEncoding(internalEncodingName);
            if (rubyEncoding == null) {
                // TODO (nirvdrum 28-Oct-16): This should just print a nice error message and exit
                // with a status code of 1 -- it's essentially an input validation error -- no need
                // to show the user a full trace.
                throw new RuntimeException("unknown encoding name - " + internalEncodingName);
            } else {
                setDefaultInternalEncoding(rubyEncoding);
            }
        }
    }

    private void initializeLocaleEncoding(TruffleNFIPlatform nfi, NativeConfiguration nativeConfiguration) {
        if (ImageInfo.inImageRuntimeCode()) {
            // Call setlocale(LC_ALL, "") to ensure the locale is set to the environment's locale
            // rather than the default "C" locale.
            ProcessProperties.setLocale("LC_ALL", "");
        }

        final String localeEncodingName;
        if (nfi != null) {
            final int codeset = (int) nativeConfiguration.get("platform.langinfo.CODESET");

            // char *nl_langinfo(nl_item item);
            // nl_item is int on at least Linux and macOS
            final Object nl_langinfo = nfi.getFunction(context, "nl_langinfo", "(sint32):string");

            final long address;
            try {
                address = nfi.asPointer(InteropLibrary.getUncached().execute(nl_langinfo, codeset));
            } catch (InteropException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
            final byte[] bytes = new Pointer(address).readZeroTerminatedByteArray(
                    context,
                    InteropLibrary.getUncached(),
                    0);
            localeEncodingName = StringOperations.decodeAscii(bytes);
        } else {
            localeEncodingName = Charset.defaultCharset().name();
        }

        RubyEncoding rubyEncoding = getRubyEncoding(localeEncodingName);
        if (rubyEncoding == null) {
            rubyEncoding = Encodings.US_ASCII;
        }

        if (context.getOptions().WARN_LOCALE && rubyEncoding.jcoding == USASCIIEncoding.INSTANCE) {
            if ("C".equals(System.getenv("LANG")) && "C".equals(System.getenv("LC_ALL"))) {
                // The parent process seems to explicitly want a C locale (e.g. EnvUtil#invoke_ruby in the MRI test harness), so only warn at config level in this case.
                RubyLanguage.LOGGER.config(
                        "Encoding.find('locale') is US-ASCII, this often indicates that the system locale is not set properly. " +
                                "Warning at level=CONFIG because LANG=C and LC_ALL=C are set. " +
                                "Set LANG=en_US.UTF-8 and see https://www.graalvm.org/dev/reference-manual/ruby/UTF8Locale/ for details.");
            } else {
                RubyLanguage.LOGGER.warning(
                        "Encoding.find('locale') is US-ASCII, this often indicates that the system locale is not set properly. " +
                                "Set LANG=en_US.UTF-8 and see https://www.graalvm.org/dev/reference-manual/ruby/UTF8Locale/ for details.");
            }
        }

        localeEncoding = rubyEncoding;
    }

    @TruffleBoundary
    public static Encoding getEncoding(String name) {
        EncodingDB.Entry entry = EncodingDB.getEncodings().get(StringOperations.encodeAsciiBytes(name));

        if (entry == null) {
            entry = EncodingDB.getAliases().get(name.getBytes());
        }

        if (entry != null) {
            return entry.getEncoding();
        }

        return null;
    }

    public Object[] getEncodingList() {
        return ArrayUtils.copyOf(ENCODING_LIST_BY_ENCODING_INDEX, ENCODING_LIST_BY_ENCODING_INDEX.length);
    }

    @TruffleBoundary
    public RubyEncoding getRubyEncoding(String name) {
        final String normalizedName = name.toLowerCase(Locale.ENGLISH);
        final RubyEncoding encoding;

        switch (normalizedName) {
            case "internal":
                encoding = getDefaultInternalEncoding();
                return encoding == null ? Encodings.BINARY : encoding;
            case "external":
            case "filesystem":
                encoding = getDefaultExternalEncoding();
                return encoding == null ? Encodings.BINARY : encoding;
            case "locale":
                encoding = getLocaleEncoding();
                return encoding == null ? Encodings.BINARY : encoding;
            default:
                return LOOKUP.get(normalizedName);
        }
    }

    // Should only be used by Primitive.encoding_get_encoding_by_index
    RubyEncoding getRubyEncoding(int encodingIndex) {
        return ENCODING_LIST_BY_ENCODING_INDEX[encodingIndex];
    }

    @TruffleBoundary
    public synchronized RubyEncoding defineBuiltInEncoding(EncodingDB.Entry encodingEntry) {
        final int encodingIndex = encodingEntry.getEncoding().getIndex();
        final RubyEncoding rubyEncoding = Encodings.getBuiltInEncoding(encodingEntry.getEncoding());

        assert ENCODING_LIST_BY_ENCODING_INDEX[encodingIndex] == null;
        ENCODING_LIST_BY_ENCODING_INDEX[encodingIndex] = rubyEncoding;

        addToLookup(rubyEncoding.jcoding.toString(), rubyEncoding);
        return rubyEncoding;

    }

    @TruffleBoundary
    public synchronized RubyEncoding defineDynamicEncoding(Encoding encoding, byte[] name) {
        final int encodingIndex = ENCODING_LIST_BY_ENCODING_INDEX.length;

        final RubyEncoding rubyEncoding = Encodings.newRubyEncoding(language, encoding, encodingIndex, name);

        ENCODING_LIST_BY_ENCODING_INDEX = Arrays.copyOf(ENCODING_LIST_BY_ENCODING_INDEX, encodingIndex + 1);
        ENCODING_LIST_BY_ENCODING_INDEX[encodingIndex] = rubyEncoding;

        addToLookup(rubyEncoding.name.getJavaString(), rubyEncoding);
        return rubyEncoding;

    }

    @TruffleBoundary
    public RubyEncoding defineAlias(Encoding encoding, String name) {
        final RubyEncoding rubyEncoding = Encodings.getBuiltInEncoding(encoding);
        addToLookup(name, rubyEncoding);
        return rubyEncoding;
    }

    @TruffleBoundary
    private void addToLookup(String name, RubyEncoding rubyEncoding) {
        LOOKUP.put(name.toLowerCase(Locale.ENGLISH), rubyEncoding);
    }

    @TruffleBoundary
    public synchronized RubyEncoding createDummyEncoding(String name) {
        if (getRubyEncoding(name) != null) {
            return null;
        }

        final byte[] nameBytes = StringOperations.encodeAsciiBytes(name);
        return defineDynamicEncoding(Encodings.DUMMY_ENCODING_BASE, nameBytes);
    }

    @TruffleBoundary
    public synchronized RubyEncoding replicateEncoding(RubyEncoding encoding, String name) {
        if (getRubyEncoding(name) != null) {
            return null;
        }

        final byte[] nameBytes = StringOperations.encodeAsciiBytes(name);
        return defineDynamicEncoding(encoding.jcoding, nameBytes);
    }

    @TruffleBoundary
    public static Charset charsetForEncoding(Encoding encoding) {
        if (encoding == ASCIIEncoding.INSTANCE) {
            throw new UnsupportedOperationException("Cannot return a Charset for the BINARY Ruby Encoding");
        }
        return encoding.getCharset();
    }

    public RubyEncoding getLocaleEncoding() {
        return localeEncoding;
    }

    public void setDefaultExternalEncoding(RubyEncoding defaultExternalEncoding) {
        this.defaultExternalEncoding = defaultExternalEncoding;
    }

    public RubyEncoding getDefaultExternalEncoding() {
        return defaultExternalEncoding;
    }

    public void setDefaultInternalEncoding(RubyEncoding defaultInternalEncoding) {
        this.defaultInternalEncoding = defaultInternalEncoding;
    }

    public RubyEncoding getDefaultInternalEncoding() {
        return defaultInternalEncoding;
    }

}
