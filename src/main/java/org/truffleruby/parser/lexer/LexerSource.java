/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2004-2006 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 Zach Dennis <zdennis@mktec.com>
 * Copyright (C) 2007-2010 JRuby Community
 * Copyright (C) 2007 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.truffleruby.parser.lexer;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

import java.nio.charset.StandardCharsets;

import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.parser.ParserRopeOperations;

import com.oracle.truffle.api.source.Source;

public class LexerSource {

    private final ParserRopeOperations parserRopeOperations = new ParserRopeOperations();
    private final Source source;
    private final String sourcePath;
    private final boolean fromRope;

    private Rope sourceBytes;
    private int byteOffset;
    private final int lineOffset;

    public LexerSource(RubySource rubySource, Encoding encoding) {
        this.source = rubySource.getSource();
        this.sourcePath = rubySource.getSourcePath();

        fromRope = rubySource.getRope() != null;

        if (fromRope) {
            this.sourceBytes = rubySource.getRope();
        } else {
            // TODO CS 5-Sep-17 can we get the bytes directly rather than using getCharacters ->  toString -> getBytes?
            this.sourceBytes = RopeOperations
                    .create(source.getCharacters().toString().getBytes(StandardCharsets.UTF_8), encoding, CR_UNKNOWN);
        }
        this.lineOffset = rubySource.getLineOffset();
    }

    public Source getSource() {
        return source;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Encoding getEncoding() {
        return sourceBytes.getEncoding();
    }

    public void setEncoding(Encoding encoding) {
        sourceBytes = parserRopeOperations.withEncoding(sourceBytes, encoding);
    }

    public int getOffset() {
        return byteOffset;
    }

    public Rope gets() {
        if (byteOffset >= sourceBytes.byteLength()) {
            return null;
        }

        int lineEnd = nextNewLine() + 1;

        if (lineEnd == 0) {
            lineEnd = sourceBytes.byteLength();
        }

        final int start = byteOffset;
        final int length = lineEnd - byteOffset;

        byteOffset = lineEnd;

        final Rope line = parserRopeOperations.makeShared(sourceBytes, start, length);
        assert line.getEncoding() == sourceBytes.getEncoding();
        return line;
    }

    private int nextNewLine() {
        int n = byteOffset;

        final byte[] bytes = sourceBytes.getBytes();
        while (n < bytes.length) {
            if (bytes[n] == '\n') {
                return n;
            }

            n++;
        }

        return -1;
    }

    public boolean isFromRope() {
        return fromRope;
    }

    public int getLineOffset() {
        return lineOffset;
    }
}
