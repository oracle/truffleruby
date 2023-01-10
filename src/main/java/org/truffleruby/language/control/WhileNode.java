/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.TruffleSafepoint;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;

public final class WhileNode extends RubyContextSourceNode {

    @Child private LoopNode loopNode;

    public WhileNode(RepeatingNode repeatingNode) {
        loopNode = Truffle.getRuntime().createLoopNode(repeatingNode);
    }

    private WhileNode(LoopNode loopNode) {
        this.loopNode = loopNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        loopNode.execute(frame);
        return nil;
    }

    public RubyNode cloneUninitialized() {
        var repeatingNode = (WhileRepeatingBaseNode) loopNode.getRepeatingNode();
        var copy = new WhileNode(repeatingNode.cloneUninitialized());
        return copy.copyFlags(this);
    }

    private abstract static class WhileRepeatingBaseNode extends RubyBaseNode implements RepeatingNode {

        @Child protected BooleanCastNode condition;
        @Child protected RubyNode body;

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

        public abstract WhileRepeatingBaseNode cloneUninitialized();
    }

    public static class WhileRepeatingNode extends WhileRepeatingBaseNode implements RepeatingNode {

        public WhileRepeatingNode(RubyNode condition, RubyNode body) {
            super(condition, body);
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            if (!condition.execute(frame)) {
                return false;
            }

            while (true) { // for redo
                try {
                    body.doExecuteVoid(frame);
                    return true;
                } catch (NextException e) {
                    nextUsed.enter();
                    return true;
                } catch (RedoException e) {
                    // Just continue in the while(true) loop.
                    redoUsed.enter();
                    TruffleSafepoint.poll(this);
                }
            }
        }

        @Override
        public WhileRepeatingBaseNode cloneUninitialized() {
            return new WhileRepeatingNode(
                    condition.getValueNode().cloneUninitialized(),
                    body.cloneUninitialized());
        }

    }

    public static class DoWhileRepeatingNode extends WhileRepeatingBaseNode implements RepeatingNode {

        public DoWhileRepeatingNode(RubyNode condition, RubyNode body) {
            super(condition, body);
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            try {
                body.doExecuteVoid(frame);
            } catch (NextException e) {
                nextUsed.enter();
            } catch (RedoException e) {
                // Just continue to next iteration without executing the condition.
                redoUsed.enter();
                return true;
            }

            return condition.execute(frame);
        }

        private RubyNode getConditionBeforeCasting() {
            return condition.getValueNode();
        }

        @Override
        public WhileRepeatingBaseNode cloneUninitialized() {
            return new DoWhileRepeatingNode(
                    getConditionBeforeCasting().cloneUninitialized(),
                    body.cloneUninitialized());
        }

    }

}
