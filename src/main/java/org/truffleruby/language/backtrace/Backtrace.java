/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.object.DynamicObjectFactory;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public class Backtrace {

    private final Node location;
    /** Only set for SyntaxError, where getLocation() does not represent where the error occurred. */
    private final SourceSection sourceLocation;
    private RaiseException raiseException;
    private Activation[] activations;
    private final int omitted;
    private final Throwable javaThrowable;

    public Backtrace(Node location, SourceSection sourceLocation, int omitted, Throwable javaThrowable) {
        this.location = location;
        this.sourceLocation = sourceLocation;
        this.omitted = omitted;
        this.javaThrowable = javaThrowable;
    }

    public Backtrace(TruffleException exception) {
        this.location = exception.getLocation();
        this.sourceLocation = exception.getSourceLocation();
        this.omitted = 0;
        this.javaThrowable = null;

        this.activations = getActivations((Throwable) exception);
    }

    public Backtrace(Throwable exception) {
        this.location = null;
        this.sourceLocation = null;
        this.omitted = 0;
        this.javaThrowable = null;

        this.activations = getActivations(exception);
    }

    public Backtrace copy(RubyContext context, DynamicObject exception) {
        Backtrace copy = new Backtrace(location, sourceLocation, omitted, javaThrowable);
        // A Backtrace is 1-1-1 with a RaiseException and a Ruby exception
        RaiseException newRaiseException = new RaiseException(
                context,
                exception,
                this.raiseException.isInternalError());
        // Copy the TruffleStackTrace
        TruffleStackTrace.fillIn(this.raiseException);
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
    public Activation[] getActivations(Throwable truffleException) {
        if (this.activations == null) {
            if (truffleException == null) {
                truffleException = new GetBacktraceException(location, GetBacktraceException.UNLIMITED);
            }

            // The stacktrace is computed here if it was not already computed and stored in the
            // TruffleException with TruffleStackTraceElement.fillIn().
            final List<TruffleStackTraceElement> stackTrace = TruffleStackTrace.getStackTrace(truffleException);

            final List<Activation> activations = new ArrayList<>();
            final RubyContext context = RubyLanguage.getCurrentContext();
            final CallStackManager callStackManager = context.getCallStack();

            int i = 0;
            for (TruffleStackTraceElement stackTraceElement : stackTrace) {
                // TODO(norswap, 24 Dec. 2019)
                //   Seems wrong to me, this causes frames that would otherwise be ignored to
                //   count towards the omitted frames.
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

                        // TODO (eregon, 4 Feb 2019): we should not ignore foreign frames without a
                        // call node, but print info based on the methodName and CallTarget.
                        if (rootNode instanceof RubyRootNode || callNode != null) {
                            activations.add(new Activation(callNode, methodName));
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

    /**
     * Returns a ruby array of {@code Thread::Backtrace::Locations} with maximum length {@code
     * length}, and omitting locations as requested ({@link #getOmitted()}). If more locations
     * are omitted than are available, return a Ruby {@code nil}.
     *
     * <p>The length can be negative, in which case it is treated as a range ending. Use -1 to
     * get the maximum length.
     *
     * <p>If the stack trace hasn't been filled yet, this method will fill it.
     */
    public DynamicObject getBacktraceLocations(int length) {

        final RubyContext context = RubyLanguage.getCurrentContext();

        // NOTE(norswap, 24 Dec 2019)
        //   Causes the stack trace to be filled if not done already.
        //   We must call this rather than TruffleStackTrace#getStackTrace because
        //   it does some additional filtering.
        // TODO(norswap, 20 Dec 2019)
        //   Currently, only the filtering at the end is taken into account (as length reduction).
        //   Filtering in the middle will be ignored because of how we build backtrace locations.
        final int activationsLength = getActivations().length;

        // TODO(norswap, 24 Dec 2019)
        //   This is an ugly stopgap solution — this doesn't seem solvable without refactoring #getActivations.
        //   The issue is that omitting more locations than are available should return nil, while
        //   omitting exactly the number of available locations should return an empty array.
        //   This isn't even entirely correct: if we should ignore some frames from the stack trace,
        //   then it's possible the method will return an empty array instead of nil.
        if (activationsLength == 0 && omitted > 0) {
            final int fullStackTraceLength = TruffleStackTrace.getStackTrace(
                    new GetBacktraceException(location, GetBacktraceException.UNLIMITED)).size();
            if (omitted > fullStackTraceLength) {
                return context.getCoreLibrary().nil;
            }
        }

        // NOTE (norswap, 18 Dec 2019)
        //  TruffleStackTrace#getStackTrace (hence Backtrace#getActivations too) does not
        //  always respect TruffleException#getStackTraceElementLimit(), so we need to use Math#min.
        //  Haven't yet investigated why.
        final int locationsLength = length < 0
            ? activationsLength + 1 + length
            : Math.min(activationsLength, length);

        final Object[] locations = new Object[locationsLength];
        final DynamicObjectFactory factory = context.getCoreLibrary().threadBacktraceLocationFactory;
        for (int i = 0; i < locationsLength; i++) {
            locations[i] = Layouts.THREAD_BACKTRACE_LOCATION.createThreadBacktraceLocation(
                factory, this, i);
        }
        return ArrayHelpers.createArray(context, locations, locations.length);
    }

    public int getOmitted() {
        return omitted;
    }

    public Throwable getJavaThrowable() {
        return javaThrowable;
    }
}
