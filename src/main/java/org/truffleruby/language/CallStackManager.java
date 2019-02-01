/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.InternalRootNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.shared.TruffleRuby;

public class CallStackManager {

    private final RubyContext context;

    public CallStackManager(RubyContext context) {
        this.context = context;
    }

    private static final Object STOP_ITERATING = new Object();

    public FrameInstance getCallerFrameIgnoringSend() {
        return getCallerFrameIgnoringSend(0);
    }

    @TruffleBoundary
    public Node getCallerNode(int skip, boolean ignoreSend) {
        // Do not try getCallerFrame() as getCallerFrame().getCallNode() is always null.
        // See the JavaDoc of getCallNode().

        return Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Node>() {
            int depth = 0;
            int skipped = 0;

            @Override
            public Node visitFrame(FrameInstance frameInstance) {
                depth++;
                if (depth == 1) { // Skip top frame
                    return null;
                }

                if (skipped >= skip) {
                    if (ignoreSend) {
                        final InternalMethod method = getMethod(frameInstance);
                        if (method != null && context.getCoreLibrary().isSend(method)) {
                            return null;
                        } else {
                            return frameInstance.getCallNode();
                        }
                    } else {
                        return frameInstance.getCallNode();
                    }
                } else {
                    skipped++;
                }
                return null;
            }
        });
    }

    @TruffleBoundary
    public boolean callerIsSend() {
        FrameInstance callerFrame = Truffle.getRuntime().getCallerFrame();
        if (callerFrame == null) {
            return false;
        }
        InternalMethod method = getMethod(callerFrame);
        return context.getCoreLibrary().isSend(method);
    }

    @TruffleBoundary
    public FrameInstance getCallerFrameIgnoringSend(int skip) {
        // Try first using getCallerFrame() as it's the common case
        if (skip == 0) {
            FrameInstance callerFrame = Truffle.getRuntime().getCallerFrame();
            if (callerFrame == null) {
                return null;
            }
            InternalMethod method = getMethod(callerFrame);

            if (method == null) { // Not a Ruby frame
                return null;
            } else if (!context.getCoreLibrary().isSend(method)) {
                return callerFrame;
            }
        }

        // Need to iterate further
        final Object frame = Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Object>() {
            int depth = 0;
            int skipped = 0;

            @Override
            public Object visitFrame(FrameInstance frameInstance) {
                depth++;
                if (depth == 1) { // Skip top frame
                    return null;
                }

                InternalMethod method = getMethod(frameInstance);
                if (method == null) {
                    return STOP_ITERATING;
                } else if (!context.getCoreLibrary().isSend(method)) {
                    if (skipped >= skip) {
                        return frameInstance;
                    } else {
                        skipped++;
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });

        if (frame instanceof FrameInstance) {
            return (FrameInstance) frame;
        } else {
            return null;
        }
    }

    @TruffleBoundary
    public FrameInstance getCallerFrameNotInModules(Object[] modules, int skip) {
        final FrameInstance frame = Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<FrameInstance>() {
            int depth = 0;
            int skipped = 0;

            @Override
            public FrameInstance visitFrame(FrameInstance frameInstance) {
                depth++;
                if (depth == 1) {
                    return null; // Skip ourselves.
                }
                final InternalMethod method = getMethod(frameInstance);
                if (method == null) {
                    return null;
                } else if (!ArrayUtils.contains(modules, method.getDeclaringModule())) {
                    if (skipped >= skip) {
                        return frameInstance;
                    } else {
                        skipped++;
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
        return frame;
    }

    @TruffleBoundary
    public InternalMethod getCallingMethodIgnoringSend() {
        return getMethod(getCallerFrameIgnoringSend());
    }

    @TruffleBoundary
    public SourceSection getTopMostUserSourceSection() {
        return Truffle.getRuntime().iterateFrames(frameInstance -> {
            final Node callNode = frameInstance.getCallNode();
            if (callNode == null) {
                return null;
            }

            final SourceSection sourceSection = callNode.getEncapsulatingSourceSection();
            if (BacktraceFormatter.isUserSourceSection(context, sourceSection)) {
                return sourceSection;
            } else {
                return null; // Keep searching
            }
        });
    }

    @TruffleBoundary
    public SourceSection getTopMostUserSourceSection(SourceSection encapsulatingSourceSection) {
        if (encapsulatingSourceSection != null && BacktraceFormatter.isUserSourceSection(context, encapsulatingSourceSection)) {
            return encapsulatingSourceSection;
        } else {
            return getTopMostUserSourceSection();
        }
    }

    private InternalMethod getMethod(FrameInstance frame) {
        return RubyArguments.tryGetMethod(frame.getFrame(FrameInstance.FrameAccess.READ_ONLY));
    }

    public Backtrace getBacktrace(Node currentNode) {
        return getBacktrace(currentNode, null, 0, null);
    }

    public Backtrace getBacktrace(Node currentNode, int omit) {
        return getBacktrace(currentNode, null, omit, null);
    }

    public Backtrace getBacktrace(Node currentNode, SourceSection sourceLocation, Throwable javaThrowable) {
        return getBacktrace(currentNode, sourceLocation, 0, javaThrowable);
    }

    public Backtrace getBacktrace(Node currentNode, SourceSection sourceLocation, int omit, Throwable javaThrowable) {
        if (context.getOptions().EXCEPTIONS_STORE_JAVA || context.getOptions().BACKTRACES_INTERLEAVE_JAVA) {
            if (javaThrowable == null) {
                javaThrowable = new Exception();
            }
        }

        return new Backtrace(currentNode, sourceLocation, omit, javaThrowable);
    }

    @SuppressFBWarnings("ES")
    public boolean ignoreFrame(Node callNode, RootCallTarget callTarget) {
        // Nodes with no call node are top-level or require, which *should* appear in the backtrace.
        if (callNode == null) {
            return false;
        }

        final RootNode rootNode = callNode.getRootNode();

        // Ignore Truffle::Boot.main and its caller
        if (rootNode instanceof RubyRootNode) {
            final SharedMethodInfo sharedMethodInfo = ((RubyRootNode) rootNode).getSharedMethodInfo();
            if (context.getCoreLibrary().isTruffleBootMainMethod(sharedMethodInfo)) {
                return true;
            }
            final SourceSection sourceSection = sharedMethodInfo.getSourceSection();
            if (sourceSection != null && sourceSection.getSource().getName() == TruffleRuby.BOOT_SOURCE_NAME) {
                return true;
            }
        }

        if (rootNode instanceof InternalRootNode) {
            return true;
        }

        // Ignore BasicObject#__send__, Kernel#send and Kernel#public_send like MRI
        if (context.getCoreLibrary().isSend(callTarget)) {
            return true;
        }

        if (callNode.getEncapsulatingSourceSection() == null) {
            return true;
        }

        return false;
    }

}
