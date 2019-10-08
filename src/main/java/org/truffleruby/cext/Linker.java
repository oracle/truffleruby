/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Linker {

    public static void link(String outputFileName, Collection<String> libraryNames, Collection<String> bitcodeFileNames)
            throws IOException {
        final byte[] buffer = new byte[4096];

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

                final MessageDigest digest;
                try {
                    digest = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

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

}
