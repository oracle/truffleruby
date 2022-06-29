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

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Encoding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.parser.ParserRopeOperations;

import com.oracle.truffle.api.source.Source;

public class LexerSource {

    public ParserRopeOperations parserRopeOperations;
    private final Source source;
    private final String sourcePath;
    private final boolean fromTruffleString;

    private TruffleString sourceBytes;
    private final int sourceByteLength;
    private RubyEncoding encoding;
    private int byteOffset;
    private final int lineOffset;

    public LexerSource(RubySource rubySource) {
        this.source = rubySource.getSource();
        this.sourcePath = rubySource.getSourcePath();

        fromTruffleString = rubySource.hasTruffleString();

        final RubyEncoding rubyEncoding;
        if (fromTruffleString) {
            rubyEncoding = rubySource.getEncoding();
            this.sourceBytes = rubySource.getTruffleString();
        } else {
            rubyEncoding = Encodings.UTF_8;
            // TODO CS 5-Sep-17 can we get the bytes directly rather than using getCharacters -> toString -> getBytes?
            var sourceString = source.getCharacters().toString();
            // this.sourceBytes = TStringUtils.fromByteArray(sourceString.getBytes(StandardCharsets.UTF_8), TruffleString.Encoding.UTF_8);
            this.sourceBytes = TStringUtils.utf8TString(sourceString);
        }
        this.sourceByteLength = sourceBytes.byteLength(rubyEncoding.tencoding);
        this.encoding = rubyEncoding;
        parserRopeOperations = new ParserRopeOperations(this.encoding);
        this.lineOffset = rubySource.getLineOffset();
    }

    public Source getSource() {
        return source;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Encoding getEncoding() {
        return encoding.jcoding;
    }

    public RubyEncoding getRubyEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding jcoding) {
        var rubyEncoding = Encodings.getBuiltInEncoding(jcoding);
        this.sourceBytes = sourceBytes.forceEncodingUncached(this.encoding.tencoding, rubyEncoding.tencoding);
        this.encoding = rubyEncoding;
        this.parserRopeOperations = new ParserRopeOperations(this.encoding);
    }

    public int getOffset() {
        return byteOffset;
    }

    public TruffleString gets() {
        if (byteOffset >= sourceByteLength) {
            return null;
        }

        int lineEnd = nextNewLine() + 1;

        if (lineEnd == 0) {
            lineEnd = sourceByteLength;
        }

        final int start = byteOffset;
        final int length = lineEnd - byteOffset;

        byteOffset = lineEnd;

        return parserRopeOperations.makeShared(sourceBytes, start, length);
    }

    private int nextNewLine() {
        // TODO: Try indexOf
        int n = byteOffset;

        while (n < sourceByteLength) {
            if (sourceBytes.readByteUncached(n, encoding.tencoding) == '\n') {
                return n;
            }

            n++;
        }

        return -1;
    }

    public boolean isFromTruffleString() {
        return fromTruffleString;
    }

    public int getLineOffset() {
        return lineOffset;
    }
}
