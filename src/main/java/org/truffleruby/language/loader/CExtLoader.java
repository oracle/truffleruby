/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.graalvm.polyglot.io.ByteSequence;
import org.truffleruby.RubyLanguage;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.source.Source;

/*
 * Loads C extension '.su' files.
 */
public class CExtLoader {

    private final Consumer<String> handleLibrary;
    private final Consumer<Source> handleSource;

    public CExtLoader(Consumer<String> handleLibrary, Consumer<Source> handleSource) {
        this.handleLibrary = handleLibrary;
        this.handleSource = handleSource;
    }

    public void loadLibrary(String path) throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry zipEntry = zipStream.getNextEntry();

            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    zipEntry = zipStream.getNextEntry();
                    continue;
                }

                loadZipFileEntry(path, zipStream, zipEntry);

                zipEntry = zipStream.getNextEntry();
            }
        }
    }

    private void loadZipFileEntry(String path, ZipInputStream zipStream, ZipEntry zipEntry) throws IOException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        final byte[] buffer = new byte[4096];

        while (true) {
            final int read = zipStream.read(buffer);
            if (read == -1) {
                break;
            }
            byteStream.write(buffer, 0, read);
        }

        final byte[] sourceBytes = byteStream.toByteArray();

        if (zipEntry.getName().equals("libs")) {
            loadLibsFile(sourceBytes);
        } else if (zipEntry.getName().endsWith("." + RubyLanguage.CEXT_BITCODE_EXTENSION)) {
            loadBitcode(path, zipEntry.getName(), sourceBytes);
        } else {
            // Ignore extra files
        }
    }

    private void loadLibsFile(byte[] sourceBytes) {
        final String libs = new String(sourceBytes, StandardCharsets.UTF_8);

        try (Scanner scanner = new Scanner(libs)) {
            while (scanner.hasNextLine()) {
                handleLibrary.accept(scanner.nextLine());
            }
        }
    }

    private void loadBitcode(String path, String entryName, byte[] sourceBytes) {
        final String name = String.format("%s@%s", path, entryName);
        final Source source = Source
                .newBuilder(TruffleRuby.LLVM_ID, ByteSequence.create(sourceBytes), name)
                .mimeType(RubyLanguage.LLVM_BITCODE_MIME_TYPE)
                .build();
        handleSource.accept(source);
    }


}
