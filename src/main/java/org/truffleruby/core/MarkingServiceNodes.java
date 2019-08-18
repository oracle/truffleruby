/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.util.ArrayList;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.MarkingService.MarkerThreadLocalData;
import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

public class MarkingServiceNodes {

    @GenerateUncached
    public static abstract class KeepAliveNode extends RubyBaseWithoutContextNode {

        public abstract void execute(Object object);

        @Specialization
        protected void keepObjectAlive(Object object,
                @Cached GetMarkerThreadLocalDataNode getThreadLocalDataNode) {
            MarkerThreadLocalData data = getThreadLocalDataNode.execute();
            addToList(data.getExtensionCallStack().getKeptObjects(), object);
            data.getKeptObjects().keepObject(object);
        }

        @TruffleBoundary
        protected void addToList(ArrayList<Object> list, Object object) {
            list.add(object);
        }

        public static KeepAliveNode create() {
            return MarkingServiceNodesFactory.KeepAliveNodeGen.create();
        }
    }

    @GenerateUncached
    public static abstract class GetMarkerThreadLocalDataNode extends RubyBaseWithoutContextNode {

        public final MarkerThreadLocalData execute() {
            return execute(Boolean.TRUE);
        }

        public abstract MarkerThreadLocalData execute(Object dynamicParameter);

        @Specialization(guards = "thread == currentJavaThread(dynamicParameter)", limit = "getCacheLimit()")
        protected MarkerThreadLocalData getDataOnKnownThread(
                Object dynamicParameter,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached("currentJavaThread(dynamicParameter)") Thread thread,
                @Cached("getData(dynamicParameter, context)") MarkerThreadLocalData data) {
            assert context == data.getKeptObjects().getService().context;
            return data;
        }

        @Specialization(replaces = "getDataOnKnownThread")
        protected MarkerThreadLocalData getData(
                Object dynamicParameter,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getMarkingService().getThreadLocalData();
        }

        static protected Thread currentJavaThread(Object dynamicParameter) {
            return Thread.currentThread();
        }

        public int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().THREAD_CACHE;
        }

        public static GetMarkerThreadLocalDataNode create() {
            return MarkingServiceNodesFactory.GetMarkerThreadLocalDataNodeGen.create();
        }
    }
}
