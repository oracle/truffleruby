/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * Contains code modified from JRuby's RubyConverter.java and licensed under the same EPL1.0/GPL 2.0/LGPL 2.1
 * used throughout.
 *
 * Contains code modified jcodings's TranscoderDB.java and EConv.java, which is licensed under the MIT license:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.jcodings.transcode.EConvFlags;
import org.jcodings.transcode.Transcoder;
import org.jcodings.transcode.TranscoderDB;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TranscodingManager {

    public static final Map<String, Map<String, Transcoder>> allTranscoders;

    static {
        allTranscoders = new HashMap<>();

        for (CaseInsensitiveBytesHash<TranscoderDB.Entry> sourceEntry : TranscoderDB.transcoders) {
            for (Hash.HashEntry<TranscoderDB.Entry> destinationEntry : sourceEntry.entryIterator()) {
                final TranscoderDB.Entry e = destinationEntry.value;

                final String sourceName = new String(e.getSource()).toUpperCase();
                final String destinationName = new String(e.getDestination()).toUpperCase();

                final Transcoder transcoder;
                if (TruffleOptions.AOT) {
                    // Load the classes eagerly
                    transcoder = e.getTranscoder();
                } else {
                    transcoder = null;
                }

                allTranscoders.putIfAbsent(sourceName, new HashMap<>());
                final Map<String, Transcoder> fromSource = allTranscoders.get(sourceName);
                fromSource.put(destinationName, transcoder);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @TruffleBoundary
    public static List<String> bfs(String sourceEncodingName, String destinationEncodingName) {
        final Deque<String> queue = new ArrayDeque<>();
        final HashMap<String, String> invertedList = new HashMap<>();

        invertedList.put(sourceEncodingName, null);

        queue.add(sourceEncodingName);
        while (!queue.isEmpty()) {
            String current = queue.pop();

            for (String child : allTranscoders.get(current).keySet()) {
                if (invertedList.containsKey(child)) {
                    // We've already visited this path or are scheduled to.
                    continue;
                }

                if (child.equals(destinationEncodingName)) {
                    // Search finished.
                    final LinkedList<String> ret = new LinkedList<>();
                    ret.add(child);

                    String next = current;
                    while (next != null) {
                        ret.addFirst(next);
                        next = invertedList.get(next);
                    }

                    return ret;
                }

                invertedList.put(child, current);
                queue.add(child);
            }
        }

        return Collections.emptyList();
    }

}
