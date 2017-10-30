/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.truffleruby.Log;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeUtil;

public class LazyRubyNode extends RubyNode {

    private final Supplier<RubyNode> resolver;
    private final ReentrantLock lock;
    private final AtomicReference<RubyNode> resolutionMaster;

    @Child volatile RubyNode resolved;

    public LazyRubyNode(Supplier<RubyNode> resolver) {
        this.resolver = resolver;
        this.lock = new ReentrantLock();
        resolutionMaster = new AtomicReference<>();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return resolve().execute(frame);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return resolve().isDefined(frame);
    }

    public RubyNode resolve() {
        if (resolved != null) {
            return resolved;
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();
        return atomic(() -> {
            if (resolved != null) {
                return resolved;
            }

            RubyNode result = getOrCreateMasterResolution();

            resolved = insert(result);
            // Tell instrumentation about our new node
            notifyInserted(result);

            return result;
        });
    }

    private RubyNode getOrCreateMasterResolution() {
        RubyNode masterResolution;

        lock.lock();
        try {
            masterResolution = resolutionMaster.get();
            if (masterResolution == null) {
                if (getContext().getOptions().LAZY_TRANSLATION_LOG) {
                    Log.LOGGER.info(() -> "lazy translating " + RubyLanguage.fileLine(getParent().getEncapsulatingSourceSection()) + " in " + getRootNode());
                }

                masterResolution = resolver.get();
                transferFlagsTo(masterResolution);

                resolutionMaster.set(masterResolution);
            }
        } finally {
            lock.unlock();
        }

        return NodeUtil.cloneNode(masterResolution);
    }
}
