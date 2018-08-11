/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;

public abstract class HashLiteralNode extends RubyNode {

    @Children protected final RubyNode[] keyValues;

    protected HashLiteralNode(RubyNode[] keyValues) {
        assert keyValues.length % 2 == 0;
        this.keyValues = keyValues;
    }

    public static HashLiteralNode create(RubyContext context, RubyNode[] keyValues) {
        if (keyValues.length == 0) {
            return new EmptyHashLiteralNode();
        } else if (keyValues.length <= context.getOptions().HASH_PACKED_ARRAY_MAX * 2) {
            return new SmallHashLiteralNode(keyValues);
        } else {
            return new GenericHashLiteralNode(keyValues);
        }
    }

    @ExplodeLoop
    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        for (RubyNode child : keyValues) {
            child.doExecuteVoid(frame);
        }
    }

    public static class EmptyHashLiteralNode extends HashLiteralNode {

        public EmptyHashLiteralNode() {
            super(RubyNode.EMPTY_ARRAY);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return HashOperations.newEmptyHash(getContext());
        }

    }

    public static class SmallHashLiteralNode extends HashLiteralNode {

        private final ConditionProfile stringKeyProfile = ConditionProfile.createBinaryProfile();

        @Child private HashNode hashNode;
        @Child private CallDispatchHeadNode equalNode;
        @Child private BooleanCastNode booleanCastNode;
        @Child protected CallDispatchHeadNode dupNode;
        @Child protected CallDispatchHeadNode freezeNode;
        @Child private IsFrozenNode isFrozenNode;

        public SmallHashLiteralNode(RubyNode[] keyValues) {
            super(keyValues);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {

            final Object[] store = PackedArrayStrategy.createStore(getContext());

            int size = 0;

            initializers: for (int n = 0; n < keyValues.length / 2; n++) {
                Object key = keyValues[n * 2].execute(frame);

                if (stringKeyProfile.profile(RubyGuards.isRubyString(key))) {
                    if (!isFrozen(key)) {
                        key = callFreeze(frame, callDup(frame, key));
                    }
                }

                final int hashed = hash(frame, key);

                final Object value = keyValues[n * 2 + 1].execute(frame);

                for (int i = 0; i < n; i++) {
                    if (i < size &&
                            hashed == PackedArrayStrategy.getHashed(store, i) &&
                            callEqual(frame, key, PackedArrayStrategy.getKey(store, i))) {
                        PackedArrayStrategy.setKey(store, i, key);
                        PackedArrayStrategy.setValue(store, i, value);
                        continue initializers;
                    }
                }

                PackedArrayStrategy.setHashedKeyValue(store, size, hashed, key, value);
                size++;
            }

            return coreLibrary().getHashFactory().newInstance(Layouts.HASH.build(store, size, null, null, nil(), nil(), false));
        }

        private int hash(VirtualFrame frame, Object key) {
            if (hashNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                hashNode = insert(new HashNode());
            }
            return hashNode.hash(frame, key, false);
        }

        private boolean callEqual(VirtualFrame frame, Object receiver, Object key) {
            if (equalNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                equalNode = insert(CallDispatchHeadNode.createOnSelf());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(equalNode.call(frame, receiver, "eql?", key));
        }

        private Object callDup(VirtualFrame frame, Object receiver) {
            if (dupNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                dupNode = insert(CallDispatchHeadNode.createOnSelf());
            }
            return dupNode.call(frame, receiver, "dup");
        }

        private Object callFreeze(VirtualFrame frame, Object receiver) {
            if (freezeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                freezeNode = insert(CallDispatchHeadNode.createOnSelf());
            }
            return freezeNode.call(frame, receiver, "freeze");
        }

        protected boolean isFrozen(Object object) {
            if (isFrozenNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isFrozenNode = insert(IsFrozenNodeGen.create(null));
            }
            return isFrozenNode.executeIsFrozen(object);
        }

    }

    public static class GenericHashLiteralNode extends HashLiteralNode {

        @Child SetNode setNode;

        public GenericHashLiteralNode(RubyNode[] keyValues) {
            super(keyValues);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            if (setNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                setNode = insert(SetNode.create());
            }

            final int bucketsCount = BucketsStrategy.capacityGreaterThan(keyValues.length / 2) * BucketsStrategy.OVERALLOCATE_FACTOR;
            final Entry[] newEntries = new Entry[bucketsCount];

            final DynamicObject hash = coreLibrary().getHashFactory().newInstance(Layouts.HASH.build(newEntries, 0, null, null, nil(), nil(), false));

            for (int n = 0; n < keyValues.length; n += 2) {
                final Object key = keyValues[n].execute(frame);
                final Object value = keyValues[n + 1].execute(frame);
                setNode.executeSet(frame, hash, key, value, false);
            }

            return hash;
        }

    }

}
