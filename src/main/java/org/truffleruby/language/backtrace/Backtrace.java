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

/**
 * Represents a backtrace: a list of activations (~ call sites).
 *
 * <p>A backtrace is always constructed from a Java throwable, but does not always correspond to a
 * Ruby exception (e.g. {@code Kernel.caller_locations}). Whenever constructing a backtrace from a
 * Ruby exception, it will be encapsulated in a Java throwable ({@link RaiseException}).
 *
 * <p>Whenever a backtrace is associated with a Ruby exception, there is a 1-1-1 match between
 * the backtrace, the Ruby exception (which has a backtrace field) and the
 * {@link #getRaiseException() raiseException} stored in the backtrace (which encapsulates the
 * Ruby exception).
 *
 * <p>NOTE(norswap): At least, that's how it should work, but there are cases where a Ruby
 * exception's backtrace may not link back to the exception (e.g. in {@code
 * TranslateExceptionNode#translateThrowable}).
 *
 * <p>Note in passing that not all Ruby exceptions have an associated backtrace (simply creating an
 * exception via {@code Exception.new} does not fill its backtrace), nor does the associated
 * backtrace necessarily match the result of {@code Exception#backtrace} in Ruby, because the user
 * may have set a custom backtrace (an array of strings).
 *
 * <p>In general, there isn't any guarantee that the getters will return non-null values, excepted
 * {@link #getActivations(), {@link #getActivations(Throwable)} and {@link #getBacktraceLocations(int)}.
 *
 * <p>NOTE(norswap): And this is somewhat unfortunate, as it's difficult to track the assumptions
 * on the backtrace object and generally require being very defensive depending on the information
 * available - but fixing this would require a dangerous refactoring for little benefit.
 *
 * <p>Also note that the activations are recorded lazily when one of the aforementionned methods is
 * called, excepted when specified otherwise in the constructor. The activations will match the
 * state of the Truffle call stack whenever the activations are recorded (so, during the constructor
 * call or first method call).
 */
public class Backtrace {

    // See accessors for info on most undocumented fields.

    private final Node location;
    private final SourceSection sourceLocation;
    private final int omitted;
    private RaiseException raiseException;
    private final Throwable javaThrowable;
    private Activation[] activations;

    /** How many activations would there be if omitted was 0? */
    private int totalUnderlyingActivations;

    // region Constructors

    /**
     * Fully explicit constructor.
     */
    public Backtrace(Node location, SourceSection sourceLocation, int omitted, Throwable javaThrowable) {
        this.location = location;
        this.sourceLocation = sourceLocation;
        this.omitted = omitted;
        this.javaThrowable = javaThrowable;
    }

    /**
     * Creates a backtrace for the given Truffle exception, setting the
     * {@link #getLocation() location} and {@link #getSourceLocation() source location} accordingly,
     * and computing the activations eagerly (since the exception itself is not retained).
     *
     * <p>This is not/should not be used for constructing the backtrace associated with Ruby
     * exceptions.
     */
    public Backtrace(TruffleException exception) {
        this.location = exception.getLocation();
        this.sourceLocation = exception.getSourceLocation();
        this.omitted = 0;
        this.javaThrowable = null;
        this.activations = getActivations((Throwable) exception);
    }

    /**
     * Creates a backtrace for the given throwable, in which only the activations and the backtrace
     * locations may be retrieved. The activations are computed eagerly, since the exception itself
     * is not retained.
     */
    public Backtrace(Throwable exception) {
        this.location = null;
        this.sourceLocation = null;
        this.omitted = 0;
        this.javaThrowable = null;
        this.activations = getActivations(exception);
    }

    // endregion
    // region Accessors

    /**
     * AST node that caused the associated exception, if the info is available, or null.
     */
    public Node getLocation() {
        return location;
    }

    /**
     * Only set for {@code SyntaxError}, where it represents where the error occurred
     * (while {@link #getLocation()} does not).
     */
    public SourceSection getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Returns the wrapper for the Ruby exception associated with this backtrace, if any, and
     * null otherwise.
     */
    public RaiseException getRaiseException() {
        return raiseException;
    }

    /**
     * Sets the wrapper for the Ruby exception associated with this backtrace.
     *
     * <p>Do not set the raise exception twice on the same backtrace!
     */
    public void setRaiseException(RaiseException raiseException) {
        assert this.raiseException == null : "the RaiseException of a Backtrace must not be set again, otherwise the original backtrace is lost";
        this.raiseException = raiseException;
    }

    /**
     * Returns the number of activations to omit from the top (= most recently called) of the
     * activation stack.
     */
    public int getOmitted() {
        return omitted;
    }

    /**
     * Returns the Java exception the associated Ruby exception was translated from, if any.
     * (This is not the same as {@link #getRaiseException() the raise exception} which is simply
     * a wrapper around the Ruby exception.)
     */
    public Throwable getJavaThrowable() {
        return javaThrowable;
    }

    // endregion

    /**
     * Used to copy the backtrace when copying {@code exception}.
     */
    public Backtrace copy(RubyContext context, DynamicObject exception) {
        Backtrace copy = new Backtrace(location, sourceLocation, omitted, javaThrowable);
        // A Backtrace is 1-1-1 with a RaiseException and a Ruby exception.
        RaiseException newRaiseException = new RaiseException(
                context,
                exception,
                this.raiseException.isInternalError());
        // Copy the TruffleStackTrace
        //noinspection ThrowableNotThrown
        TruffleStackTrace.fillIn(this.raiseException);
        assert this.raiseException.getCause() != null;
        newRaiseException.initCause(this.raiseException.getCause());
        // Another way would be to copy the activations (copy.activations = getActivations()), but
        // then the TruffleStrackTrace would be inconsistent.
        copy.setRaiseException(newRaiseException);
        return copy;
    }

    @TruffleBoundary
    public Activation[] getActivations(Throwable truffleException) {
        if (this.activations != null) {
            return this.activations;
        }

        if (truffleException == null) {
            truffleException = new GetBacktraceException(location, GetBacktraceException.UNLIMITED);
        }

        // The stacktrace is computed here if it was not already computed and stored in the
        // TruffleException with TruffleStackTraceElement.fillIn().
        final List<TruffleStackTraceElement> stackTrace = TruffleStackTrace.getStackTrace(truffleException);
        assert stackTrace != null;

        final List<Activation> activations = new ArrayList<>();
        final RubyContext context = RubyLanguage.getCurrentContext();
        final CallStackManager callStackManager = context.getCallStack();

        int elementCount = 0;
        int activationCount = 0;
        for (TruffleStackTraceElement stackTraceElement : stackTrace) {
            assert elementCount != 0 || stackTraceElement.getLocation() == location;
            final Node callNode = stackTraceElement.getLocation();
            ++elementCount;

            if (callStackManager.ignoreFrame(callNode, stackTraceElement.getTarget())) {
                continue;
            }

            if (activationCount < omitted) {
                ++activationCount;
                continue;
            }

            final RootNode rootNode = stackTraceElement.getTarget().getRootNode();
            final String methodName;
            if (rootNode instanceof RubyRootNode) {
                // Ruby backtraces do not include the class name for MRI compatibility.
                methodName = ((RubyRootNode) rootNode).getSharedMethodInfo().getName();
            } else {
                methodName = rootNode.getName();
            }

            // TODO (eregon, 4 Feb 2019): we should not ignore foreign frames without a
            //  call node, but print info based on the methodName and CallTarget.
            if (rootNode instanceof RubyRootNode || callNode != null) {
                activations.add(new Activation(callNode, methodName));
            }

            activationCount++;
        }

        // If there are activations with a InternalMethod but no caller information above in the
        // stack, then all of these activations are internal as they are not called from user code.
        while (!activations.isEmpty() && activations.get(activations.size() - 1).getCallNode() == null) {
            activations.remove(activations.size() - 1);
            --activationCount;
        }

        this.totalUnderlyingActivations = activationCount;
        return this.activations = activations.toArray(new Activation[activations.size()]);
    }

    public Activation[] getActivations() {
        return getActivations(this.raiseException);
    }

    /**
     * Returns a ruby array of {@code Thread::Backtrace::Locations} with maximum length {@code
     * length}, and omitting locations as requested ({@link #getOmitted()}). If more locations are
     * omitted than are available, return a Ruby {@code nil}.
     *
     * <p>The length can be negative, in which case it is treated as a range ending. Use -1 to
     * get the maximum length.
     *
     * <p>This causes the activations to be computed if not yet the case.
     *
     * @param length the maximum number of locations to return (if positive), or -1 minus the
     *               number of items to exclude at the end. You can use
     *               {@link GetBacktraceException#UNLIMITED} to signal that you want all locations.
     *
     * @param node the node at which we're requiring the backtrace. Can be null if the backtrace
     *             is associated with a ruby exception or if we are sure the activations have
     *             already been computed.
     *
     */
    public DynamicObject getBacktraceLocations(int length, Node node) {

        final RubyContext context = RubyLanguage.getCurrentContext();

        final int activationsLength;
        if (this.raiseException != null) {
            // When dealing with the backtrace of a Ruby exception, we use the wrapping
            // exception and we don't set a limit on the retrieved activations.
            activationsLength = getActivations().length;
        }
        else {
            // We can't set an effective limit when dealing with negative range endings.
            final int stackTraceElementsLimit = length < 0
                    ? GetBacktraceException.UNLIMITED
                    : omitted + length;
            final Throwable e = new GetBacktraceException(node, stackTraceElementsLimit);
            activationsLength = getActivations(e).length;
        }
        
        // Omitting more locations than available should return nil.
        if (activationsLength == 0) {
            return omitted > totalUnderlyingActivations
                    ? context.getCoreLibrary().nil
                    : ArrayHelpers.createEmptyArray(context);
        }

        // NOTE (norswap, 08 Jan 2020)
        //  It used to be that TruffleException#getStackTraceElementLimit() wasn't respected
        //  due to a mishandling of internal frames. That's why we used Math.min and not just
        //  length. Leaving it in just in case.
        final int locationsLength = length < 0
                ? activationsLength + 1 + length
                : Math.min(activationsLength, length);
        
        final Object[] locations = new Object[locationsLength];
        final DynamicObjectFactory factory = context.getCoreLibrary().threadBacktraceLocationFactory;
        for (int i = 0; i < locationsLength; i++) {
            locations[i] = Layouts.THREAD_BACKTRACE_LOCATION.createThreadBacktraceLocation(factory, this, i);
        }
        return ArrayHelpers.createArray(context, locations, locations.length);
    }
}
