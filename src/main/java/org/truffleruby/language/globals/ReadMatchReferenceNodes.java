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

import org.truffleruby.Layouts;
import org.truffleruby.core.regexp.MatchDataNodes.PostMatchNode;
import org.truffleruby.core.regexp.MatchDataNodes.PreMatchNode;
import org.truffleruby.core.regexp.MatchDataNodes.ValuesNode;
import org.truffleruby.core.regexp.MatchDataNodes.GlobalNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.threadlocal.GetFromThreadAndFrameLocalStorageNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ReadMatchReferenceNodes {

    public static final int PRE = -1;
    public static final int POST = -2;
    public static final int GLOBAL = -3;
    public static final int HIGHEST = -4;

    public static class ReadPreMatchNode extends RubyNode {
        @Child PreMatchNode preMatchNode = PreMatchNode.create(null);
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

            final DynamicObject matchData = (DynamicObject) match;

            return preMatchNode.execute(matchData);
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
        @Child PostMatchNode postMatchNode = PostMatchNode.create(null);
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

            final DynamicObject matchData = (DynamicObject) match;

            return postMatchNode.execute(matchData);
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

    public static class ReadGlobalMatchNode extends RubyNode {
        @Child GlobalNode tosMatchNode = GlobalNode.create(null);
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;

        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();

        public ReadGlobalMatchNode(GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object match = readMatchNode.execute(frame);

            if (matchNilProfile.profile(match == nil())) {
                return nil();
            }

            final DynamicObject matchData = (DynamicObject) match;

            return tosMatchNode.execute(matchData);
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

            for (int n = values.length - 1; n >= 0; n--)
                if (values[n] != nil()) {
                    return values[n];
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
        @Child private ValuesNode getValues = ValuesNode.create();
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

            final DynamicObject matchData = (DynamicObject) match;

            final Object[] values = getValues.execute(matchData);

            if (index >= values.length) {
                return nil();
            } else {
                return values[index];
            }
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

    public static RubyNode create(GetFromThreadAndFrameLocalStorageNode readMatchNode2, int index) {
        switch (index) {
            case PRE:
                return new ReadPreMatchNode(readMatchNode2);
            case POST:
                return new ReadPostMatchNode(readMatchNode2);
            case GLOBAL:
                return new ReadGlobalMatchNode(readMatchNode2);
            case HIGHEST:
                return new ReadHighestMatchNode(readMatchNode2);
            default:
                return new ReadNthMatchNode(readMatchNode2, index);
        }
    }

}
