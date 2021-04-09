/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.ProcessProperties;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.EncodingDB.Entry;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.EncodingUtils;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/** Always use {@link Encoding#getIndex()} for encoding indices. Never use
 * {@link org.jcodings.EncodingDB.Entry#getIndex()}. */
public class EncodingManager {

    private static final int INITIAL_NUMBER_OF_ENCODINGS = EncodingDB.getEncodings().size();

    private final List<RubyEncoding> ENCODING_LIST_BY_ENCODING_INDEX = new ArrayList<>(INITIAL_NUMBER_OF_ENCODINGS);
    @CompilationFinal(dimensions = 1) private final RubyEncoding[] BUILT_IN_ENCODINGS = //
            new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];
    private final Map<String, RubyEncoding> LOOKUP = new ConcurrentHashMap<>();

    private final RubyContext context;
    private final RubyLanguage language;

    @CompilationFinal private Encoding localeEncoding;
    private Encoding defaultExternalEncoding;
    private Encoding defaultInternalEncoding;


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
        final CaseInsensitiveBytesHash<EncodingDB.Entry>.CaseInsensitiveBytesHashEntryIterator hei = EncodingDB
                .getEncodings()
                .entryIterator();

        while (hei.hasNext()) {
            final CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = hei.next();
            final EncodingDB.Entry encodingEntry = e.value;
            final RubyEncoding rubyEncoding = defineEncoding(encodingEntry, e.bytes, e.p, e.end);
            BUILT_IN_ENCODINGS[encodingEntry.getEncoding().getIndex()] = rubyEncoding;
            for (String constName : EncodingUtils.encodingNames(e.bytes, e.p, e.end)) {
                encodingClass.fields.setConstant(context, null, constName, rubyEncoding);
            }
        }
    }

    private void initializeEncodingAliases(RubyClass encodingClass) {
        final CaseInsensitiveBytesHash<EncodingDB.Entry>.CaseInsensitiveBytesHashEntryIterator hei = EncodingDB
                .getAliases()
                .entryIterator();

        while (hei.hasNext()) {
            final CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = hei.next();
            final EncodingDB.Entry encodingEntry = e.value;

            // The alias name should be exactly the one in the encodings DB.
            final Encoding encoding = encodingEntry.getEncoding();
            final RubyEncoding rubyEncoding = defineAlias(encoding, RopeOperations.decodeAscii(e.bytes, e.p, e.end));

            // The constant names must be treated by the the <code>encodingNames</code> helper.
            for (String constName : EncodingUtils.encodingNames(e.bytes, e.p, e.end)) {
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
                setDefaultExternalEncoding(loadedEncoding.encoding);
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
                setDefaultInternalEncoding(rubyEncoding.encoding);
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
            // nl_item is int on at least Linux, macOS & Solaris
            final NativeFunction nl_langinfo = nfi.getFunction("nl_langinfo", "(sint32):string");

            final long address = nfi.asPointer(nl_langinfo.call(codeset));
            final byte[] bytes = new Pointer(address).readZeroTerminatedByteArray(context, 0);
            localeEncodingName = RopeOperations.decodeAscii(bytes);
        } else {
            localeEncodingName = Charset.defaultCharset().name();
        }

        RubyEncoding rubyEncoding = getRubyEncoding(localeEncodingName);
        if (rubyEncoding == null) {
            rubyEncoding = getRubyEncoding("US-ASCII");
        }

        localeEncoding = rubyEncoding.encoding;
    }

    @TruffleBoundary
    private RubyEncoding newRubyEncoding(Encoding encoding, byte[] name, int p, int end) {
        assert p == 0 : "Ropes can't be created with non-zero offset: " + p;
        assert end == name.length : "Ropes must have the same exact length as the name array (len = " + end +
                "; name.length = " + name.length + ")";

        final Rope rope = RopeOperations.create(name, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        final ImmutableRubyString string = language.getFrozenStringLiteral(rope);

        final RubyEncoding instance = new RubyEncoding(encoding, string);
        // TODO BJF Jul-29-2020 Add allocation tracing
        return instance;
    }

    public static Encoding getEncoding(String name) {
        return getEncoding(RopeOperations.encodeAscii(name, USASCIIEncoding.INSTANCE));
    }

    @TruffleBoundary
    public static Encoding getEncoding(Rope name) {
        EncodingDB.Entry entry = EncodingDB.getEncodings().get(name.getBytes());

        if (entry == null) {
            entry = EncodingDB.getAliases().get(name.getBytes());
        }

        if (entry != null) {
            return entry.getEncoding();
        }

        return null;
    }

    @TruffleBoundary
    public Object[] getEncodingList() {
        return new ArrayList<>(ENCODING_LIST_BY_ENCODING_INDEX).toArray();
    }

    @TruffleBoundary
    public RubyEncoding getRubyEncoding(String name) {
        final String normalizedName = name.toLowerCase(Locale.ENGLISH);
        final Encoding encoding;

        switch (normalizedName) {
            case "internal":
                encoding = getDefaultInternalEncoding();
                return getRubyEncoding(encoding == null ? ASCIIEncoding.INSTANCE : encoding);
            case "external":
            case "filesystem":
                encoding = getDefaultExternalEncoding();
                return getRubyEncoding(encoding == null ? ASCIIEncoding.INSTANCE : encoding);
            case "locale":
                encoding = getLocaleEncoding();
                return getRubyEncoding(encoding == null ? ASCIIEncoding.INSTANCE : encoding);
            default:
                return LOOKUP.get(normalizedName);
        }
    }

    @TruffleBoundary
    public RubyEncoding getRubyEncoding(int encodingIndex) {
        return ENCODING_LIST_BY_ENCODING_INDEX.get(encodingIndex);
    }

    @TruffleBoundary
    public RubyEncoding getRubyEncoding(Encoding encoding) {
        return ENCODING_LIST_BY_ENCODING_INDEX.get(encoding.getIndex());
    }

    @TruffleBoundary
    public synchronized RubyEncoding defineEncoding(EncodingDB.Entry encodingEntry, byte[] name, int p, int end) {
        final Encoding encoding = encodingEntry.getEncoding();
        final int encodingIndex = encoding.getIndex();
        final RubyEncoding rubyEncoding = newRubyEncoding(encoding, name, p, end);

        assert encodingIndex >= ENCODING_LIST_BY_ENCODING_INDEX.size() ||
                ENCODING_LIST_BY_ENCODING_INDEX.get(encodingIndex) == null;

        while (encodingIndex >= ENCODING_LIST_BY_ENCODING_INDEX.size()) {
            ENCODING_LIST_BY_ENCODING_INDEX.add(null);
        }
        ENCODING_LIST_BY_ENCODING_INDEX.set(encodingIndex, rubyEncoding);

        LOOKUP.put(rubyEncoding.encoding.toString().toLowerCase(Locale.ENGLISH), rubyEncoding);
        return rubyEncoding;
    }

    @TruffleBoundary
    public RubyEncoding defineAlias(Encoding encoding, String name) {
        final RubyEncoding rubyEncoding = getRubyEncoding(encoding);
        LOOKUP.put(name.toLowerCase(Locale.ENGLISH), rubyEncoding);
        return rubyEncoding;
    }

    @TruffleBoundary
    public synchronized RubyEncoding createDummyEncoding(String name) {
        if (getRubyEncoding(name) != null) {
            return null;
        }

        byte[] nameBytes = RopeOperations.encodeAsciiBytes(name);
        EncodingDB.dummy(nameBytes);
        final Entry entry = EncodingDB.getEncodings().get(nameBytes);
        return defineEncoding(entry, nameBytes, 0, nameBytes.length);
    }

    @TruffleBoundary
    public synchronized RubyEncoding replicateEncoding(Encoding encoding, String name) {
        if (getRubyEncoding(name) != null) {
            return null;
        }

        EncodingDB.replicate(name, encoding.toString());
        byte[] nameBytes = RopeOperations.encodeAsciiBytes(name);
        final Entry entry = EncodingDB.getEncodings().get(nameBytes);
        return defineEncoding(entry, nameBytes, 0, nameBytes.length);
    }

    @TruffleBoundary
    public static Charset charsetForEncoding(Encoding encoding) {
        if (encoding == ASCIIEncoding.INSTANCE) {
            throw new UnsupportedOperationException("Cannot return a Charset for the BINARY Ruby Encoding");
        }
        return encoding.getCharset();
    }

    public Encoding getLocaleEncoding() {
        return localeEncoding;
    }

    public void setDefaultExternalEncoding(Encoding defaultExternalEncoding) {
        this.defaultExternalEncoding = defaultExternalEncoding;
    }

    public Encoding getDefaultExternalEncoding() {
        return defaultExternalEncoding;
    }

    public void setDefaultInternalEncoding(Encoding defaultInternalEncoding) {
        this.defaultInternalEncoding = defaultInternalEncoding;
    }

    public Encoding getDefaultInternalEncoding() {
        return defaultInternalEncoding;
    }

    public RubyEncoding getBuiltInEncoding(int index) {
        return BUILT_IN_ENCODINGS[index];
    }

}
