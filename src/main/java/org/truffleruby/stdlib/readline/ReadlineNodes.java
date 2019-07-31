/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * This code is modified from the Readline JRuby extension module
 * implementation with the following header:
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.interop.ToJavaStringNodeGen;
import org.truffleruby.interop.ToJavaStringWithDefaultNodeGen;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.TaintNode;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

@CoreClass("Truffle::Readline")
public abstract class ReadlineNodes {

    @CoreMethod(names = "basic_word_break_characters", onSingleton = true)
    public abstract static class BasicWordBreakCharactersNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject basicWordBreakCharacters() {
            return makeStringNode.executeMake(ProcCompleter.getDelimiter(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "basic_word_break_characters=", onSingleton = true, required = 1)
    @NodeChild(value = "characters", type = RubyNode.class)
    public abstract static class SetBasicWordBreakCharactersNode extends CoreMethodNode {

        @CreateCast("characters") public RubyNode coerceCharactersToString(RubyNode characters) {
            return ToStrNodeGen.create(characters);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject setBasicWordBreakCharacters(DynamicObject characters) {
            ProcCompleter.setDelimiter(StringOperations.getString(characters));
            return characters;
        }

    }

    @Primitive(name = "readline_set_completion_proc")
    public abstract static class CompletionProcSetNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(proc)")
        public DynamicObject setCompletionProc(DynamicObject proc) {
            final ProcCompleter completer = new ProcCompleter(getContext(), proc);
            getContext().getConsoleHolder().setCurrentCompleter(completer);
            return proc;
        }

    }

    @CoreMethod(names = "get_screen_size", onSingleton = true)
    public abstract static class GetScreenSizeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject getScreenSize() {
            final ConsoleReader readline = getContext().getConsoleHolder().getReadline();
            final int[] store = {
                    readline.getTerminal().getHeight(),
                    readline.getTerminal().getWidth()
            };

            return ArrayHelpers.createArray(getContext(), store, 2);
        }

    }

    @CoreMethod(names = "readline", isModuleFunction = true, optional = 2)
    @NodeChild(value = "prompt", type = RubyNode.class)
    @NodeChild(value = "addToHistory", type = RubyNode.class)
    public abstract static class ReadlineNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private TaintNode taintNode = TaintNode.create();

        @CreateCast("prompt") public RubyNode coercePromptToJavaString(RubyNode prompt) {
            return ToJavaStringWithDefaultNodeGen.create(coreStrings().EMPTY_STRING.toString(), prompt);
        }

        @CreateCast("addToHistory") public RubyNode coerceToBoolean(RubyNode addToHistory) {
            return BooleanCastWithDefaultNodeGen.create(false, addToHistory);
        }

        @TruffleBoundary
        @Specialization
        public Object readline(String prompt, boolean addToHistory) {
            final ConsoleReader readline = getContext().getConsoleHolder().getReadline();

            // Use a Memo as readLine() can return null on Ctrl+D and we should not retry
            final Memo<String> result = new Memo<>(null);

            // Unset the default unblocking action which does Thread.interrupt(), because
            // JLine does not support interrupt, in e.g. UnixTerminal#disableLitteralNextCharacter().
            getContext().getThreadManager().runUntilResult(this, () -> {
                try {
                    result.set(readline.readLine(prompt));
                    return BlockingAction.SUCCESS;
                } catch (IOException e) {
                    throw new RaiseException(getContext(), coreExceptions().ioError(e.getMessage(), this));
                }
            }, ThreadManager.EMPTY_UNBLOCKING_ACTION);

            final String value = result.get();
            if (value == null) { // EOF
                return nil();
            } else {
                if (addToHistory) {
                    readline.getHistory().add(value);
                }

                // Enebo: This is a little weird and a little broken.  We just ask
                // for the bytes and hope they match default_external.  This will
                // work for common cases, but not ones in which the user explicitly
                // sets the default_external to something else.  The second problem
                // is that no al M17n encodings are valid encodings in java.lang.String.
                // We clearly need a byte[]-version of JLine since we cannot totally
                // behave properly using Java Strings.
                final DynamicObject ret = makeStringNode.executeMake(value, getContext().getEncodingManager().getDefaultExternalEncoding(), CodeRange.CR_UNKNOWN);
                return taintNode.executeTaint(ret);
            }
        }

    }

    @CoreMethod(names = "point", onSingleton = true)
    public abstract static class PointNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int point() {
            return getContext().getConsoleHolder().getReadline().getCursorBuffer().cursor;
        }

    }

    @CoreMethod(names = "insert_text", constructor = true, required = 1)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "text", type = RubyNode.class)
    public abstract static class InsertTextNode extends CoreMethodNode {

        @CreateCast("text") public RubyNode coerceTextToString(RubyNode text) {
            return ToJavaStringNodeGen.RubyNodeWrapperNodeGen.create(text);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject insertText(DynamicObject readline, String text) {
            getContext().getConsoleHolder().getReadline().getCursorBuffer().write(text);

            return readline;
        }

    }

    @CoreMethod(names = "delete_text", constructor = true)
    public abstract static class DeleteTextNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject deleteText(DynamicObject readline) {
            getContext().getConsoleHolder().getReadline().getCursorBuffer().clear();

            return readline;
        }

    }

    @CoreMethod(names = "line_buffer", onSingleton = true)
    public abstract static class LineBufferNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private TaintNode taintNode = TaintNode.create();

        @TruffleBoundary
        @Specialization
        public Object lineBuffer() {
            final CursorBuffer cb = getContext().getConsoleHolder().getReadline().getCursorBuffer();

            final DynamicObject ret = makeStringNode.executeMake(cb.toString(), getLocaleEncoding(), CodeRange.CR_UNKNOWN);
            return taintNode.executeTaint(ret);
        }

    }

    @CoreMethod(names = "refresh_line", onSingleton = true)
    public abstract static class RefreshLineNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject refreshLine() {
            try {
                getContext().getConsoleHolder().getReadline().redrawLine();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e.getMessage(), this));
            }

            return nil();
        }

    }

    @Primitive(name = "readline_set_input", needsSelf = false, lowerFixnum = 1)
    public abstract static class SetInputNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public int setInput(int fd, DynamicObject io) {
            final ConsoleHolder oldConsoleHolder = getContext().getConsoleHolder();
            final ConsoleHolder newConsoleHolder = oldConsoleHolder.updateIn(fd, io);

            if (newConsoleHolder != oldConsoleHolder) {
                getContext().setConsoleHolder(newConsoleHolder);
            }

            return fd;
        }

    }

    @Primitive(name = "readline_set_output", needsSelf = false, lowerFixnum = 1)
    public abstract static class SetOutputNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public int setOutput(int fd, DynamicObject io) {
            final ConsoleHolder oldConsoleHolder = getContext().getConsoleHolder();
            final ConsoleHolder newConsoleHolder = oldConsoleHolder.updateOut(fd, io);

            if (newConsoleHolder != oldConsoleHolder) {
                getContext().setConsoleHolder(newConsoleHolder);
            }

            return fd;
        }

    }

    // Taken from org.jruby.ext.readline.Readline.ProcCompleter.
    // Complete using a Proc object
    private static class ProcCompleter implements Completer {

        //\t\n\"\\'`@$><=;|&{(
        static private String[] delimiters = {" ", "\t", "\n", "\"", "\\", "'", "`", "@", "$", ">", "<", "=", ";", "|", "&", "{", "("};

        private final RubyContext context;
        private final DynamicObject proc;

        public ProcCompleter(RubyContext context, DynamicObject proc) {
            this.context = context;
            this.proc = proc;
        }

        @TruffleBoundary
        public static String getDelimiter() {
            StringBuilder result = new StringBuilder(delimiters.length);
            for (String delimiter : delimiters) {
                result.append(delimiter);
            }
            return result.toString();
        }

        @TruffleBoundary
        public static void setDelimiter(String delimiter) {
            List<String> l = new ArrayList<>();
            CharBuffer buf = CharBuffer.wrap(delimiter);
            while (buf.hasRemaining()) {
                l.add(String.valueOf(buf.get()));
            }
            delimiters = l.toArray(new String[l.size()]);
        }

        @TruffleBoundary
        private int wordIndexOf(String buffer) {
            int index = 0;
            for (String c : delimiters) {
                index = buffer.lastIndexOf(c);
                if (index != -1) {
                    return index;
                }
            }
            return index;
        }

        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            buffer = buffer.substring(0, cursor);
            int index = wordIndexOf(buffer);
            if (index != -1) {
                buffer = buffer.substring(index + 1);
            }

            DynamicObject string = StringOperations.createString(context, StringOperations.encodeRope(buffer, UTF8Encoding.INSTANCE));
            DynamicObject completions = (DynamicObject) context.send(proc, "call", string);
            assert RubyGuards.isRubyArray(completions);
            for (Object element : ArrayOperations.toIterable(completions)) {
                assert RubyGuards.isRubyString(element);
                candidates.add(StringOperations.getString((DynamicObject) element));
            }
            return cursor - buffer.length();
        }
    }

    // Taken from org.jruby.ext.readline.Readline.RubyFileNameCompleter.
    // Fix FileNameCompleter to work mid-line
    public static class RubyFileNameCompleter extends FileNameCompleter {

        @TruffleBoundary
        @Override
        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            buffer = buffer.substring(0, cursor);
            int index = buffer.lastIndexOf(' ');
            if (index != -1) {
                buffer = buffer.substring(index + 1);
            }
            return index + 1 + super.complete(buffer, cursor, candidates);
        }

    }
}
