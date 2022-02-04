/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * This code is modified from the Readline JRuby extension module
 * implementation with the following header:
 *
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
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2006 Damian Steer <pldms@mac.com>
 * Copyright (C) 2008 Joseph LaFata <joe@quibb.org>
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
 */
package org.truffleruby.stdlib.readline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.graalvm.shadowed.org.jline.builtins.Completers;
import org.graalvm.shadowed.org.jline.reader.Completer;
import org.graalvm.shadowed.org.jline.reader.LineReader;
import org.graalvm.shadowed.org.jline.reader.LineReader.Option;
import org.graalvm.shadowed.org.jline.reader.impl.LineReaderImpl;
import org.graalvm.shadowed.org.jline.terminal.Size;
import org.graalvm.shadowed.org.jline.terminal.Terminal;
import org.graalvm.shadowed.org.jline.terminal.impl.DumbTerminal;
import org.graalvm.shadowed.org.jline.terminal.impl.ExecPty;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.support.RubyIO;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class ConsoleHolder {

    // Set to true to print logging messages from JLine
    private static final boolean DEBUG_JLINE = false;

    static {
        if (DEBUG_JLINE) {
            final Level level = Level.FINEST;
            final Logger logger = Logger.getLogger("org.graalvm.shadowed.org.jline");
            logger.setLevel(level);
            final ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(level);
            logger.addHandler(handler);
        }
    }

    private final RubyContext context;
    private final RubyLanguage language;
    private final LineReaderImpl readline;
    private final IoStream in;
    private final IoStream out;

    public static ConsoleHolder create(RubyContext context, RubyLanguage language) {
        final RubyIO stdin = (RubyIO) context.getCoreLibrary().getStdin();
        return new ConsoleHolder(
                context,
                language,
                0,
                stdin,
                1,
                null,
                new Completers.FileNameCompleter(),
                new MemoryHistory(),
                new ParserWithCustomDelimiters());
    }

    /** We need to pass all the state through this constructor in order to keep it when updating the streams. */
    @TruffleBoundary
    private ConsoleHolder(
            RubyContext context,
            RubyLanguage language,
            int inFd,
            RubyIO inIo,
            int outFd,
            RubyIO outIo,
            Completer completer,
            MemoryHistory history,
            ParserWithCustomDelimiters parser) {
        this.context = context;
        this.language = language;
        this.in = new IoStream(context, language, inFd, inIo);
        this.out = new IoStream(context, language, outFd, outIo);

        boolean isTTY = System.console() != null;
        boolean system = isTTY && inFd == 0 && outFd == 1;

        final Terminal terminal;
        try {
            if (system) {
                terminal = new PosixSysTerminalKeepSignalHandlers(
                        "TruffleRuby",
                        getType(),
                        ExecPty.current(),
                        StandardCharsets.UTF_8);
            } else {
                try (Terminal inherit = new DumbTerminal(in.getIn(), out.getOut())) {
                    terminal = new SingleThreadTerminal(
                            "TruffleRuby",
                            getType(),
                            in.getIn(),
                            out.getOut(),
                            StandardCharsets.UTF_8,
                            inherit.getAttributes(),
                            new Size(160, 50));
                }
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Couldn't initialize readline", e);
        }

        readline = new LineReaderImpl(terminal, null, null);
        readline.setHistory(history);
        readline.setParser(parser);
        readline.setCompleter(completer);

        readline.option(Option.DISABLE_EVENT_EXPANSION, true);
        readline.option(Option.HISTORY_BEEP, true);

        if (!system) {
            readline.option(Option.BRACKETED_PASTE, false);
        }
    }

    private String getType() {
        String type = System.getProperty("org.jline.terminal.type");
        if (type == null) {
            type = System.getenv("TERM");
        }
        return type;
    }

    public void close() {
        try {
            final Terminal terminal = readline.getTerminal();
            if (terminal instanceof PosixSysTerminalKeepSignalHandlers) {
                ((PosixSysTerminalKeepSignalHandlers) terminal).customClose();
            } else {
                terminal.close();
            }
        } catch (IOException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    public LineReader getReadline() {
        return readline;
    }

    public MemoryHistory getHistory() {
        return (MemoryHistory) readline.getHistory();
    }

    public ParserWithCustomDelimiters getParser() {
        return (ParserWithCustomDelimiters) readline.getParser();
    }

    public void setCompleter(Completer completer) {
        readline.setCompleter(completer);
    }

    public ConsoleHolder updateIn(int fd, RubyIO io) {
        if (fd == in.getFd()) {
            return this;
        }

        return new ConsoleHolder(
                context,
                language,
                fd,
                io,
                out.getFd(),
                out.getIo(),
                readline.getCompleter(),
                getHistory(),
                getParser());
    }

    public ConsoleHolder updateOut(int fd, RubyIO io) {
        if (fd == out.getFd()) {
            return this;
        }

        return new ConsoleHolder(
                context,
                language,
                in.getFd(),
                in.getIo(),
                fd,
                io,
                readline.getCompleter(),
                getHistory(),
                getParser());
    }

}
