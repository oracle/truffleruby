/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
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

import org.graalvm.shadowed.org.jline.reader.History;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.BoundaryIterable;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

import java.io.IOException;

@CoreModule("Truffle::ReadlineHistory")
public abstract class ReadlineHistoryNodes {

    @CoreMethod(names = { "push", "<<" }, rest = true)
    public abstract static class PushNode extends CoreMethodArrayArgumentsNode {

        @Child private ToJavaStringNode toJavaStringNode = ToJavaStringNode.create();

        @Specialization
        protected RubyBasicObject push(RubyBasicObject history, Object... lines) {
            for (Object line : lines) {
                final String asString = toJavaStringNode.executeToJavaString(line);
                addToHistory(asString);
            }

            return history;
        }

        @TruffleBoundary
        private void addToHistory(String item) {
            getContext().getConsoleHolder().getHistory().add(item);
        }

    }

    @CoreMethod(names = "pop", needsSelf = false)
    public abstract static class PopNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object pop() {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            if (consoleHolder.getHistory().isEmpty()) {
                return nil;
            }

            final String lastLine = consoleHolder.getHistory().removeLast().line();
            return makeStringNode.executeMake(
                    lastLine,
                    getContext().getEncodingManager().getRubyEncoding(getLocaleEncoding()),
                    CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "shift", needsSelf = false)
    public abstract static class ShiftNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object shift() {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            if (consoleHolder.getHistory().isEmpty()) {
                return nil;
            }

            final String lastLine = consoleHolder.getHistory().removeFirst().line();
            return makeStringNode.executeMake(
                    lastLine,
                    getContext().getEncodingManager().getRubyEncoding(getLocaleEncoding()),
                    CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = { "length", "size" }, needsSelf = false)
    public abstract static class LengthNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int length() {
            return getContext().getConsoleHolder().getHistory().size();
        }

    }

    @CoreMethod(names = "clear", needsSelf = false)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object clear() {
            try {
                getContext().getConsoleHolder().getHistory().purge();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }
            return nil;
        }

    }

    @CoreMethod(names = "each", needsBlock = true)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyBasicObject each(RubyBasicObject history, RubyProc block) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            for (final History.Entry e : BoundaryIterable.wrap(consoleHolder.getHistory())) {
                final RubyString line = makeStringNode
                        .executeMake(
                                historyEntryToString(e),
                                getContext().getEncodingManager().getRubyEncoding(getLocaleEncoding()),
                                CodeRange.CR_UNKNOWN);
                callBlock(block, line);
            }

            return history;
        }

        @TruffleBoundary
        private String historyEntryToString(History.Entry entry) {
            return entry.line();
        }

    }

    @CoreMethod(names = "[]", needsSelf = false, required = 1, lowerFixnum = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object getIndex(int index) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;

            try {
                final String line = consoleHolder.getHistory().get(normalizedIndex);
                return makeStringNode.executeMake(
                        line,
                        getContext().getEncodingManager().getRubyEncoding(getLocaleEncoding()),
                        CodeRange.CR_UNKNOWN);
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }

    @CoreMethod(names = "[]=", needsSelf = false, lowerFixnum = 1, required = 2)
    @NodeChild(value = "index", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "line", type = RubyNode.class)
    public abstract static class SetIndexNode extends CoreMethodNode {

        @CreateCast("index")
        protected ToIntNode coerceIndexToInt(RubyBaseNodeWithExecute index) {
            return ToIntNode.create(index);
        }

        @CreateCast("line")
        protected RubyNode coerceLineToJavaString(RubyNode line) {
            return ToJavaStringNode.create(line);
        }

        @TruffleBoundary
        @Specialization
        protected Object setIndex(int index, String line) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;

            try {
                consoleHolder.getHistory().set(normalizedIndex, line);
                return nil;
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }

    @CoreMethod(names = "delete_at", needsSelf = false, required = 1, lowerFixnum = 1)
    public abstract static class DeleteAtNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object deleteAt(int index) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();
            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;
            try {
                final String line = consoleHolder.getHistory().remove(normalizedIndex).line();
                return makeStringNode.executeMake(
                        line,
                        getContext().getEncodingManager().getRubyEncoding(getLocaleEncoding()),
                        CodeRange.CR_UNKNOWN);
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }
}
