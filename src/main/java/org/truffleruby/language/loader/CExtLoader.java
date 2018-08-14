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

import com.oracle.truffle.api.source.Source;
import org.truffleruby.RubyLanguage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * Loads C extension '.su' files.
 */
public class CExtLoader {

    public void loadLibrary(File file, Consumer<String> handleLibrary, Consumer<Source> handleSource) throws IOException {
        final byte[] buffer = new byte[4096];

        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry = zipStream.getNextEntry();

            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    zipEntry = zipStream.getNextEntry();
                    continue;
                }

                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                while (true) {
                    final int read = zipStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    byteStream.write(buffer, 0, read);
                }

                final byte[] bytes = byteStream.toByteArray();

                if (zipEntry.getName().equals("libs")) {
                    final String libs = byteStream.toString(StandardCharsets.UTF_8.name());
                    try (Scanner scanner = new Scanner(libs)) {
                        while (scanner.hasNextLine()) {
                            handleLibrary.accept(scanner.nextLine());
                        }
                    }
                } else if (zipEntry.getName().endsWith("." + RubyLanguage.CEXT_BITCODE_EXTENSION)) {
                    final String sourceCode = Base64.getEncoder().encodeToString(bytes);
                    final Source source = Source.newBuilder("llvm", sourceCode, file.getPath() + "@" + zipEntry.getName()).mimeType(RubyLanguage.SULONG_BITCODE_BASE64_MIME_TYPE).build();
                    handleSource.accept(source);
                }

                zipEntry = zipStream.getNextEntry();
            }
        }
    }

}
