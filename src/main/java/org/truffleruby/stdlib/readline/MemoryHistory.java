/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code from Jline3's DefaultHistory class:
 * Copyright (c) 2002-2018, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.truffleruby.stdlib.readline;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

import org.graalvm.shadowed.org.jline.reader.History;
import org.graalvm.shadowed.org.jline.reader.LineReader;

import static org.graalvm.shadowed.org.jline.reader.LineReader.HISTORY_IGNORE;
import static org.graalvm.shadowed.org.jline.reader.impl.ReaderUtils.getBoolean;
import static org.graalvm.shadowed.org.jline.reader.impl.ReaderUtils.getString;
import static org.graalvm.shadowed.org.jline.reader.impl.ReaderUtils.isSet;

/** Since JLine3 has no MemoryHistory and we need a mutable history because the Ruby API exposes it, we have to write
 * our own. */
public class MemoryHistory implements History {

    private final LinkedList<Entry> entries = new LinkedList<>();
    private int index = 0;

    private LineReader reader;

    @Override
    public void attach(LineReader lineReader) {
        if (this.reader != lineReader) {
            this.reader = lineReader;
        }
    }

    @Override
    public void load() throws IOException {
    }

    @Override
    public void save() throws IOException {
    }

    @Override
    public void write(Path path, boolean incremental) throws IOException {
    }

    @Override
    public void append(Path path, boolean incremental) throws IOException {
    }

    @Override
    public void read(Path path, boolean incremental) throws IOException {
    }

    @Override
    public void purge() throws IOException {
        entries.clear();
        index = 0;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int first() {
        return 0;
    }

    @Override
    public int last() {
        return entries.size() - 1;
    }

    @Override
    public String get(int i) {
        return entries.get(i).line();
    }

    @Override
    public void add(Instant time, String line) {
        Objects.requireNonNull(time);
        Objects.requireNonNull(line);

        if (getBoolean(reader, LineReader.DISABLE_HISTORY, false)) {
            return;
        }
        if (isSet(reader, LineReader.Option.HISTORY_IGNORE_SPACE) && line.startsWith(" ")) {
            return;
        }
        if (isSet(reader, LineReader.Option.HISTORY_REDUCE_BLANKS)) {
            line = line.strip();
        }
        if (isSet(reader, LineReader.Option.HISTORY_IGNORE_DUPS)) {
            if (!entries.isEmpty() && line.equals(entries.getLast().line())) {
                return;
            }
        }
        if (matchPatterns(getString(reader, HISTORY_IGNORE, ""), line)) {
            return;
        }

        entries.add(new SimpleEntry(entries.size(), line, time));
        moveToEnd();
    }

    protected boolean matchPatterns(String patterns, String line) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < patterns.length(); i++) {
            char ch = patterns.charAt(i);
            if (ch == '\\') {
                ch = patterns.charAt(++i);
                builder.append(ch);
            } else if (ch == ':') {
                builder.append('|');
            } else if (ch == '*') {
                builder.append('.').append('*');
            }
        }
        return line.matches(builder.toString());
    }

    @Override
    public String current() {
        return entries.get(index).line();
    }

    @Override
    public boolean previous() {
        if (index > 0) {
            index--;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean next() {
        if (index < size()) {
            index++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean moveToFirst() {
        if (size() > 0 && index != 0) {
            index = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean moveToLast() {
        int lastEntry = size() - 1;
        if (lastEntry >= 0 && lastEntry != index) {
            index = size() - 1;
            return true;
        }

        return false;
    }

    @Override
    public boolean moveTo(int i) {
        if (i >= 0 && i < size()) {
            this.index = i;
            return true;
        }
        return false;
    }

    @Override
    public void moveToEnd() {
        index = size();
    }

    @Override
    public void resetIndex() {
        index = Math.min(index, entries.size());
    }

    public void set(int i, String line) {
        entries.set(i, new SimpleEntry(i, line, Instant.now()));
    }

    public Entry remove(int i) {
        final Entry entry = entries.remove(i);
        resetIndex();
        return entry;
    }

    public Entry removeFirst() {
        return remove(first());
    }

    public Entry removeLast() {
        return remove(last());
    }

    @Override
    public ListIterator<Entry> iterator(int i) {
        return entries.listIterator(i);
    }

    private static class SimpleEntry implements Entry {
        private final int index;
        private final String line;
        private final Instant time;

        private SimpleEntry(int index, String line, Instant time) {
            this.index = index;
            this.line = line;
            this.time = time;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public String line() {
            return line;
        }

        @Override
        public Instant time() {
            return time;
        }
    }

}
