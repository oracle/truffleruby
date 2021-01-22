/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.function.BiConsumer;

import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.thread.RubyThread;

/** A action to run in a guest-language safepoint. Assumed to have side-effects, unless it is an instance of
 * {@link Pure}. Such actions are always deferred, and not executed inside the safepoint but either just after it or if
 * a thread is inside {@code Thread.handle_interrupt(Object => :never/:blocking) { ... }} then once the thread exit the
 * handle_interrupt block. */
public interface SafepointAction extends BiConsumer<RubyThread, Node> {

    /** A guest-language safepoint with no side effects, which can be run safely even in
     * {@code Thread.handle_interrupt(Object => :never) { ... }}. Such actions are always executed inside the safepoint
     * and never deferred. */
    interface Pure extends SafepointAction {
    }

    static boolean isDeferred(SafepointAction action) {
        return !(action instanceof Pure);
    }
}

