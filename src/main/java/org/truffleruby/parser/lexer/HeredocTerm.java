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
 * Copyright (C) 2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004-2007 Thomas E Enebo <enebo@acm.org>
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

import static org.truffleruby.parser.lexer.RubyLexer.EOF;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_END;
import static org.truffleruby.parser.lexer.RubyLexer.STR_FUNC_EXPAND;
import static org.truffleruby.parser.lexer.RubyLexer.STR_FUNC_INDENT;
import static org.truffleruby.parser.lexer.RubyLexer.STR_FUNC_TERM;

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Encoding;
import org.truffleruby.core.string.TStringBuilder;
import org.truffleruby.parser.parser.RubyParser;

/** A lexing unit for scanning a heredoc element. Example:
 * 
 * <pre>
 * foo(<<EOS, bar)
 * This is heredoc country!
 * EOF
 *
 * Where:
 * EOS = marker
 * ',bar)\n' = lastLine
 * </pre>
 */
public final class HeredocTerm extends StrTerm {
    /** End marker delimiting heredoc boundary. */
    private final TruffleString nd_lit;

    /** Indicates whether string interpolation (expansion) should be performed, and the identation of the end marker. */
    private final int flags;

    /** End position of the end marker on the line where it is declared. */
    final int nth;

    /** Line index of the line where the end marker is declared (1-based). */
    final int line;

    /** Portion of the line where the end marker is declarer, from right after the marker until the end of the line. */
    final TruffleString lastLine;

    public HeredocTerm(TruffleString marker, int func, int nth, int line, TruffleString lastLine) {
        this.nd_lit = marker;
        this.flags = func;
        this.nth = nth;
        this.line = line;
        this.lastLine = lastLine;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    protected int error(RubyLexer lexer, TruffleString eos) {
        lexer.compile_error("can't find string \"" + eos.toJavaStringUncached() + "\" anywhere before EOF");
        return -1;
    }

    private int restore(RubyLexer lexer) {
        lexer.heredoc_restore(this);
        // this will cause the next call to RubyLexer#yylex() to emit the RubyParser.tSTRING_END token
        lexer.setStrTerm(new StringTerm(flags | STR_FUNC_TERM, 0, 0, line));
        return EOF;
    }

    @Override
    public int parseString(RubyLexer lexer) {
        TStringBuilder str = null;
        boolean indent = (flags & STR_FUNC_INDENT) != 0;
        int c = lexer.nextc();

        if (c == EOF) {
            return error(lexer, nd_lit);
        }

        // Found end marker for this heredoc, at the start of a line
        if (lexer.was_bol() && lexer.whole_match_p(this.nd_lit, lexer.tencoding, indent)) {
            lexer.heredoc_restore(this); // will also skip over the end marker
            lexer.setStrTerm(null);
            lexer.setState(EXPR_END);
            return RubyParser.tSTRING_END;
        }

        if ((flags & STR_FUNC_EXPAND) == 0) {
            // heredocs without string interpolation

            do { // iterate on lines, while end marker not found
                final TruffleString lbuf = lexer.lexb;
                int pend = lexer.lex_pend;

                // Remove trailing newline, it will be appended later in normalized form (single \n).
                if (pend > 0) {
                    switch (lexer.p(pend - 1)) {
                        case '\n':
                            pend--;
                            if (pend > 0 && lexer.p(pend - 1) == '\r') {
                                pend--;
                            }
                            break;
                        case '\r':
                            pend--;
                            break;
                    }
                }

                // if we are dealing with a squiggly heredoc
                if (lexer.getHeredocIndent() > 0) {
                    // update the indent for the current line
                    for (int i = 0; i < pend && lexer.update_heredoc_indent(lexer.p(i)); i++) {
                    }
                    // reset heredoc_line_indent to 0 (was -1 after we matched the first non-whitespace character)
                    lexer.setHeredocLineIndent(0);
                }

                var bytes = lbuf.getInternalByteArrayUncached(lexer.getTEncoding());
                if (str != null) {
                    str.append(bytes.getArray(), bytes.getOffset(), pend);
                } else {
                    // lazy initialization of string builder
                    final TStringBuilder builder = TStringBuilder.create(bytes.getArray(), bytes.getOffset(),
                            pend);
                    builder.setEncoding(lexer.encoding);
                    str = builder;
                }

                // append the newline that we removed earlier
                str.append('\n');
                lexer.lex_goto_eol();

                if (lexer.getHeredocIndent() > 0) {
                    // for squiggly (indented) heredocs, generate one string content token token per line
                    // this will be dedented in the parser through lexer.heredoc_dedent
                    lexer.setValue(lexer.createStr(str, 0));
                    return RubyParser.tSTRING_CONTENT;
                }
                // MRI null checks str in this case but it is unconditionally non-null?
                if (lexer.nextc() == -1) {
                    return error(lexer, nd_lit);
                }
            } while (!lexer.whole_match_p(nd_lit, lexer.tencoding, indent));
        } else {
            // heredoc with string interpolation

            TStringBuilder tok = new TStringBuilder();
            tok.setEncoding(lexer.encoding);

            if (c == '#') {
                // interpolated variable or block begin
                // This returns tSTRING_DVAR (if it finds $, @ or @@), tSTRING_DBEG (if it finds '{'), or 0 (none of
                // these things were found).
                int token = lexer.peekVariableName(RubyParser.tSTRING_DVAR, RubyParser.tSTRING_DBEG);
                if (token != 0) {
                    // Emit the token - note that the parser will unset RubyLexer#lex_strTerm while the variable or
                    // block is being parse and restore it when it is done, allowing the rest of the heredoc to be
                    // processed.
                    return token;
                }
                tok.append('#');
            }

            // MRI has extra pointer which makes our code look a little bit more strange in comparison
            do {
                lexer.pushback(c);

                Encoding[] enc = new Encoding[1];
                enc[0] = lexer.getEncoding();

                // Parse the next string segment into the buffer, as a regular string (with expansion).
                // The segment might terminate because of a newline, line continuation (\\) or because of a
                // an interpolation (#{...}, #@foo, #$foo, etc).
                if ((c = new StringTerm(flags, '\0', '\n', lexer.ruby_sourceline)
                        .parseStringIntoBuffer(lexer, tok, enc)) == EOF) {
                    if (lexer.eofp) {
                        return error(lexer, nd_lit);
                    }
                    return restore(lexer);
                }

                if (c != '\n') {
                    // emit the string segment
                    lexer.setValue(lexer.createStr(tok, 0));
                    return RubyParser.tSTRING_CONTENT;
                }

                // append the terminating newline
                tok.append(lexer.nextc());

                if (lexer.getHeredocIndent() > 0) {
                    // for squiggly (indented) heredocs, generate one string content token token per line
                    // this will be dedented in the parser through lexer.heredoc_dedent
                    lexer.lex_goto_eol();
                    lexer.setValue(lexer.createStr(tok, 0));
                    return RubyParser.tSTRING_CONTENT;
                }

                if ((c = lexer.nextc()) == EOF) {
                    return error(lexer, nd_lit);
                }
                // NOTE: The end marker is not processed here, but in the next call to HeredocTerm#parseString
            } while (!lexer.whole_match_p(nd_lit, lexer.tencoding, indent));
            str = tok;
        }

        lexer.pushback(c);
        lexer.setValue(lexer.createStr(str, 0));
        return RubyParser.tSTRING_CONTENT;
    }
}
