/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.objectspace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.Map.Entry;

/** Note that WeakMap uses identity comparison semantics. See top comment in src/main/ruby/truffleruby/core/weakmap.rb
 * for more information. */
@CoreModule(value = "ObjectSpace::WeakMap", isClass = true)
public abstract class WeakMapNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, Layouts.WEAK_MAP.build(new WeakMapStorage()));
        }
    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends UnaryCoreMethodNode {

        @Specialization
        protected int size(DynamicObject map) {
            return Layouts.WEAK_MAP.getWeakMapStorage(map).size();
        }
    }

    @CoreMethod(names = { "key?", "member?", "include?" }, required = 1)
    public abstract static class MemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isMember(DynamicObject map, Object key) {
            return Layouts.WEAK_MAP.getWeakMapStorage(map).get(key) != null;
        }
    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object get(DynamicObject map, Object key) {
            Object value = Layouts.WEAK_MAP.getWeakMapStorage(map).get(key);
            return value == null ? nil : value;
        }
    }

    @Primitive(name = "weakmap_aset")
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object set(DynamicObject map, Object key, Object value) {
            return Layouts.WEAK_MAP.getWeakMapStorage(map).put(key, value);
        }
    }

    @CoreMethod(names = { "keys" })
    public abstract static class KeysNode extends UnaryCoreMethodNode {

        @Specialization
        protected DynamicObject getKeys(DynamicObject map) {
            return createArray(keys(Layouts.WEAK_MAP.getWeakMapStorage(map)));
        }
    }

    @CoreMethod(names = { "values" })
    public abstract static class ValuesNode extends UnaryCoreMethodNode {

        @Specialization
        protected DynamicObject getValues(DynamicObject map) {
            return createArray(values(Layouts.WEAK_MAP.getWeakMapStorage(map)));
        }
    }

    @CoreMethod(names = { "each_key" }, needsBlock = true)
    public abstract static class EachKeyNode extends YieldingCoreMethodNode {

        @Specialization
        protected DynamicObject eachKey(DynamicObject map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @Specialization
        protected DynamicObject eachKey(DynamicObject map, DynamicObject block) {
            for (Object key : keys(Layouts.WEAK_MAP.getWeakMapStorage(map))) {
                yield(block, key);
            }
            return map;
        }
    }

    @CoreMethod(names = { "each_value" }, needsBlock = true)
    public abstract static class EachValueNode extends YieldingCoreMethodNode {

        @Specialization
        protected DynamicObject eachValue(DynamicObject map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @Specialization
        protected DynamicObject eachValue(DynamicObject map, DynamicObject block) {
            for (Object value : values(Layouts.WEAK_MAP.getWeakMapStorage(map))) {
                yield(block, value);
            }
            return map;
        }
    }

    @CoreMethod(names = { "each", "each_pair" }, needsBlock = true)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        private final ConditionProfile arityMoreThanOne = ConditionProfile.createBinaryProfile();

        @Specialization
        protected DynamicObject each(DynamicObject map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @TruffleBoundary
        @Specialization
        protected DynamicObject each(DynamicObject map, DynamicObject block) {

            for (Entry<Object, Object> e : Layouts.WEAK_MAP.getWeakMapStorage(map).entries()) {
                yieldPair(block, e.getKey(), e.getValue());
            }

            return map;
        }

        private void yieldPair(DynamicObject block, Object key, Object value) {
            // MRI behavior, see rb_hash_each_pair()
            if (arityMoreThanOne.profile(Layouts.PROC.getSharedMethodInfo(block).getArity().getArityNumber() > 1)) {
                yield(block, key, value);
            } else {
                yield(block, createArray(new Object[]{ key, value }, 2));
            }
        }
    }

    @TruffleBoundary
    private static Object[] keys(WeakMapStorage storage) {
        return storage.keys().toArray();
    }

    @TruffleBoundary
    private static Object[] values(WeakMapStorage storage) {
        return storage.values().toArray();
    }

    private static DynamicObject eachNoBlockProvided(YieldingCoreMethodNode node, DynamicObject map) {
        if (Layouts.WEAK_MAP.getWeakMapStorage(map).size() == 0) {
            return map;
        }
        throw new RaiseException(node.getContext(), node.coreExceptions().localJumpError("no block given", node));
    }
}
