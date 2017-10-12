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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.joni.NameEntry;
import org.joni.Regex;
import org.joni.Region;
import org.truffleruby.Layouts;
import org.truffleruby.core.regexp.MatchDataNodes.MatchNode;
import org.truffleruby.core.regexp.MatchDataNodes.PostMatchNode;
import org.truffleruby.core.regexp.MatchDataNodes.PreMatchNode;
import org.truffleruby.core.regexp.MatchDataNodes.ValuesNode;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.threadlocal.GetFromThreadAndFrameLocalStorageNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ReadMatchReferenceNodes extends RubyNode {

    public static final int PRE = -1;
    public static final int POST = -2;
    public static final int GLOBAL = -3;
    public static final int HIGHEST = -4;

    public static class ReadPreMatchNode extends RubyNode {
        @Child PreMatchNode preMatchNode = PreMatchNode.create();
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
        @Child PostMatchNode postMatchNode = PostMatchNode.create();
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

    public static class ReadMatchNode extends RubyNode {
        @Child MatchNode tosMatchNode = MatchNode.create();
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

    public static class SetNamedVariablesMatchNode extends RubyNode {
        @Child private RubyNode matchDataNode;
        @Child private GetFromThreadAndFrameLocalStorageNode readMatchNode;
        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();
        protected final ConditionProfile matchNilProfile = ConditionProfile.createBinaryProfile();
        private final Regex regex;

        public SetNamedVariablesMatchNode(Regex regex, RubyNode matchDataNode, GetFromThreadAndFrameLocalStorageNode readMatchNode) {
            this.regex = regex;
            this.matchDataNode = matchDataNode;
            this.readMatchNode = readMatchNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object matchResult = matchDataNode.execute(frame);
            DynamicObject match = (DynamicObject) readMatchNode.execute(frame);
            MaterializedFrame frame2 = frame.materialize();
            if (matchNilProfile.profile(match == nil())) {
                setNamedForNil(frame2);
            } else {
                setNamedForNonNil(frame2, regex, Layouts.MATCH_DATA.getSource(match), match);
            }
            return matchResult;
        }

        @TruffleBoundary
        private void setNamedForNonNil(MaterializedFrame frame, Regex regex, DynamicObject string, DynamicObject matchData) {
            DynamicObject nil = nil();
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            for (Iterator<NameEntry> i = regex.namedBackrefIterator(); i.hasNext();) {
                final NameEntry e = i.next();
                final String name = new String(e.name, e.nameP, e.nameEnd - e.nameP, StandardCharsets.UTF_8).intern();
                int nth = regex.nameToBackrefNumber(e.name, e.nameP, e.nameEnd, region);

                final Object value;

                // Copied from jruby/RubyRegexp - see copyright notice there

                if (nth >= region.numRegs || (nth < 0 && (nth += region.numRegs) <= 0)) {
                    value = nil;
                } else {
                    final int start = region.beg[nth];
                    final int end = region.end[nth];
                    if (start != -1) {
                        value = createSubstring(string, start, end - start);
                    } else {
                        value = nil;
                    }
                }

                setLocalVariable(frame, name, value);
            }
        }

        @TruffleBoundary
        private void setNamedForNil(MaterializedFrame frame) {
            DynamicObject nil = nil();
            for (Iterator<NameEntry> i = regex.namedBackrefIterator(); i.hasNext();) {
                final NameEntry e = i.next();
                final String name = new String(e.name, e.nameP, e.nameEnd - e.nameP, StandardCharsets.UTF_8).intern();
                setLocalVariable(frame, name, nil);
            }
        }

        private static void setLocalVariable(MaterializedFrame frame, String name, Object value) {
            assert value != null;

            while (frame != null) {
                final FrameSlot slot = frame.getFrameDescriptor().findFrameSlot(name);
                if (slot != null) {
                    frame.setObject(slot, value);
                    break;
                }

                frame = RubyArguments.getDeclarationFrame(frame);
            }
        }

        private DynamicObject createSubstring(DynamicObject source, int start, int length) {
            assert RubyGuards.isRubyString(source);

            final Rope sourceRope = StringOperations.rope(source);
            final Rope substringRope = makeSubstringNode.executeMake(sourceRope, start, length);

            final DynamicObject ret = Layouts.CLASS.getInstanceFactory(Layouts.BASIC_OBJECT.getLogicalClass(source)).newInstance(Layouts.STRING.build(false, false, substringRope));

            return ret;
        }
    }
}
