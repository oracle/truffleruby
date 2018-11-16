/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import java.util.ArrayList;
import java.util.List;

public class Backtrace {

    private final Node location;
    private SourceSection sourceLocation;
    private TruffleException truffleException;
    private Activation[] activations;
    private final int omitted;
    private final Throwable javaThrowable;
    private DynamicObject backtraceStringArray;

    public Backtrace(Node location, SourceSection sourceLocation, int omitted, Throwable javaThrowable) {
        this.location = location;
        this.sourceLocation = sourceLocation;
        this.omitted = omitted;
        this.javaThrowable = javaThrowable;
    }

    public Node getLocation() {
        return location;
    }

    public SourceSection getSourceLocation() {
        return sourceLocation;
    }

    public TruffleException getTruffleException() {
        return truffleException;
    }

    public void setTruffleException(TruffleException truffleException) {
        assert this.truffleException == null : "the TruffleException of a Backtrace must not be set again, otherwise the original backtrace is lost";
        this.truffleException = truffleException;
    }

    @TruffleBoundary
    public Activation[] getActivations() {
        if (this.activations == null) {
            final TruffleException truffleException;
            if (this.truffleException != null) {
                truffleException = this.truffleException;
            } else {
                truffleException = new GetBacktraceException(location, GetBacktraceException.UNLIMITED);
            }

            // The stacktrace is computed here if it was not already computed and stored in the
            // TruffleException with TruffleStackTraceElement.fillIn().
            final List<TruffleStackTraceElement> stackTrace = TruffleStackTraceElement.getStackTrace((Throwable) truffleException);

            final List<Activation> activations = new ArrayList<>();
            final RubyContext context = RubyLanguage.getCurrentContext();
            final CallStackManager callStackManager = context.getCallStack();

            int i = 0;
            for (TruffleStackTraceElement stackTraceElement : stackTrace) {
                if (i >= omitted) {
                    final Node callNode = i == 0 ? location : stackTraceElement.getLocation();

                    if (!callStackManager.ignoreFrame(callNode)) {
                        final Frame frame = stackTraceElement.getFrame();
                        final InternalMethod method = frame == null ? null : RubyArguments.tryGetMethod(frame);

                        if (callNode != null || method != null) { // Ignore the frame if we know nothing about it
                            activations.add(new Activation(callNode, method));
                        }
                    }

                }
                i++;
            }

            // If there are activations with a InternalMethod but no caller information above in the
            // stack, then all of these activations are internal as they are not called from user code.
            while (!activations.isEmpty() && activations.get(activations.size() - 1).getCallNode() == null) {
                activations.remove(activations.size() - 1);
            }

            this.activations = activations.toArray(new Activation[activations.size()]);
        }

        return this.activations;

    }

    public void setActivations(Activation[] activations) {
        this.activations = activations;
    }

    public int getOmitted() {
        return omitted;
    }

    public Throwable getJavaThrowable() {
        return javaThrowable;
    }

    public DynamicObject getBacktraceStringArray() {
        return backtraceStringArray;
    }

    public void setBacktraceStringArray(DynamicObject backtraceStringArray) {
        this.backtraceStringArray = backtraceStringArray;
    }

}
