/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.services;

import org.junit.Test;
import org.truffleruby.RubyTest;
import org.truffleruby.shared.TruffleRuby;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RubyFileTypeDetectorTest extends RubyTest {

    @Test
    public void testDirect() throws IOException {
        final RubyFileTypeDetector fileTypeDetector = new RubyFileTypeDetector();
        for (Path path : getTestPaths()) {
            assertEquals(TruffleRuby.MIME_TYPE, fileTypeDetector.probeContentType(path));
        }
    }

    @Test
    public void testIndirect() throws IOException {
        for (Path path : getTestPaths()) {
            assertEquals(TruffleRuby.MIME_TYPE, Files.probeContentType(path));
        }
    }

    private static Path[] getTestPaths() throws IOException {
        final Path tempDirectory = Files.createTempDirectory("truffleruby");
        tempDirectory.toFile().deleteOnExit();

        final List<Path> paths = new ArrayList<>();

        paths.add(createFile(tempDirectory, "test.rb", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "TESTUP.RB", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "Gemfile", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "Rakefile", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "Mavenfile", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "test.rake", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "test.gemspec", "puts 'hello'"));
        paths.add(createFile(tempDirectory, "shebang", "#!/usr/bin/ruby\nputs 'hello'"));
        paths.add(createFile(tempDirectory, "env-shebang", "#!/usr/bin/env ruby\nputs 'hello'"));

        return paths.toArray(new Path[paths.size()]);
    }

    private static Path createFile(Path parent, String name, String contents) throws IOException {
        final Path file = Files.createFile(parent.resolve(name));
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        return file;
    }

}
