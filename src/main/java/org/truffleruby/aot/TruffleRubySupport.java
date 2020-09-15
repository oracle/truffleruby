/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 *
 * Some of the code in this class is modified from org.jruby.util.StringSupport,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */
package org.truffleruby.aot;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.truffle.api.CompilerDirectives;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.util.ArrayReader;
import org.jcodings.util.CaseInsensitiveBytesHash;

public final class TruffleRubySupport {
    public static final Map<String, EncodingInstance> allEncodings = getEncodings();
    public static final Map<String, byte[]> allJCodingsTables = getJcodingsTables();

    private static Map<String, EncodingInstance> getEncodings() {
        final Map<String, EncodingInstance> encodings = new HashMap<>();
        final CaseInsensitiveBytesHash<EncodingDB.Entry> encodingdb = EncodingDB.getEncodings();
        for (EncodingDB.Entry entry : encodingdb) {
            final String encodingClassName = entry.getEncodingClass();
            final Encoding encoding = entry.getEncoding();
            encodings.put(encodingClassName, new EncodingInstance(encoding, encoding));
        }

        return encodings;
    }

    private static Map<String, byte[]> getJcodingsTables() {
        Map<String, byte[]> jcodingsTables = new HashMap<>();
        Set<String> jcodingsTableNames = new HashSet<>();

        RootedFileVisitor<Path> visitor = new SimpleRootedFileVisitor<Path>() {
            // match files that start with "tables/" and end with ".bin"
            Pattern filePattern = Pattern.compile("^tables/(.*)\\.bin$");

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = getRoot().relativize(file).toString();
                Matcher m = filePattern.matcher(fileName);
                if (m.matches()) {
                    jcodingsTableNames.add(m.group(1));
                }
                return FileVisitResult.CONTINUE;
            }
        };

        RootedFileVisitor.visitEachFileOnClassPath(visitor);

        for (String name : jcodingsTableNames) {
            String entry = "/tables/" + name + ".bin";
            try (InputStream is = ArrayReader.class.getResourceAsStream(entry)) {
                if (is != null) {
                    byte[] buf = new byte[is.available()];
                    try (DataInputStream dataInputStream = new DataInputStream(is)) {
                        dataInputStream.readFully(buf);
                    }
                    jcodingsTables.put(name, buf);
                } else {
                    throw CompilerDirectives.shouldNotReachHere("Unable to load Jcodings table " + name);
                }
            } catch (IOException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
        }
        return jcodingsTables;
    }

    static class EncodingInstance {
        Encoding instance;
        Encoding dummy;

        EncodingInstance(Encoding instance, Encoding dummy) {
            this.instance = instance;
            this.dummy = dummy;
        }

        Encoding get(boolean useDummy) {
            if (useDummy && this.dummy != null) {
                return this.dummy;
            } else {
                return this.instance;
            }
        }
    }

}
