/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.concurrent.locks.ReentrantLock;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.language.library.RubyStringLibrary;

public final class AutoloadConstant {

    private final Object feature;
    private final String autoloadPath;
    private volatile ReentrantLock autoloadLock;
    private Object unpublishedValue = null;
    private boolean published = false;

    public AutoloadConstant(Object feature) {
        assert RubyStringLibrary.getUncached().isRubyString(feature);
        this.feature = feature;
        this.autoloadPath = RubyGuards.getJavaString(this.feature);
    }

    public String getAutoloadPath() {
        return autoloadPath;
    }

    public Object getFeature() {
        return feature;
    }

    private ReentrantLock getAutoloadLock() {
        synchronized (this) {
            if (autoloadLock == null) {
                autoloadLock = new ReentrantLock();
            }
        }
        return autoloadLock;
    }

    @TruffleBoundary
    public void startAutoLoad(RubyContext context, Node currentNode) {
        MutexOperations.lockInternal(context, getAutoloadLock(), currentNode);
    }

    @TruffleBoundary
    public void stopAutoLoad() {
        getAutoloadLock().unlock();
    }

    public boolean isAutoloading() {
        return autoloadLock != null && autoloadLock.isLocked();
    }

    public boolean isAutoloadingThread() {
        return autoloadLock != null && autoloadLock.isHeldByCurrentThread();
    }

    public boolean isAutoloadingThreadAndUnset() {
        return isAutoloadingThread() && !hasUnpublishedValue();
    }

    public boolean hasUnpublishedValue() {
        assert isAutoloadingThread();
        return unpublishedValue != null;
    }

    public Object getUnpublishedValue() {
        assert isAutoloadingThread();
        return unpublishedValue;
    }

    public void setUnpublishedValue(Object unpublishedValue) {
        assert isAutoloadingThread();
        assert RubyGuards.assertIsValidRubyValue(unpublishedValue);
        this.unpublishedValue = unpublishedValue;
    }

    public boolean isPublished() {
        assert isAutoloadingThread();
        return published;
    }

    public void publish(RubyContext context, RubyConstant constant, Node node) {
        assert isAutoloadingThread();
        assert hasUnpublishedValue();
        this.published = true;
        constant.getDeclaringModule().fields.setConstant(context, node, constant.getName(), getUnpublishedValue());
    }
}
