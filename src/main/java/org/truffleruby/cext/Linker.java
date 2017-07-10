/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.source.Source;

public class Linker {

    private static final int BUFFER_SIZE = 4096;

    private static final String LLVM_BITCODE_EXTENSION = "bc";

    public static void link(String outputFileName, Collection<String> libraryNames, Collection<String> bitcodeFileNames) throws IOException, NoSuchAlgorithmException {
        final byte[] buffer = new byte[BUFFER_SIZE];

        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outputFileName))) {
            outputStream.putNextEntry(new ZipEntry("libs"));

            PrintStream libsStream = new PrintStream(outputStream);

            for (String libraryName : libraryNames) {
                libsStream.println(libraryName);
            }

            libsStream.flush();

            outputStream.closeEntry();

            for (String bitcodeFileName : bitcodeFileNames) {
                final File bitcodeFile = new File(bitcodeFileName);

                final MessageDigest digest = MessageDigest.getInstance("SHA-1");

                try (RandomAccessFile inputFile = new RandomAccessFile(bitcodeFile, "r")) {
                    while (true) {
                        int count = inputFile.read(buffer);

                        if (count == -1) {
                            break;
                        }

                        digest.update(buffer, 0, count);
                    }

                    final String digestString = new BigInteger(1, digest.digest()).toString(16);
                    final String entryName = String.format("%s_%s", digestString, bitcodeFile.getName());

                    outputStream.putNextEntry(new ZipEntry(entryName));

                    inputFile.seek(0);

                    while (true) {
                        int count = inputFile.read(buffer);

                        if (count == -1) {
                            break;
                        }

                        outputStream.write(buffer, 0, count);
                    }

                    outputStream.closeEntry();
                }
            }
        }
    }

    public static void loadLibrary(File file, Consumer<String> handleLibrary, Consumer<Source> handleSource) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];

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
                } else if (zipEntry.getName().endsWith("." + LLVM_BITCODE_EXTENSION)) {
                    final String sourceCode = Base64.getEncoder().encodeToString(bytes);
                    final Source source = Source.newBuilder(sourceCode).name(file.getPath() + "@" + zipEntry.getName()).mimeType(RubyLanguage.SULONG_BITCODE_BASE64_MIME_TYPE).build();
                    handleSource.accept(source);
                }

                zipEntry = zipStream.getNextEntry();
            }
        }
    }

}
