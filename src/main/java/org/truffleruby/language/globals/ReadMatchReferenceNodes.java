/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ReadMatchReferenceNodes extends RubyNode {

    public static class ReadNthMatchNode extends RubyNode {
        @Child private RubyNode readMatchNode;
        @Child private CallDispatchHeadNode getIndexNode;
        private final int index;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadNthMatchNode(RubyNode readMatchNode, int index) {
            this.readMatchNode = readMatchNode;
            this.index = index;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            return callGetIndex(frame, match, index);
        }

        private Object callGetIndex(VirtualFrame frame, Object match, int index) {
            if (getIndexNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                getIndexNode = insert(CallDispatchHeadNode.createPrivate());
            }
            return getIndexNode.call(match, "[]", index);
        }

        @Override
        public Object isDefined(VirtualFrame frame) {
            if (isNil(execute(frame))) {
                return nil();
            } else {
                return coreStrings().GLOBAL_VARIABLE.createInstance();
            }
        }
    }

    public static class SetNamedVariablesMatchNode extends RubyNode {
        @Child private RubyNode matchDataNode;
        @Child private RubyNode readMatchNode;
        @Children private final RubyNode[] setters;
        @Children private final RubyNode[] nilSetters;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public SetNamedVariablesMatchNode(RubyNode matchDataNode, RubyNode readMatchNode, RubyNode[] setters, RubyNode[] nilSetters) {
            this.matchDataNode = matchDataNode;
            this.readMatchNode = readMatchNode;
            this.setters = setters;
            this.nilSetters = nilSetters;

        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object matchResult = matchDataNode.execute(frame);
            DynamicObject match = (DynamicObject) readMatchNode.execute(frame);
            if (matchNilProfile.profile(match == nil())) {
                setNamedForNil(frame);
            } else {
                setNamedForNonNil(frame);
            }
            return matchResult;
        }

        @ExplodeLoop
        private void setNamedForNonNil(VirtualFrame frame) {
            for (int n = 0; n < setters.length; n++) {
                setters[n].execute(frame);
            }
        }

        @ExplodeLoop
        private void setNamedForNil(VirtualFrame frame) {
            for (int n = 0; n < setters.length; n++) {
                nilSetters[n].execute(frame);
            }
        }

    }
}
