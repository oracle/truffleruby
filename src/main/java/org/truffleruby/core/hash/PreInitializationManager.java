/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Shape;

public final class PreInitializationManager {

    private final RubyContext context;

    private final List<ReHashable> reHashables = new ArrayList<>();

    private TrackingHashFactory trackingHashFactory;
    private final Set<DynamicObject> hashesCreatedDuringPreInit = Collections.newSetFromMap(new WeakHashMap<>());

    public PreInitializationManager(RubyContext context) {
        this.context = context;
    }

    public void addReHashable(ReHashable reHashable) {
        // This might get called multiple times for the same ReHashable,
        // so only add it if it is not already in the List.
        for (ReHashable existing : reHashables) {
            if (reHashable == existing) {
                return;
            }
        }
        reHashables.add(reHashable);
    }

    @TruffleBoundary
    private void addPreInitHash(DynamicObject hash) {
        hashesCreatedDuringPreInit.add(hash);
    }

    public DynamicObjectFactory hookIntoHashFactory(DynamicObjectFactory originalHashFactory) {
        trackingHashFactory = new TrackingHashFactory(context, this, originalHashFactory);
        return trackingHashFactory;
    }

    private void restoreOriginalHashFactory() {
        Layouts.CLASS.setInstanceFactoryUnsafe(
                context.getCoreLibrary().getHashClass(),
                trackingHashFactory.originalHashFactory);
    }

    public void rehash() {
        for (ReHashable reHashable : reHashables) {
            reHashable.rehash();
        }
        reHashables.clear();

        rehashRubyHashes();
    }

    private void rehashRubyHashes() {
        for (DynamicObject hash : hashesCreatedDuringPreInit) {
            if (!HashGuards.isCompareByIdentity(hash)) {
                context.send(hash, "rehash");
            }
        }
        hashesCreatedDuringPreInit.clear();

        restoreOriginalHashFactory();
    }

    private static final class TrackingHashFactory implements DynamicObjectFactory {

        private final RubyContext context;
        private final PreInitializationManager preInitializationManager;
        private final DynamicObjectFactory originalHashFactory;

        public TrackingHashFactory(
                RubyContext context,
                PreInitializationManager preInitializationManager,
                DynamicObjectFactory originalHashFactory) {
            this.context = context;
            this.preInitializationManager = preInitializationManager;
            this.originalHashFactory = originalHashFactory;
        }

        public DynamicObject newInstance(Object... initialValues) {
            final DynamicObject object = originalHashFactory.newInstance(initialValues);
            if (context.isPreInitializing()) {
                preInitializationManager.addPreInitHash(object);
            }
            return object;
        }

        public Shape getShape() {
            return originalHashFactory.getShape();
        }
    }

}
