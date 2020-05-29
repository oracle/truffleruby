/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.NodeUtil;

public class LazyRubyNode extends RubyContextSourceNode {

    private final RubyContext context;
    private Supplier<RubyNode> resolver;
    private final ReentrantLock lock;
    /** Not a direct RubyNode field as we want to share the resolution between split LazyRubyNodes. We use
     * AtomicReference as a box here so all copies share the same AtomicReference and resolve only once. */
    private final AtomicReference<RubyNode> resolutionMaster;

    @Child volatile RubyNode resolved;

    public LazyRubyNode(RubyContext context, Supplier<RubyNode> resolver) {
        this.context = context;
        this.resolver = resolver;
        this.lock = new ReentrantLock();
        this.resolutionMaster = new AtomicReference<>();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return resolve().execute(frame);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        return resolve().isDefined(frame, context);
    }

    @Override
    public InstrumentableNode materializeInstrumentableNodes(Set<Class<? extends Tag>> materializedTags) {
        for (Class<? extends Tag> tag : materializedTags) {
            if (tag == StandardTags.StatementTag.class || tag == StandardTags.CallTag.class) {
                resolve();
                return this;
            }
        }

        return this;
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
                if (context.getOptions().LAZY_TRANSLATION_LOG) {
                    RubyLanguage.LOGGER.info(
                            () -> "lazy translating " +
                                    RubyContext.fileLine(getParent().getEncapsulatingSourceSection()) + " in " +
                                    getRootNode());
                }

                masterResolution = resolver.get();

                // We no longer need the resolver, so let it be GC'd
                resolver = null;
                resolutionMaster.set(masterResolution);
            }
        } finally {
            lock.unlock();
        }

        return NodeUtil.cloneNode(masterResolution);
    }
}
