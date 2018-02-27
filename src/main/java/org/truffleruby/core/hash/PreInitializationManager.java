/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.hash;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.regexp.TruffleRegexpNodes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Shape;

public final class PreInitializationManager {

    private final RubyContext context;

    private TrackingHashFactory trackingHashFactory;
    private final Set<DynamicObject> hashesCreatedDuringPreInit = Collections.newSetFromMap(new WeakHashMap<>());

    public PreInitializationManager(RubyContext context) {
        this.context = context;
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
        Layouts.CLASS.setInstanceFactoryUnsafe(context.getCoreLibrary().getHashClass(), trackingHashFactory.originalHashFactory);
    }

    public void rehash() {
        context.getRopeCache().rehash();
        context.getSymbolTable().rehash();
        context.getRegexpCache().rehash();
        TruffleRegexpNodes.rehash();
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

        public TrackingHashFactory(RubyContext context, PreInitializationManager preInitializationManager, DynamicObjectFactory originalHashFactory) {
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
