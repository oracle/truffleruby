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

import org.truffleruby.core.MarkingService.MarkerThreadLocalData;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public class MarkingServiceNodes {

    public static abstract class KeepAliveNode extends RubyBaseNode {

        public abstract void execute(VirtualFrame frame, Object object);

        @Specialization
        public void keepObjectAlive(VirtualFrame frame, Object object,
                @Cached("create()") GetMarkerThreadLocalDataNode getThreadLocalDataNode) {
            MarkerThreadLocalData data = getThreadLocalDataNode.execute(frame);
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

    public static abstract class GetMarkerThreadLocalDataNode extends RubyBaseNode {

        public abstract MarkerThreadLocalData execute(VirtualFrame frame);

        @Specialization(guards = "thread == currentJavaThread(frame)", limit = "getCacheLimit()")
        public MarkerThreadLocalData getDataOnKnownThread(VirtualFrame frame,
                @Cached("currentJavaThread(frame)") Thread thread,
                @Cached("getData()") MarkerThreadLocalData data) {
            return data;
        }

        @Specialization(replaces = "getDataOnKnownThread")
        protected MarkerThreadLocalData getData() {
            return getContext().getMarkingService().getThreadLocalData();
        }

        static protected Thread currentJavaThread(VirtualFrame frame) {
            return Thread.currentThread();
        }

        public int getCacheLimit() {
            return getContext().getOptions().THREAD_CACHE;
        }

        public static GetMarkerThreadLocalDataNode create() {
            return MarkingServiceNodesFactory.GetMarkerThreadLocalDataNodeGen.create();
        }
    }
}
