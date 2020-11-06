/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.arguments.ReadCallerDataNode;
import org.truffleruby.language.arguments.ReadCallerFrameAndVariablesNode;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

/** Some Ruby methods need access to the caller frame (the frame active when the method call was made) or to the storage
 * of special variables within that frame: see usages of {@link ReadCallerFrameNode} and {@link ReadCallerVariablesNode}
 * . This is notably used to get hold of instances of {@link DeclarationContext} and {@link RubyBinding} and methods
 * which need to access the last regexp match or the last io line.
 *
 * <p>
 * This means that when making a method call, we might need to pass down its {@link Frame} or
 * {@link SpecialVariableStorage} active when the method call was made.
 *
 * <p>
 * When retrieving the frame or special variable storage in a method called through the Ruby {@code #send} method, we
 * must not retrieve the frame of the actual call (made by {@code #send}) but the frame of the {@code #send} call
 * itself.
 *
 * <p>
 * Materializing a frame is expensive, and the point of this parent node is to only materialize the frame when we know
 * for sure it has been requested by the callee. It is also possible to walk the stack to retrieve the frame to
 * materialize - but this is even slower and causes a deoptimization in the callee every time we walk the stack.
 *
 * <p>
 * This class works in tandem with {@link ReadCallerFrameNode} for this purpose. At first, we don't send down the frame.
 * If the callee needs it, it will de-optimize and walk the stack to retrieve it (slow). It will also call
 * {@link #startSendingOwnFrame()}}, so that the next time the method is called, the frame will be passed down and the
 * method does not need further de-optimizations. (Note in the case of {@code #send} calls, we need to recursively call
 * {@link ReadCallerFrameNode} to get the parent frame!) {@link ReadCallerVariablesNode} is used similarly to access
 * special variable storage, but for child nodes that only require access to this storage ensures they receive an object
 * that will not require node splitting to be accessed efficiently.
 *
 * <p>
 * This class is the sole consumer of {@link RubyRootNode#getNeedsCallerAssumption()}, which is used to optimize
 * {@link #getFrameOrStorageIfRequired(VirtualFrame)} (called by subclasses in order to pass down the frame or not).
 * Starting to send the frame invalidates the assumption. In other words, the assumption guards the fact that
 * {@link #sendsFrame} is a compilation constant, and is invalidated whenever it needs to change. */
@SuppressFBWarnings("IS")
public abstract class FrameAndVariablesSendingNode extends RubyContextNode {

    private enum SendsFrame {
        NO_FRAME,       // callees don't need to read the frame
        MY_FRAME,       // for most calls
        CALLER_FRAME;   // for `send` calls
    }

    @CompilationFinal protected SendsFrame sendsFrame = SendsFrame.NO_FRAME;
    @CompilationFinal protected Assumption needsCallerAssumption;
    @CompilationFinal protected SendsFrame sendsVariables = SendsFrame.NO_FRAME;

    @Child protected ReadCallerDataNode readCaller;
    @Child protected GetSpecialVariableStorage readMyVariables;

    /** Whether we are sending down the frame (because the called method reads it). */
    protected boolean sendingFrames() {
        return sendsFrame != SendsFrame.NO_FRAME;
    }

    protected boolean sendingVariables() {
        return sendsVariables != SendsFrame.NO_FRAME;
    }

    public void startSendingOwnFrame() {
        if (getContext().getCallStack().callerIsSend()) {
            startSendingFrame(SendsFrame.CALLER_FRAME);
        } else {
            startSendingFrame(SendsFrame.MY_FRAME);
        }
    }

    public void startSendingOwnVariables() {
        if (getContext().getCallStack().callerIsSend()) {
            startSendingVariables(SendsFrame.CALLER_FRAME);
        } else {
            startSendingVariables(SendsFrame.MY_FRAME);
        }
    }

    public void startSendingOwnFrameAndVariables() {
        if (getContext().getCallStack().callerIsSend()) {
            startSendingFrameAndVariables(SendsFrame.CALLER_FRAME);
        } else {
            startSendingFrameAndVariables(SendsFrame.MY_FRAME);
        }
    }

    private synchronized void startSendingFrame(SendsFrame frameToSend) {
        if (sendingFrames()) {
            assert sendsFrame == frameToSend;
            return;
        }

        if (sendingVariables()) {
            assert sendsVariables == frameToSend;
        }

        // We'd only get AlwaysValidAssumption if the root node isn't Ruby (in which case this shouldn't be called),
        // or when we already know to send the frame (in which case we'd have exited above).
        assert needsCallerAssumption != AlwaysValidAssumption.INSTANCE;

        this.sendsFrame = frameToSend;
        if (frameToSend == SendsFrame.CALLER_FRAME) {
            if (!sendingVariables()) {
                this.readCaller = insert(new ReadCallerFrameNode());
            } else {
                this.readCaller = readCaller.replace(new ReadCallerFrameAndVariablesNode());
            }
        }
        Node root = getRootNode();
        if (root instanceof RubyRootNode) {
            ((RubyRootNode) root).invalidateNeedsCallerAssumption();
        } else {
            throw new Error();
        }
    }

    private synchronized void startSendingVariables(SendsFrame variablesToSend) {
        if (sendingFrames()) {
            assert sendsFrame == variablesToSend;
        }

        if (sendingVariables()) {
            assert sendsVariables == variablesToSend;
            return;
        }

        // We'd only get AlwaysValidAssumption if the root node isn't Ruby (in which case this shouldn't be called),
        // or when we already know to send the frame (in which case we'd have exited above).
        assert needsCallerAssumption != AlwaysValidAssumption.INSTANCE;

        this.sendsVariables = variablesToSend;
        if (variablesToSend == SendsFrame.CALLER_FRAME) {
            if (!sendingFrames()) {
                this.readCaller = insert(new ReadCallerVariablesNode());
            } else {
                this.readCaller = readCaller.replace(new ReadCallerFrameAndVariablesNode());
            }
        } else {
            this.readMyVariables = insert(GetSpecialVariableStorage.create());
        }
        Node root = getRootNode();
        if (root instanceof RubyRootNode) {
            ((RubyRootNode) root).invalidateNeedsVariablesAssumption();
        } else {
            throw new Error();
        }
    }

    private synchronized void startSendingFrameAndVariables(SendsFrame dataToSend) {
        if (sendingFrames()) {
            assert sendsFrame == dataToSend;
            return;
        }

        if (sendingVariables()) {
            assert sendsFrame == dataToSend;
            return;
        }

        // We'd only get AlwaysValidAssumption if the root node isn't Ruby (in which case this shouldn't be called),
        // or when we already know to send the frame (in which case we'd have exited above).
        assert needsCallerAssumption != AlwaysValidAssumption.INSTANCE;

        this.sendsFrame = dataToSend;
        this.sendsVariables = dataToSend;
        if (dataToSend == SendsFrame.CALLER_FRAME) {
            if (sendingFrames() || sendingVariables()) {
                this.readCaller = readCaller.replace(new ReadCallerFrameAndVariablesNode());
            } else {
                this.readCaller = insert(new ReadCallerFrameNode());
            }
        }
        Node root = getRootNode();
        if (root instanceof RubyRootNode) {
            ((RubyRootNode) root).invalidateNeedsCallerAssumption();
        } else {
            throw new Error();
        }
    }

    private synchronized void resetNeedsCallerAssumption() {
        Node root = getRootNode();
        if (root instanceof RubyRootNode && (!sendingFrames() || !sendingVariables())) {
            needsCallerAssumption = ((RubyRootNode) root).getNeedsCallerAssumption();
        } else {
            needsCallerAssumption = AlwaysValidAssumption.INSTANCE;
        }
    }

    public Object getFrameOrStorageIfRequired(VirtualFrame frame) {
        if (frame == null) { // the frame should be proved null or non-null at PE time
            return null;
        }

        if (needsCallerAssumption == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
        }
        try {
            needsCallerAssumption.check();
        } catch (InvalidAssumptionException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
        }

        if (sendsVariables == SendsFrame.NO_FRAME && sendsFrame == SendsFrame.NO_FRAME) {
            return null;
        } else if (sendsVariables == SendsFrame.MY_FRAME && sendsFrame == SendsFrame.NO_FRAME) {
            return readMyVariables.execute(frame);
        } else if (sendsVariables == SendsFrame.NO_FRAME && sendsFrame == SendsFrame.MY_FRAME) {
            return frame.materialize();
        } else if (sendsVariables == SendsFrame.MY_FRAME && sendsFrame == SendsFrame.MY_FRAME) {
            return new FrameAndVariables(readMyVariables.execute(frame), frame.materialize());
        } else if ((sendsVariables == SendsFrame.CALLER_FRAME && sendsFrame == SendsFrame.MY_FRAME) ||
                (sendsVariables == SendsFrame.MY_FRAME && sendsFrame == SendsFrame.CALLER_FRAME)) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            CompilerDirectives.shouldNotReachHere();
            return null;
        } else {
            return readCaller.execute(frame);
        }
    }

}
