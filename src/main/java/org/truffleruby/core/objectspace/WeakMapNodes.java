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

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

/** Note that WeakMap uses identity comparison semantics. See top comment in src/main/ruby/truffleruby/core/weakmap.rb
 * for more information. */
@CoreModule(value = "ObjectSpace::WeakMap", isClass = true)
public abstract class WeakMapNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateHelperNode allocate = AllocateHelperNode.create();

        @Specialization
        protected RubyWeakMap allocate(RubyClass rubyClass,
                @CachedLanguage RubyLanguage language) {
            final Shape shape = allocate.getCachedShape(rubyClass);
            final RubyWeakMap weakMap = new RubyWeakMap(rubyClass, shape, new WeakMapStorage());
            allocate.trace(weakMap, this, language);
            return weakMap;
        }
    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends UnaryCoreMethodNode {

        @Specialization
        protected int size(RubyWeakMap map) {
            return map.storage.size();
        }
    }

    @CoreMethod(names = { "key?", "member?", "include?" }, required = 1)
    public abstract static class MemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isMember(RubyWeakMap map, Object key) {
            return map.storage.get(key) != null;
        }
    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object get(RubyWeakMap map, Object key) {
            Object value = map.storage.get(key);
            return value == null ? nil : value;
        }
    }

    @Primitive(name = "weakmap_aset")
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object set(RubyWeakMap map, Object key, Object value) {
            map.storage.put(key, value);
            return value;
        }
    }

    @CoreMethod(names = { "keys" })
    public abstract static class KeysNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyArray getKeys(RubyWeakMap map) {
            return createArray(keys(map.storage));
        }
    }

    @CoreMethod(names = { "values" })
    public abstract static class ValuesNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyArray getValues(RubyWeakMap map) {
            return createArray(values(map.storage));
        }
    }

    @CoreMethod(names = { "each_key" }, needsBlock = true)
    public abstract static class EachKeyNode extends YieldingCoreMethodNode {

        @Specialization
        protected RubyWeakMap eachKey(RubyWeakMap map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @Specialization
        protected RubyWeakMap eachKey(RubyWeakMap map, RubyProc block) {
            for (Object key : keys(map.storage)) {
                yield(block, key);
            }
            return map;
        }
    }

    @CoreMethod(names = { "each_value" }, needsBlock = true)
    public abstract static class EachValueNode extends YieldingCoreMethodNode {

        @Specialization
        protected RubyWeakMap eachValue(RubyWeakMap map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @Specialization
        protected RubyWeakMap eachValue(RubyWeakMap map, RubyProc block) {
            for (Object value : values(map.storage)) {
                yield(block, value);
            }
            return map;
        }
    }

    @CoreMethod(names = { "each", "each_pair" }, needsBlock = true)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        @Specialization
        protected RubyWeakMap each(RubyWeakMap map, NotProvided block) {
            return eachNoBlockProvided(this, map);
        }

        @Specialization
        protected RubyWeakMap each(RubyWeakMap map, RubyProc block) {

            for (WeakValueCache.WeakMapEntry<?, ?> e : entries(map.storage)) {
                yield(block, e.getKey(), e.getValue());
            }

            return map;
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

    @TruffleBoundary
    private static WeakValueCache.WeakMapEntry<?, ?>[] entries(WeakMapStorage storage) {
        return storage.entries().toArray(new WeakValueCache.WeakMapEntry<?, ?>[0]);
    }

    private static RubyWeakMap eachNoBlockProvided(YieldingCoreMethodNode node, RubyWeakMap map) {
        if (map.storage.size() == 0) {
            return map;
        }
        throw new RaiseException(node.getContext(), node.coreExceptions().localJumpError("no block given", node));
    }
}
