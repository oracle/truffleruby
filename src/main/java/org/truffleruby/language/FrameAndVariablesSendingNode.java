/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;

import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

/** Some Ruby methods need access to the caller frame (the frame active when the method call was made) or to the storage
 * of special variables within that frame: see usages of {@link ReadCallerFrameNode} and {@link ReadCallerVariablesNode}
 * . This is notably used to get hold of instances of {@link DeclarationContext} and {@link RubyBinding} and methods
 * which need to access the last regexp MatchData or the last IO line.
 *
 * <p>
 * This means that when making a method call, we might need to pass down its {@link Frame} or
 * {@link SpecialVariableStorage} active when the method call was made.
 *
 * <p>
 * Materializing a frame is expensive, and the point of this parent node is to only materialize the frame when we know
 * for sure it has been requested by the callee. It is also possible to walk the stack to retrieve the frame to
 * materialize - but this is even slower and causes a deoptimization in the callee every time we walk the stack.
 *
 * <p>
 * This class works in tandem with {@link FrameOrVariablesReadingNode} for this purpose. At first, we don't send down
 * the frame. If the callee needs it, it will de-optimize and walk the stack to retrieve it (slow). It will also call
 * {@link #startSendingOwnFrame()}}, so that the next time the method is called, the frame will be passed down and the
 * method does not need further de-optimizations.
 *
 * <p>
 * {@link ReadCallerVariablesNode} is used similarly to access special variable storage, but for child nodes that only
 * require access to this storage ensures they receive an object that will not require node splitting to be accessed
 * efficiently. */
@SuppressFBWarnings("IS")
public abstract class FrameAndVariablesSendingNode extends RubyBaseNode {

    @Child protected FrameOrVariablesReadingNode readingNode;

    public boolean sendingFrames() {
        if (readingNode == null) {
            return false;
        } else {
            return readingNode.sendingFrame();
        }
    }

    private synchronized void startSending(boolean variables, boolean frame) {
        if (readingNode != null) {
            readingNode.startSending(variables, frame);
        } else if (variables && !frame) {
            readingNode = insert(GetSpecialVariableStorage.create());
        } else if (!variables && frame) {
            readingNode = insert(new ReadOwnFrameNode());
        }
    }

    /** Whether we are sending down the frame (because the called method reads it). */
    public void startSendingOwnFrame() {
        startSending(false, true);
    }

    public void startSendingOwnVariables() {
        startSending(true, false);
    }

    public Object getFrameOrStorageIfRequired(Frame frame) {
        if (readingNode == null) {
            return null;
        } else {
            return readingNode.execute(frame);
        }
    }

}
