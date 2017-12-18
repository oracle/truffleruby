/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.globals;

import org.truffleruby.core.regexp.MatchDataNodes.ValuesNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.threadlocal.GetFromThreadAndFrameLocalStorageNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ReadMatchReferenceNodes extends RubyNode {

    public static final int PRE = -1;
    public static final int POST = -2;
    public static final int GLOBAL = -3;
    public static final int HIGHEST = -4;

    public static class ReadPreMatchNode extends RubyNode {
        @Child CallDispatchHeadNode preMatchNode = CallDispatchHeadNode.create();
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadPreMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            return preMatchNode.call(frame, match, "pre_match");
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

    public static class ReadPostMatchNode extends RubyNode {
        @Child CallDispatchHeadNode postMatchNode = CallDispatchHeadNode.create();
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadPostMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            return postMatchNode.call(frame, match, "post_match");
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

    public static class ReadMatchNode extends RubyNode {
        @Child private CallDispatchHeadNode getMatchIndexNode = CallDispatchHeadNode.create();
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;
        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            return getMatchIndexNode.call(frame, match, "[]", 0);
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

    public static class ReadHighestMatchNode extends RubyNode {
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;
        @Child private ValuesNode getValues = ValuesNode.create();

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadHighestMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            final DynamicObject matchData = (DynamicObject) match;

            final Object[] values = getValues.execute(matchData);

            for (int n = values.length - 1; n >= 0; n--) {
                if (values[n] != nil()) {
                    return values[n];
                }
            }

            return nil();
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

    public static class ReadNthMatchNode extends RubyNode {
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;
        @Child private CallDispatchHeadNode getIndexNode = CallDispatchHeadNode.create();
        private final int index;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadNthMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode, int index) {
            this.readMatchNode = readMatchNode;
            this.index = index;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            return getIndexNode.call(frame, match, "[]", index);
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
