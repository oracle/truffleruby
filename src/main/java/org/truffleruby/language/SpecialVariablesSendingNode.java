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
import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;

import org.truffleruby.language.threadlocal.SpecialVariableStorage;

/** Some Ruby methods need access to the caller special variables: see usages of {@link ReadCallerVariablesNode}. This
 * is used for methods which need to access the last regexp MatchData or the last IO line.
 *
 * <p>
 * This means that when making a method call, we might need to pass down its {@link SpecialVariableStorage} active when
 * the method call was made.
 *
 * <p>
 * Always passing the {@link SpecialVariableStorage} would be expensive, and the point of this base node is to only pass
 * it when we know for sure it has been requested by the callee. It is also possible to walk the stack to retrieve the
 * caller special variables - but this is even slower and causes a deoptimization in the callee every time we walk the
 * stack.
 *
 * <p>
 * This class works in tandem with {@link GetSpecialVariableStorage} for this purpose. At first, we don't send down the
 * {@link SpecialVariableStorage}. If the callee needs it, it will de-optimize and walk the stack to retrieve it (slow).
 * It will also call {@link #startSendingOwnVariables()}, so that the next time the method is called, the special
 * variables will be passed down and the method does not need further de-optimizations.
 *
 * <p>
 * {@link ReadCallerVariablesNode} is used by child nodes that require access to this storage and this mechanism ensures
 * they receive an object that will not require CallTarget splitting to be accessed efficiently (i.e., part of the
 * calling convention and not having to find the value in the caller frame). */
@SuppressFBWarnings("IS")
public abstract class SpecialVariablesSendingNode extends RubyBaseNode {

    @Child protected GetSpecialVariableStorage readingNode;

    public void startSendingOwnVariables() {
        synchronized (this) {
            if (readingNode == null) {
                readingNode = insert(GetSpecialVariableStorage.create());
            }
        }
    }

    public SpecialVariableStorage getSpecialVariablesIfRequired(Frame frame) {
        if (readingNode == null) {
            return null;
        } else {
            return readingNode.execute(frame);
        }
    }

}
