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
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.RaiseException;

import java.util.ArrayList;
import java.util.List;

public class Backtrace {

    private final Node location;
    /** Only set for SyntaxError, where getLocation() does not represent where the error occurred. */
    private final SourceSection sourceLocation;
    private RaiseException raiseException;
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

    public Backtrace copy(RubyContext context, DynamicObject exception) {
        Backtrace copy = new Backtrace(location, sourceLocation, omitted, javaThrowable);
        // A Backtrace is 1-1-1 with a RaiseException and a Ruby exception
        RaiseException newRaiseException = new RaiseException(context, exception, this.raiseException.isInternalError());
        // Copy the TruffleStackTrace
        TruffleStackTraceElement.fillIn(this.raiseException);
        assert this.raiseException.getCause() != null;
        newRaiseException.initCause(this.raiseException.getCause());
        // Another way would be to copy the activations (copy.activations = getActivations()), but
        // then the TruffleStrackTrace would be inconsistent.
        copy.setRaiseException(newRaiseException);
        return copy;
    }

    public Node getLocation() {
        return location;
    }

    public SourceSection getSourceLocation() {
        return sourceLocation;
    }

    public RaiseException getRaiseException() {
        return raiseException;
    }

    public void setRaiseException(RaiseException raiseException) {
        assert this.raiseException == null : "the RaiseException of a Backtrace must not be set again, otherwise the original backtrace is lost";
        this.raiseException = raiseException;
    }

    public Activation[] getActivations() {
        return getActivations(this.raiseException);
    }

    @TruffleBoundary
    public Activation[] getActivations(TruffleException truffleException) {
        if (this.activations == null) {
            if (truffleException == null) {
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
                    assert i != 0 || stackTraceElement.getLocation() == location;
                    final Node callNode = stackTraceElement.getLocation();

                    if (!callStackManager.ignoreFrame(callNode, stackTraceElement.getTarget())) {
                        final RootNode rootNode = stackTraceElement.getTarget().getRootNode();
                        final String methodName;
                        if (rootNode instanceof RubyRootNode) {
                            // Ruby backtraces do not include the class name for MRI compatibility.
                            methodName = ((RubyRootNode) rootNode).getSharedMethodInfo().getName();
                        } else {
                            methodName = rootNode.getName();
                        }
                        activations.add(new Activation(callNode, methodName));
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
