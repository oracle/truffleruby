/*
 * Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.shadowed.org.jline.reader.History;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.collections.BoundaryIterable;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.yield.CallBlockNode;

import java.io.IOException;

@CoreModule("Truffle::ReadlineHistory")
public abstract class ReadlineHistoryNodes {

    @CoreMethod(names = { "push", "<<" }, rest = true)
    public abstract static class PushNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        RubyBasicObject push(RubyBasicObject history, Object[] lines,
                @Cached ToJavaStringNode toJavaStringNode) {
            for (Object line : lines) {
                final String asString = toJavaStringNode.execute(this, line);
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

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        Object pop() {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            if (consoleHolder.getHistory().isEmpty()) {
                return nil;
            }

            final String lastLine = consoleHolder.getHistory().removeLast().line();
            return createString(
                    fromJavaStringNode,
                    lastLine,
                    getLocaleEncoding());
        }

    }

    @CoreMethod(names = "shift", needsSelf = false)
    public abstract static class ShiftNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        Object shift() {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            if (consoleHolder.getHistory().isEmpty()) {
                return nil;
            }

            final String lastLine = consoleHolder.getHistory().removeFirst().line();
            return createString(
                    fromJavaStringNode,
                    lastLine,
                    getLocaleEncoding());
        }

    }

    @CoreMethod(names = { "length", "size" }, needsSelf = false)
    public abstract static class LengthNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        int length() {
            return getContext().getConsoleHolder().getHistory().size();
        }

    }

    @CoreMethod(names = "clear", needsSelf = false)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        Object clear() {
            try {
                getContext().getConsoleHolder().getHistory().purge();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }
            return nil;
        }

    }

    @CoreMethod(names = "each", needsBlock = true)
    public abstract static class EachNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @Specialization
        RubyBasicObject each(RubyBasicObject history, RubyProc block,
                @Cached CallBlockNode yieldNode) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            for (final History.Entry e : BoundaryIterable.wrap(consoleHolder.getHistory())) {
                final RubyString line = createString(
                        fromJavaStringNode,
                        historyEntryToString(e),
                        getLocaleEncoding());
                yieldNode.yield(this, block, line);
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

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        Object getIndex(int index) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;

            try {
                final String line = consoleHolder.getHistory().get(normalizedIndex);
                return createString(
                        fromJavaStringNode,
                        line,
                        getLocaleEncoding());
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }

    @CoreMethod(names = "[]=", needsSelf = false, lowerFixnum = 1, required = 2)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        Object setIndex(Object indexObject, Object line,
                @Cached ToIntNode toIntNode,
                @Cached ToJavaStringNode toJavaStringNode) {
            final int index = toIntNode.execute(indexObject);
            final var lineAsString = toJavaStringNode.execute(this, line);
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;

            try {
                consoleHolder.getHistory().set(normalizedIndex, lineAsString);
                return nil;
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }

    @CoreMethod(names = "delete_at", needsSelf = false, required = 1, lowerFixnum = 1)
    public abstract static class DeleteAtNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        Object deleteAt(int index) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();
            final int normalizedIndex = index < 0 ? index + consoleHolder.getHistory().size() : index;
            try {
                final String line = consoleHolder.getHistory().remove(normalizedIndex).line();
                return createString(
                        fromJavaStringNode,
                        line,
                        getLocaleEncoding());
            } catch (IndexOutOfBoundsException e) {
                throw new RaiseException(getContext(), coreExceptions().indexErrorInvalidIndex(this));
            }
        }

    }
}
