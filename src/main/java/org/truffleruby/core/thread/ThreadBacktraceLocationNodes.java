/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.thread;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.backtrace.Activation;

@CoreClass("Thread::Backtrace::Location")
public class ThreadBacktraceLocationNodes {

    @CoreMethod(names = "absolute_path")
    public abstract static class AbsolutePathNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject absolutePath(DynamicObject threadBacktraceLocation,
                                          @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
            final Activation activation = Layouts.THREAD_BACKTRACE_LOCATION.getActivation(threadBacktraceLocation);

            if (activation.getCallNode() == null) {
                return coreStrings().BACKTRACE_OMITTED_LIMIT.createInstance();
            }

            final SourceSection sourceSection = activation.getCallNode().getEncapsulatingSourceSection();
            final Source source = sourceSection.getSource();

            // Get absolute path
            final String path = source.getPath();

            if (path == null) {
                return coreStrings().UNKNOWN.createInstance();
            } else {
                return makeStringNode.fromRope(getContext().getRopeTable().getRope(path));
            }
        }

    }

    @CoreMethod(names = "path")
    public abstract static class PathNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject path(DynamicObject threadBacktraceLocation,
                                  @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
            final Activation activation = Layouts.THREAD_BACKTRACE_LOCATION.getActivation(threadBacktraceLocation);

            if (activation.getCallNode() == null) {
                return coreStrings().BACKTRACE_OMITTED_LIMIT.createInstance();
            }

            final SourceSection sourceSection = activation.getCallNode().getEncapsulatingSourceSection();
            final Source source = sourceSection.getSource();

            // Get file path except for the main script
            final String path = source.getName();

            if (path == null) {
                return coreStrings().UNKNOWN.createInstance();
            } else {
                return makeStringNode.fromRope(getContext().getRopeTable().getRope(path));
            }
        }

    }

    @CoreMethod(names = "label")
    public abstract static class LabelNode extends UnaryCoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject label(DynamicObject threadBacktraceLocation) {
            final Activation activation = Layouts.THREAD_BACKTRACE_LOCATION.getActivation(threadBacktraceLocation);
            // TODO eregon 8 Nov. 2016 This does not handle blocks
            final String methodName = activation.getMethod().getSharedMethodInfo().getName();

            return makeStringNode.executeMake(methodName, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "lineno")
    public abstract static class LinenoNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public int lineno(DynamicObject threadBacktraceLocation) {
            final Activation activation = Layouts.THREAD_BACKTRACE_LOCATION.getActivation(threadBacktraceLocation);

            final SourceSection sourceSection = activation.getCallNode().getEncapsulatingSourceSection();

            return sourceSection.getStartLine();
        }

    }

    @CoreMethod(names = "to_s")
    public abstract static class ToSNode extends UnaryCoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject toS(DynamicObject threadBacktraceLocation) {
            final String description = Layouts.THREAD_BACKTRACE_LOCATION.getDescription(threadBacktraceLocation);
            return makeStringNode.executeMake(description, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

}
