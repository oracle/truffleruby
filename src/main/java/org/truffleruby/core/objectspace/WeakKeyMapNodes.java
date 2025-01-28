/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.objectspace;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.hash.CompareByRubyHashEqlWrapper;
import org.truffleruby.core.hash.CompareByRubyHashEqlWrapper.RecordingCompareByRubyHashEqlWrapper;
import org.truffleruby.core.hash.HashingNodes.ToHashByHashCode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.annotations.Visibility;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.yield.CallBlockNode;

import java.util.Objects;

@CoreModule(value = "ObjectSpace::WeakKeyMap", isClass = true)
public abstract class WeakKeyMapNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        RubyWeakKeyMap allocate(RubyClass rubyClass) {
            final RubyWeakKeyMap weakKeyMap = new RubyWeakKeyMap(rubyClass, getLanguage().weakKeyMapShape);
            AllocationTracing.trace(weakKeyMap, this);
            return weakKeyMap;
        }
    }

    @Primitive(name = "weakkeymap_size")
    public abstract static class SizeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        int size(RubyWeakKeyMap map) {
            return map.storage.size();
        }
    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        Object get(RubyWeakKeyMap map, Object key,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            Object value = map.storage.get(new CompareByRubyHashEqlWrapper(key, hashCode));
            return value == null ? nil : value;
        }
    }

    @CoreMethod(names = "getkey", required = 1)
    public abstract static class GetKeyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        Object getKey(RubyWeakKeyMap map, Object key,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            var recordingWrapper = new RecordingCompareByRubyHashEqlWrapper(key, hashCode);
            if (map.storage.containsKey(recordingWrapper)) {
                return Objects.requireNonNull(recordingWrapper.keyInMap);
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "key?", required = 1)
    public abstract static class MemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        boolean containsKey(RubyWeakKeyMap map, Object key,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            return map.storage.containsKey(new CompareByRubyHashEqlWrapper(key, hashCode));
        }
    }

    @CoreMethod(names = "[]=", required = 2)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        Object set(RubyWeakKeyMap map, Object key, Object value,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            map.storage.put(new CompareByRubyHashEqlWrapper(key, hashCode), value);
            return value;
        }
    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        RubyWeakKeyMap clear(RubyWeakKeyMap map) {
            map.storage.clear();
            return map;
        }
    }

    @CoreMethod(names = "delete", required = 1, needsBlock = true)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        Object delete(RubyWeakKeyMap map, Object key, RubyProc block,
                @Cached InlinedConditionProfile isContainedProfile,
                @Cached CallBlockNode yieldNode,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            Object value = map.storage.remove(new CompareByRubyHashEqlWrapper(key, hashCode));

            if (isContainedProfile.profile(this, value != null)) {
                return value;
            } else {
                return yieldNode.yield(this, block, key);
            }
        }

        @Specialization
        Object delete(RubyWeakKeyMap map, Object key, Nil block,
                @Cached InlinedConditionProfile isContainedProfile,
                @Cached ToHashByHashCode hashCodeNode) {
            int hashCode = hashCodeNode.execute(this, key);
            Object value = map.storage.remove(new CompareByRubyHashEqlWrapper(key, hashCode));

            if (isContainedProfile.profile(this, value != null)) {
                return value;
            } else {
                return nil;
            }
        }
    }
}
