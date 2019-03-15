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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.MarkingService.MarkerThreadLocalData;

public class MarkingServiceNodes {

    @GenerateUncached
    public static abstract class KeepAliveNode extends Node {

        public abstract void execute(Object object);

        @Specialization
        public void keepObjectAlive(Object object,
                @Cached("create()") GetMarkerThreadLocalDataNode getThreadLocalDataNode) {
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
    public static abstract class GetMarkerThreadLocalDataNode extends Node {

        public final MarkerThreadLocalData execute() {
            return execute(Boolean.TRUE);
        }

        public abstract MarkerThreadLocalData execute(Object dynamicParameter);

        @Specialization(guards = "thread == currentJavaThread(dynamicParameter)", limit = "getCacheLimit()")
        public MarkerThreadLocalData getDataOnKnownThread(
                Object dynamicParameter,
                @CachedContext(RubyLanguage.class) RubyContext rubyContext,
                @Cached("currentJavaThread(dynamicParameter)") Thread thread,
                @Cached("getData(dynamicParameter, rubyContext)") MarkerThreadLocalData data) {
            return data;
        }

        @Specialization(replaces = "getDataOnKnownThread")
        protected MarkerThreadLocalData getData(
                Object dynamicParameter,
                @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
            return rubyContext.getMarkingService().getThreadLocalData();
        }

        static protected Thread currentJavaThread(Object dynamicParameter) {
            return Thread.currentThread();
        }

        public int getCacheLimit() {
            // TODO (pitr-ch 19-Mar-2019): is this an issue?
            return RubyLanguage.getCurrentContext().getOptions().THREAD_CACHE;
        }

        public static GetMarkerThreadLocalDataNode create() {
            return MarkingServiceNodesFactory.GetMarkerThreadLocalDataNodeGen.create();
        }
    }
}
