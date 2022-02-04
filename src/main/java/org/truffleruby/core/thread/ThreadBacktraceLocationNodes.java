/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.backtrace.Backtrace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.language.backtrace.BacktraceFormatter;

@CoreModule(value = "Thread::Backtrace::Location", isClass = true)
public class ThreadBacktraceLocationNodes {

    @TruffleBoundary
    private static SourceSection getAvailableSourceSection(RubyContext context,
            RubyBacktraceLocation threadBacktraceLocation) {
        final Backtrace backtrace = threadBacktraceLocation.backtrace;
        final int activationIndex = threadBacktraceLocation.activationIndex;

        return context
                .getUserBacktraceFormatter()
                .nextAvailableSourceSection(backtrace.getStackTrace(), activationIndex);
    }

    @CoreMethod(names = "absolute_path")
    public abstract static class AbsolutePathNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object absolutePath(RubyBacktraceLocation threadBacktraceLocation,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final SourceSection sourceSection = getAvailableSourceSection(getContext(), threadBacktraceLocation);

            if (sourceSection == null) {
                return coreStrings().UNKNOWN.createInstance(getContext());
            } else {
                final Source source = sourceSection.getSource();
                if (BacktraceFormatter.isRubyCore(getLanguage(), source)) {
                    return nil;
                } else if (source.getPath() != null) { // A normal file
                    final String path = getLanguage().getSourcePath(source);
                    final String canonicalPath = getContext().getFeatureLoader().canonicalize(path);
                    final Rope cachedRope = getLanguage().ropeCache
                            .getRope(StringOperations.encodeRope(canonicalPath, UTF8Encoding.INSTANCE));
                    return makeStringNode.fromRope(cachedRope, Encodings.UTF_8);
                } else { // eval()
                    final Rope cachedPath = getLanguage().getPathToRopeCache().getCachedPath(source);
                    return makeStringNode.fromRope(cachedPath, Encodings.UTF_8);
                }
            }
        }

    }

    @CoreMethod(names = "path")
    public abstract static class PathNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected RubyString path(RubyBacktraceLocation threadBacktraceLocation,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final SourceSection sourceSection = getAvailableSourceSection(getContext(), threadBacktraceLocation);

            if (sourceSection == null) {
                return coreStrings().UNKNOWN.createInstance(getContext());
            } else {
                final Rope path = getLanguage().getPathToRopeCache().getCachedPath(sourceSection.getSource());
                return makeStringNode.fromRope(path, Encodings.UTF_8);
            }
        }

    }

    @CoreMethod(names = "label")
    public abstract static class LabelNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyString label(RubyBacktraceLocation threadBacktraceLocation,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final Backtrace backtrace = threadBacktraceLocation.backtrace;
            final int index = threadBacktraceLocation.activationIndex;
            final TruffleStackTraceElement element = backtrace.getStackTrace()[index];

            final String label = Backtrace.labelFor(element);
            return makeStringNode.executeMake(label, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "base_label")
    public abstract static class BaseLabelNode extends UnaryCoreMethodNode {
        @Specialization
        protected RubyString label(RubyBacktraceLocation threadBacktraceLocation,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final Backtrace backtrace = threadBacktraceLocation.backtrace;
            final int index = threadBacktraceLocation.activationIndex;
            final TruffleStackTraceElement element = backtrace.getStackTrace()[index];

            final String baseLabel = Backtrace.baseLabelFor(element);
            return makeStringNode.executeMake(baseLabel, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "lineno")
    public abstract static class LinenoNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected int lineno(RubyBacktraceLocation threadBacktraceLocation) {
            final SourceSection sourceSection = getAvailableSourceSection(getContext(), threadBacktraceLocation);

            return sourceSection.getStartLine();
        }

    }

    @CoreMethod(names = "to_s")
    public abstract static class ToSNode extends UnaryCoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString toS(RubyBacktraceLocation threadBacktraceLocation) {
            final Backtrace backtrace = threadBacktraceLocation.backtrace;
            final int index = threadBacktraceLocation.activationIndex;

            final String description = getContext()
                    .getUserBacktraceFormatter()
                    .formatLine(backtrace.getStackTrace(), index, null);
            return makeStringNode.executeMake(description, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }

    }

}
