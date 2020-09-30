/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.readline;

import java.util.List;

import org.graalvm.shadowed.org.jline.reader.Buffer;
import org.graalvm.shadowed.org.jline.reader.Candidate;
import org.graalvm.shadowed.org.jline.reader.Completer;
import org.graalvm.shadowed.org.jline.reader.EndOfFileException;
import org.graalvm.shadowed.org.jline.reader.LineReader;
import org.graalvm.shadowed.org.jline.reader.ParsedLine;
import org.graalvm.shadowed.org.jline.reader.UserInterruptException;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.support.RubyIO;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.interop.ToJavaStringWithDefaultNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

@CoreModule("Truffle::Readline")
public abstract class ReadlineNodes {

    @CoreMethod(names = "basic_word_break_characters", onSingleton = true)
    public abstract static class BasicWordBreakCharactersNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString basicWordBreakCharacters() {
            final String delimiters = getContext().getConsoleHolder().getParser().getDelimiters();
            return makeStringNode.executeMake(delimiters, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "basic_word_break_characters=", onSingleton = true, required = 1)
    @NodeChild(value = "characters", type = RubyNode.class)
    public abstract static class SetBasicWordBreakCharactersNode extends CoreMethodNode {

        @CreateCast("characters")
        protected RubyNode coerceCharactersToString(RubyNode characters) {
            return ToStrNodeGen.create(characters);
        }

        @TruffleBoundary
        @Specialization
        protected RubyString setBasicWordBreakCharacters(RubyString characters) {
            final String delimiters = characters.getJavaString();
            getContext().getConsoleHolder().getParser().setDelimiters(delimiters);
            return characters;
        }

    }

    @Primitive(name = "readline_set_completion_proc")
    public abstract static class CompletionProcSetNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyProc setCompletionProc(RubyProc proc) {
            final ProcCompleter completer = new ProcCompleter(getContext(), proc);
            getContext().getConsoleHolder().setCompleter(completer);
            return proc;
        }

    }

    @CoreMethod(names = "get_screen_size", onSingleton = true)
    public abstract static class GetScreenSizeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray getScreenSize() {
            final LineReader readline = getContext().getConsoleHolder().getReadline();
            final int[] store = {
                    readline.getTerminal().getHeight(),
                    readline.getTerminal().getWidth()
            };

            return ArrayHelpers.createArray(getContext(), store);
        }

    }

    @CoreMethod(names = "readline", isModuleFunction = true, optional = 2)
    @NodeChild(value = "prompt", type = RubyNode.class)
    @NodeChild(value = "addToHistory", type = RubyNode.class)
    public abstract static class ReadlineNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @CreateCast("prompt")
        protected RubyNode coercePromptToJavaString(RubyNode prompt) {
            return ToJavaStringWithDefaultNodeGen.create("", prompt);
        }

        @CreateCast("addToHistory")
        protected RubyNode coerceToBoolean(RubyNode addToHistory) {
            return BooleanCastWithDefaultNodeGen.create(false, addToHistory);
        }

        @TruffleBoundary
        @Specialization
        protected Object readline(String prompt, boolean addToHistory,
                @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary) {
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

                final RubyString ret = makeStringNode.executeMake(
                        value,
                        getContext().getEncodingManager().getDefaultExternalEncoding(),
                        CodeRange.CR_UNKNOWN);
                rubyLibrary.taint(ret);
                return ret;
            }
        }

    }

    @CoreMethod(names = "point", onSingleton = true)
    public abstract static class PointNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int point() {
            return getContext().getConsoleHolder().getReadline().getBuffer().cursor();
        }

    }

    @CoreMethod(names = "insert_text", constructor = true, required = 1)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "text", type = RubyNode.class)
    public abstract static class InsertTextNode extends CoreMethodNode {

        @CreateCast("text")
        protected RubyNode coerceTextToString(RubyNode text) {
            return ToJavaStringNode.create(text);
        }

        @TruffleBoundary
        @Specialization
        protected RubyBasicObject insertText(RubyBasicObject readline, String text) {
            getContext().getConsoleHolder().getReadline().getBuffer().write(text);
            return readline;
        }

    }

    @CoreMethod(names = "delete_text", constructor = true)
    public abstract static class DeleteTextNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyBasicObject deleteText(RubyBasicObject readline) {
            getContext().getConsoleHolder().getReadline().getBuffer().clear();
            return readline;
        }

    }

    @CoreMethod(names = "line_buffer", onSingleton = true)
    public abstract static class LineBufferNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object lineBuffer(
                @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary) {
            final Buffer buffer = getContext().getConsoleHolder().getReadline().getBuffer();

            final RubyString ret = makeStringNode
                    .executeMake(buffer.toString(), getLocaleEncoding(), CodeRange.CR_UNKNOWN);
            rubyLibrary.taint(ret);
            return ret;
        }

    }

    @Primitive(name = "readline_set_input", lowerFixnum = 0)
    public abstract static class SetInputNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int setInput(int fd, RubyIO io) {
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
        protected int setOutput(int fd, RubyIO io) {
            final ConsoleHolder oldConsoleHolder = getContext().getConsoleHolder();
            final ConsoleHolder newConsoleHolder = oldConsoleHolder.updateOut(fd, io);

            if (newConsoleHolder != oldConsoleHolder) {
                getContext().setConsoleHolder(newConsoleHolder);
            }

            return fd;
        }

    }

    // Complete using a Proc object
    private static class ProcCompleter implements Completer {

        private final RubyContext context;
        private final RubyProc proc;

        public ProcCompleter(RubyContext context, RubyProc proc) {
            this.context = context;
            this.proc = proc;
        }

        @Override
        public void complete(LineReader lineReader, ParsedLine commandLine, List<Candidate> candidates) {
            String buffer = commandLine.word().substring(0, commandLine.wordCursor());
            String after = commandLine.word().substring(commandLine.wordCursor());
            boolean complete = lineReader.getBuffer().cursor() == lineReader.getBuffer().length();

            RubyString string = StringOperations
                    .createString(context, StringOperations.encodeRope(buffer, UTF8Encoding.INSTANCE));
            RubyArray completions = (RubyArray) context.send(proc, "call", string);
            for (Object element : ArrayOperations.toIterable(completions)) {
                final String completion = ((RubyString) element).getJavaString();
                candidates.add(new Candidate(completion + after, completion, null, null, null, null, complete));
            }
        }
    }
}
