/*
 * Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.readline;

import java.util.List;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.shadowed.org.jline.reader.Buffer;
import org.graalvm.shadowed.org.jline.reader.Candidate;
import org.graalvm.shadowed.org.jline.reader.Completer;
import org.graalvm.shadowed.org.jline.reader.EndOfFileException;
import org.graalvm.shadowed.org.jline.reader.LineReader;
import org.graalvm.shadowed.org.jline.reader.ParsedLine;
import org.graalvm.shadowed.org.jline.reader.UserInterruptException;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.BooleanCastWithDefaultNode;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.support.RubyIO;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.interop.ToJavaStringWithDefaultNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

@CoreModule("Truffle::Readline")
public abstract class ReadlineNodes {

    @CoreMethod(names = "basic_word_break_characters", onSingleton = true)
    public abstract static class BasicWordBreakCharactersNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        RubyString basicWordBreakCharacters() {
            final String delimiters = getContext().getConsoleHolder().getParser().getDelimiters();
            return createString(fromJavaStringNode, delimiters, Encodings.UTF_8);
        }

    }

    @CoreMethod(names = "basic_word_break_characters=", onSingleton = true, required = 1)
    @NodeChild(value = "characters", type = RubyBaseNodeWithExecute.class)
    public abstract static class SetBasicWordBreakCharactersNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(this, charactersAsString)", limit = "1")
        static Object setBasicWordBreakCharacters(Object characters,
                @Cached ToStrNode toStrNode,
                @Cached RubyStringLibrary strings,
                @Bind("this") Node node,
                @Bind("toStrNode.execute(node, characters)") Object charactersAsString) {
            final String delimiters = RubyGuards.getJavaString(charactersAsString);
            getContext(node).getConsoleHolder().getParser().setDelimiters(delimiters);
            return charactersAsString;
        }

    }

    @Primitive(name = "readline_set_completion_proc")
    public abstract static class CompletionProcSetNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        RubyProc setCompletionProc(RubyProc proc) {
            final ProcCompleter completer = new ProcCompleter(getContext(), getLanguage(), proc);
            getContext().getConsoleHolder().setCompleter(completer);
            return proc;
        }

    }

    @CoreMethod(names = "get_screen_size", onSingleton = true)
    public abstract static class GetScreenSizeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        RubyArray getScreenSize() {
            final LineReader readline = getContext().getConsoleHolder().getReadline();
            final int[] store = {
                    readline.getTerminal().getHeight(),
                    readline.getTerminal().getWidth()
            };

            return ArrayHelpers.createArray(getContext(), getLanguage(), store);
        }

    }

    @CoreMethod(names = "readline", isModuleFunction = true, optional = 2)
    public abstract static class ReadlineNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        Object readline(Object maybePromptObject, Object maybeAddToHistory,
                @Cached ToJavaStringWithDefaultNode toJavaStringWithDefaultNode,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                @Cached BooleanCastWithDefaultNode booleanCastWithDefaultNode) {
            final var prompt = toJavaStringWithDefaultNode.execute(this, maybePromptObject, "");
            final boolean addToHistory = booleanCastWithDefaultNode.execute(this, maybeAddToHistory, false);
            final LineReader readline = getContext().getConsoleHolder().getReadline();

            // Use a Memo as readLine() can return null on Ctrl+D and we should not retry
            final Memo<String> result = new Memo<>(null);

            getContext().getThreadManager().runUntilResult(this, () -> {
                String line;
                try {
                    line = readline.readLine(prompt);
                } catch (EndOfFileException e) {
                    line = null;
                } catch (UserInterruptException e) {
                    throw new InterruptedException();
                }
                result.set(line);
                return BlockingAction.SUCCESS;
            });

            final String value = result.get();
            if (value == null) { // EOF
                return nil;
            } else {
                if (addToHistory) {
                    readline.getHistory().add(value);
                }

                return createString(
                        fromJavaStringNode,
                        value,
                        getContext().getEncodingManager().getDefaultExternalEncoding());
            }
        }

    }

    @CoreMethod(names = "point", onSingleton = true)
    public abstract static class PointNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        int point() {
            return getContext().getConsoleHolder().getReadline().getBuffer().cursor();
        }

    }

    @CoreMethod(names = "insert_text", constructor = true, required = 1)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "text", type = RubyNode.class)
    public abstract static class InsertTextNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        RubyBasicObject insertText(RubyBasicObject readline, Object text,
                @Cached ToJavaStringNode toJavaStringNode) {
            final var textAsString = toJavaStringNode.execute(this, text);
            getContext().getConsoleHolder().getReadline().getBuffer().write(textAsString);
            return readline;
        }
    }

    @CoreMethod(names = "delete_text", constructor = true)
    public abstract static class DeleteTextNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        RubyBasicObject deleteText(RubyBasicObject readline) {
            getContext().getConsoleHolder().getReadline().getBuffer().clear();
            return readline;
        }

    }

    @CoreMethod(names = "line_buffer", onSingleton = true)
    public abstract static class LineBufferNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        Object lineBuffer() {
            final Buffer buffer = getContext().getConsoleHolder().getReadline().getBuffer();

            return createString(
                    fromJavaStringNode,
                    buffer.toString(),
                    getLocaleEncoding());
        }

    }

    @Primitive(name = "readline_set_input", lowerFixnum = 0)
    public abstract static class SetInputNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        int setInput(int fd, RubyIO io) {
            final ConsoleHolder oldConsoleHolder = getContext().getConsoleHolder();
            final ConsoleHolder newConsoleHolder = oldConsoleHolder.updateIn(fd, io);

            if (newConsoleHolder != oldConsoleHolder) {
                getContext().setConsoleHolder(newConsoleHolder);
            }

            return fd;
        }

    }

    @Primitive(name = "readline_set_output", lowerFixnum = 0)
    public abstract static class SetOutputNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        int setOutput(int fd, RubyIO io) {
            final ConsoleHolder oldConsoleHolder = getContext().getConsoleHolder();
            final ConsoleHolder newConsoleHolder = oldConsoleHolder.updateOut(fd, io);

            if (newConsoleHolder != oldConsoleHolder) {
                getContext().setConsoleHolder(newConsoleHolder);
            }

            return fd;
        }

    }

    // Complete using a Proc object
    private static final class ProcCompleter implements Completer {

        private final RubyContext context;
        private final RubyLanguage language;
        private final RubyProc proc;

        public ProcCompleter(RubyContext context, RubyLanguage language, RubyProc proc) {
            this.context = context;
            this.language = language;
            this.proc = proc;
        }

        @Override
        public void complete(LineReader lineReader, ParsedLine commandLine, List<Candidate> candidates) {
            String buffer = commandLine.word().substring(0, commandLine.wordCursor());
            String after = commandLine.word().substring(commandLine.wordCursor());
            boolean complete = lineReader.getBuffer().cursor() == lineReader.getBuffer().length();

            RubyString string = StringOperations.createUTF8String(context, language, buffer);
            RubyArray completions = (RubyArray) DispatchNode.getUncached().call(proc, "call", string);
            for (Object element : ArrayOperations.toIterable(completions)) {
                final String completion = RubyGuards.getJavaString(element);
                candidates.add(new Candidate(completion + after, completion, null, null, null, null, complete));
            }
        }
    }
}
