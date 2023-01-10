/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileTypeDetectionTest {

    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            System.out.println(Files.probeContentType(Paths.get(arg)));
        }
    }

}
