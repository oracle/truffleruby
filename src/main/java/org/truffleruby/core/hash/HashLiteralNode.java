/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;

public abstract class HashLiteralNode extends RubyContextSourceNode {

    @Children protected final RubyNode[] keyValues;
    protected final RubyLanguage language;

    protected HashLiteralNode(RubyLanguage language, RubyNode[] keyValues) {
        assert keyValues.length % 2 == 0;
        this.language = language;
        this.keyValues = keyValues;
    }

    public static HashLiteralNode create(RubyLanguage language, RubyNode[] keyValues) {
        if (keyValues.length == 0) {
            return new EmptyHashLiteralNode(language);
        } else if (keyValues.length <= language.options.HASH_PACKED_ARRAY_MAX * 2) {
            return new SmallHashLiteralNode(language, keyValues);
        } else {
            return new GenericHashLiteralNode(language, keyValues);
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

        public EmptyHashLiteralNode(RubyLanguage language) {
            super(language, RubyNode.EMPTY_ARRAY);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return HashOperations.newEmptyHash(getContext());
        }

    }

    public static class SmallHashLiteralNode extends HashLiteralNode {

        @Child private HashingNodes.ToHashByHashCode hashNode;
        @Child private DispatchNode equalNode;
        @Child private BooleanCastNode booleanCastNode;
        @Child private FreezeHashKeyIfNeededNode freezeHashKeyIfNeededNode = FreezeHashKeyIfNeededNodeGen.create();
        private final BranchProfile duplicateKeyProfile = BranchProfile.create();

        public SmallHashLiteralNode(RubyLanguage language, RubyNode[] keyValues) {
            super(language, keyValues);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] store = PackedArrayStrategy.createStore(language);

            int size = 0;

            for (int n = 0; n < keyValues.length / 2; n++) {
                Object key = keyValues[n * 2].execute(frame);
                key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(key, false);

                final int hashed = hash(key);

                final Object value = keyValues[n * 2 + 1].execute(frame);
                boolean duplicateKey = false;

                for (int i = 0; i < n; i++) {
                    if (i < size &&
                            hashed == PackedArrayStrategy.getHashed(store, i) &&
                            callEqual(key, PackedArrayStrategy.getKey(store, i))) {
                        duplicateKeyProfile.enter();
                        PackedArrayStrategy.setKey(store, i, key);
                        PackedArrayStrategy.setValue(store, i, value);
                        duplicateKey = true;
                        break;
                    }
                }

                if (!duplicateKey) {
                    PackedArrayStrategy.setHashedKeyValue(store, size, hashed, key, value);
                    size++;
                }
            }

            return new RubyHash(
                    coreLibrary().hashClass,
                    RubyLanguage.hashShape,
                    getContext(),
                    store,
                    size,
                    null,
                    null,
                    nil,
                    nil,
                    false);
        }

        private int hash(Object key) {
            if (hashNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                hashNode = insert(HashingNodes.ToHashByHashCode.create());
            }
            return hashNode.execute(key);
        }

        private boolean callEqual(Object receiver, Object key) {
            if (equalNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                equalNode = insert(DispatchNode.create());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(equalNode.call(receiver, "eql?", key));
        }

    }

    public static class GenericHashLiteralNode extends HashLiteralNode {

        @Child SetNode setNode;
        private final int bucketsCount;

        public GenericHashLiteralNode(RubyLanguage language, RubyNode[] keyValues) {
            super(language, keyValues);
            bucketsCount = BucketsStrategy.capacityGreaterThan(keyValues.length / 2) *
                    BucketsStrategy.OVERALLOCATE_FACTOR;
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            if (setNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                setNode = insert(SetNode.create());
            }

            final Entry[] newEntries = new Entry[bucketsCount];
            final RubyHash hash = new RubyHash(
                    coreLibrary().hashClass,
                    RubyLanguage.hashShape,
                    getContext(),
                    newEntries,
                    0,
                    null,
                    null,
                    nil,
                    nil,
                    false);

            for (int n = 0; n < keyValues.length; n += 2) {
                final Object key = keyValues[n].execute(frame);
                final Object value = keyValues[n + 1].execute(frame);
                setNode.executeSet(hash, key, value, false);
            }

            return hash;
        }
    }
}
