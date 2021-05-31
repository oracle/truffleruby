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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.ThreadLocalAction;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.thread.RubyThread;

import java.util.Objects;

/** A action to run in a guest-language safepoint. Actions with side-effects should usually be asynchronous. */
public abstract class SafepointAction extends ThreadLocalAction {

    private final boolean publicSynchronous;
    private final String reason;
    private final SafepointPredicate filter;
    private final RubyThread targetThread;

    public SafepointAction(String reason, RubyThread targetThread, boolean hasSideEffects, boolean synchronous) {
        this(reason, SafepointPredicate.CURRENT_FIBER_OF_THREAD, hasSideEffects, synchronous, targetThread);
    }

    public SafepointAction(String reason, SafepointPredicate filter, boolean hasSideEffects, boolean synchronous) {
        this(reason, filter, hasSideEffects, synchronous, null);
    }

    private SafepointAction(
            String reason,
            SafepointPredicate filter,
            boolean hasSideEffects,
            boolean synchronous,
            RubyThread targetThread) {
        super(hasSideEffects, synchronous);
        this.publicSynchronous = synchronous;
        this.reason = reason;
        this.filter = filter;
        this.targetThread = targetThread;
    }

    public abstract void run(RubyThread rubyThread, Node currentNode);

    @Override
    protected final void perform(Access access) {
        if (Thread.currentThread() != access.getThread()) {
            throw CompilerDirectives.shouldNotReachHere(
                    "safepoint action for " + access.getThread() + " executed on other thread: " +
                            Thread.currentThread());
        }

        final RubyContext context = RubyLanguage.getCurrentContext();
        final RubyThread rubyThread = context.getThreadManager().getCurrentThread();
        if (filter.test(context, rubyThread, this)) {
            run(rubyThread, access.getLocation());
        }
    }

    public boolean isSynchronous() {
        return publicSynchronous;
    }

    public RubyThread getTargetThread() {
        return Objects.requireNonNull(targetThread);
    }

    @Override
    public String toString() {
        return reason + " " + Integer.toHexString(System.identityHashCode(this));
    }

}

