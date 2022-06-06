/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.core.klass.RubyClass;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.objects.ObjectIDOperations;

import java.util.ArrayList;

import static org.truffleruby.cext.ValueWrapperManager.NIL_HANDLE;

/** The Ruby {@code nil}, the single instance of NilClass. */
@ExportLibrary(InteropLibrary.class)
public final class Nil extends ImmutableRubyObject implements TruffleObject {

    public static final boolean TRACE = true;

    private static final Nil INSTANCE = new Nil();

    public static Nil get() {
        return get(null, "implicit nil");
    }

    public static Nil get(Node node, String event) {
        if (TRACE) {
            final Nil nil = new Nil();
            nil.trace(node, event);
            return nil;
        } else {
            return INSTANCE;
        }
    }

    public static boolean is(Object object) {
        if (TRACE) {
            return object instanceof Nil;
        } else {
            return object == INSTANCE;
        }
    }

    public static boolean isNot(Object object) {
        if (TRACE) {
            return !(object instanceof Nil);
        } else {
            return object != INSTANCE;
        }
    }

    private Nil() {
        this.valueWrapper = new ValueWrapper(this, NIL_HANDLE, null);
        this.objectId = ObjectIDOperations.NIL;
    }

    @Override
    public String toString() {
        return "nil";
    }

    // region InteropLibrary messages
    @Override
    @ExportMessage
    public String toDisplayString(boolean allowSideEffects) {
        return "nil";
    }

    @ExportMessage
    public boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    public RubyClass getMetaObject(
            @CachedLibrary("this") InteropLibrary node) {
        return RubyContext.get(node).getCoreLibrary().nilClass;
    }

    @ExportMessage
    public boolean isNull() {
        return true;
    }
    // endregion

    private static class TraceEntry {

        private final TraceEntry previous;
        private final Node node;
        private final String event;

        public TraceEntry(TraceEntry previous, Node node, String event) {
            this.previous = previous;
            this.node = node;
            this.event = event;
        }

    }

    private TraceEntry trace = null;

    public void trace(Node node, String event) {
        if (TRACE) {
            trace = new TraceEntry(trace, node, event);
        }
    }

    public String[] getTrace(RubyLanguage language) {
        final ArrayList<String> lines = new ArrayList<>();

        for (TraceEntry entry = trace; entry != null; entry = entry.previous) {
            if (entry.node == null) {
                lines.add(entry.event);
            } else {
                final SourceSection sourceSection = entry.node.getEncapsulatingSourceSection();
                if (BacktraceFormatter.isUserSourceSection(language, sourceSection)) {
                    final String source = RubyLanguage.fileLine(sourceSection);
                    final String method = entry.node.getRootNode().getName();
                    lines.add(entry.event + " from " + source + " in " + method);
                } else if (entry.previous == null) {
                    // Source of nil may be in the core library, but we still want to show it
                    lines.add(entry.event);
                }
            }
        }

        return lines.toArray(StringUtils.EMPTY_STRING_ARRAY);
    }

}
