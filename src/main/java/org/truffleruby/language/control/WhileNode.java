/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.language.SafepointManager;

public final class WhileNode extends RubyContextSourceNode {

    @Child private LoopNode loopNode;

    public WhileNode(RepeatingNode repeatingNode) {
        loopNode = Truffle.getRuntime().createLoopNode(repeatingNode);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        loopNode.execute(frame);
        return nil;
    }

    private abstract static class WhileRepeatingBaseNode extends RubyContextNode implements RepeatingNode {

        @Child protected BooleanCastNode condition;
        @Child protected RubyNode body;

        protected final LoopConditionProfile conditionProfile = LoopConditionProfile.createCountingProfile();
        protected final BranchProfile redoUsed = BranchProfile.create();
        protected final BranchProfile nextUsed = BranchProfile.create();

        public WhileRepeatingBaseNode(RubyNode condition, RubyNode body) {
            this.condition = BooleanCastNodeGen.create(condition);
            this.body = body;
        }

        @Override
        public String toString() {
            return "while loop at " + RubyLanguage.filenameLine(getEncapsulatingSourceSection());
        }

    }

    public static class WhileRepeatingNode extends WhileRepeatingBaseNode implements RepeatingNode {

        public WhileRepeatingNode(RubyNode condition, RubyNode body) {
            super(condition, body);
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            if (!conditionProfile.profile(condition.executeBoolean(frame))) {
                return false;
            }

            while (true) { // for redo
                SafepointManager.poll(getLanguage(), this);
                try {
                    body.execute(frame);
                    return true;
                } catch (NextException e) {
                    nextUsed.enter();
                    return true;
                } catch (RedoException e) {
                    // Just continue in the while(true) loop.
                    redoUsed.enter();
                }
            }
        }

    }

    public static class DoWhileRepeatingNode extends WhileRepeatingBaseNode implements RepeatingNode {

        public DoWhileRepeatingNode(RubyNode condition, RubyNode body) {
            super(condition, body);
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            SafepointManager.poll(getLanguage(), this);
            try {
                body.execute(frame);
            } catch (NextException e) {
                nextUsed.enter();
            } catch (RedoException e) {
                // Just continue to next iteration without executing the condition.
                redoUsed.enter();
                return true;
            }

            return conditionProfile.profile(condition.executeBoolean(frame));
        }

    }

}
