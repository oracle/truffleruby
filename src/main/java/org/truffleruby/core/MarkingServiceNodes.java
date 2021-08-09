/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.util.ArrayList;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.MarkingService.MarkerThreadLocalData;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

public class MarkingServiceNodes {

    @GenerateUncached
    public abstract static class KeepAliveNode extends RubyBaseNode {

        public abstract void execute(Object object);

        @Specialization
        protected void keepObjectAlive(Object object,
                @Cached GetMarkerThreadLocalDataNode getThreadLocalDataNode) {
            MarkerThreadLocalData data = getThreadLocalDataNode.execute();
            addToList(data.getExtensionCallStack().getKeptObjects(), object);
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
    public abstract static class GetMarkerThreadLocalDataNode extends RubyBaseNode {

        public final MarkerThreadLocalData execute() {
            return executeInternal(Boolean.TRUE);
        }

        protected abstract MarkerThreadLocalData executeInternal(Object dynamicParameter);

        @Specialization(guards = "thread == currentJavaThread(dynamicParameter)", limit = "getCacheLimit()")
        protected MarkerThreadLocalData getDataOnKnownThread(Object dynamicParameter,
                @Cached("currentJavaThread(dynamicParameter)") Thread thread,
                @Cached("getData(dynamicParameter)") MarkerThreadLocalData data) {
            return data;
        }

        @Specialization(replaces = "getDataOnKnownThread")
        protected MarkerThreadLocalData getData(Object dynamicParameter) {
            return getContext().getMarkingService().getThreadLocalData();
        }

        protected static Thread currentJavaThread(Object dynamicParameter) {
            return Thread.currentThread();
        }

        public int getCacheLimit() {
            return RubyLanguage.getCurrentLanguage().options.THREAD_CACHE;
        }

        public static GetMarkerThreadLocalDataNode create() {
            return MarkingServiceNodesFactory.GetMarkerThreadLocalDataNodeGen.create();
        }
    }
}
