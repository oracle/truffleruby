/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocationTracing;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

@CoreModule(value = "TruffleRuby::ConcurrentHashMap", isClass = true)
public class ConcurrentHashMapNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyConcurrentHashMap allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().concurrentHashMapShape;
            final RubyConcurrentHashMap instance = new RubyConcurrentHashMap(rubyClass, shape, null);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyConcurrentHashMap initializeNoValue(RubyConcurrentHashMap self, NotProvided options) {
            return self;
        }

        @Specialization
        protected RubyConcurrentHashMap initializeNil(RubyConcurrentHashMap self, Nil options) {
            return self;
        }

        @Specialization
        protected RubyConcurrentHashMap initializeWithOptions(RubyConcurrentHashMap self, RubyHash options) {
            return self;
        }

        @Specialization(guards = { "!isNil(options)", "wasProvided(options)" })
        protected RubyConcurrentHashMap initializeWithOptions(RubyConcurrentHashMap self, RubyBasicObject options) {
            return self;
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyConcurrentHashMap initializeCopy(RubyConcurrentHashMap self, RubyConcurrentHashMap other) {
            self.concurrentHash.putAll(self.concurrentHash);
            return self;
        }
    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getIndex(RubyConcurrentHashMap self, Object key,
                                  @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(get(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode)));
        }

        @TruffleBoundary
        private Object get(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                           RubyConcurrentHashMap.Key key) {
            return hashMap.get(key);
        }
    }

    @CoreMethod(names = "[]=", required = 2)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object setIndex(RubyConcurrentHashMap self, Object key, Object value,
                                  @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            put(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), value);
            return value;
        }

        @TruffleBoundary
        private void put(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                         RubyConcurrentHashMap.Key key, Object value) {
            hashMap.put(key, value);
        }
    }

    @CoreMethod(names = "compute_if_absent", required = 1, needsBlock = true)
    public abstract static class ComputeIfAbsentNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object computeIfAbsent(RubyConcurrentHashMap self, Object key, RubyProc block,
                                         @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(computeIfAbsent(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode),
                    (k) -> callBlock(block)));
        }

        @TruffleBoundary
        private Object computeIfAbsent(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                                       RubyConcurrentHashMap.Key key,
                                       Function<RubyConcurrentHashMap.Key, Object> remappingFunction) {
            return hashMap.computeIfAbsent(key, remappingFunction);
        }
    }

    @CoreMethod(names = "compute_if_present", required = 1, needsBlock = true)
    public abstract static class ComputeIfPresentNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object computeIfPresent(RubyConcurrentHashMap self, Object key, RubyProc block,
                                          @Cached HashingNodes.ToHashByHashCode hashNode) {
            return nullToNil(computeIfPresent(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashNode.execute(key)), (k, v) ->
               // To do: It's unfortunate we're calling this behind a boundary! Can we do better?
               nilToNull(callBlock(block, v))
            ));
        }

        @TruffleBoundary
        private Object computeIfPresent(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                                        RubyConcurrentHashMap.Key key,
                                        BiFunction<RubyConcurrentHashMap.Key, Object, Object> remappingFunction) {
            return hashMap.computeIfPresent(key, remappingFunction);
        }
    }

    @CoreMethod(names = "compute", required = 1, needsBlock = true)
    public abstract static class ComputeNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object compute(RubyConcurrentHashMap self, Object key, RubyProc block,
                                 @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(compute(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode),
                    (k, v) -> {
                Object newValue = callBlock(block, nullToNil(v));
                return nilToNull(newValue);
            }));
        }

        @TruffleBoundary
        private Object compute(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                               RubyConcurrentHashMap.Key key,
                               BiFunction<RubyConcurrentHashMap.Key, Object, Object> remappingFunction) {
            return hashMap.compute(key, remappingFunction);
        }
    }

    @CoreMethod(names = "merge_pair", required = 2, needsBlock = true)
    public abstract static class MergePairNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object mergePair(RubyConcurrentHashMap self, Object key, Object value, RubyProc block,
                                   @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(merge(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), value, (k, v) -> {
                final Object oldValue = get(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode));
                return nilToNull(callBlock(block, oldValue));
            }));
        }

        @TruffleBoundary
        private Object merge(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                             RubyConcurrentHashMap.Key key,
                             Object value,
                             BiFunction<Object, Object, Object> remappingFunction) {
            return hashMap.merge(key, value, remappingFunction);
        }

        @TruffleBoundary
        private Object get(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                           RubyConcurrentHashMap.Key key) {
            return hashMap.get(key);
        }
    }

    @CoreMethod(names = "replace_pair", required = 3)
    public abstract static class ReplacePairNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean replacePair(RubyConcurrentHashMap self, Object key, Object oldValue, Object newValue,
                                      @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return replace(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), oldValue, newValue);
        }

        @TruffleBoundary
        private boolean replace(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                             RubyConcurrentHashMap.Key key, Object oldValue, Object newValue) {
            return hashMap.replace(key, oldValue, newValue);
        }
    }

    @CoreMethod(names = "replace_if_exists", required = 2)
    public abstract static class ReplaceIfExistsNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object replaceIfExists(RubyConcurrentHashMap self, Object key, Object newValue,
                                         @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(replace(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), newValue));
        }

        @TruffleBoundary
        private Object replace(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                               RubyConcurrentHashMap.Key key, Object newValue) {
            return hashMap.replace(key, newValue);
        }
    }

@CoreMethod(names = "get_and_set", required = 2)
    public abstract static class GetAndSetNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getAndSet(RubyConcurrentHashMap self, Object key, Object value,
                                   @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            final Object oldValue = get(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode));
            put(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), value);
            return nullToNil(oldValue);
        }

        @TruffleBoundary
        private Object get(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                           RubyConcurrentHashMap.Key key) {
            return hashMap.get(key);
        }

        @TruffleBoundary
        private Object put(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                           RubyConcurrentHashMap.Key key, Object value) {
            return hashMap.put(key, value);
        }
    }

    @CoreMethod(names = "key?", required = 1)
    public abstract static class KeyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean key(RubyConcurrentHashMap self, Object key,
                              @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return containsKey(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode));
        }

        @TruffleBoundary
        private boolean containsKey(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                                   RubyConcurrentHashMap.Key key) {
            return hashMap.containsKey(key);
        }
    }

    @CoreMethod(names = "delete", required = 1)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object delete(RubyConcurrentHashMap self, Object key,
                                @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return nullToNil(remove(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode)));
        }

        @TruffleBoundary
        private Object remove(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                               RubyConcurrentHashMap.Key key) {
            return hashMap.remove(key);
        }
    }

    @CoreMethod(names = "delete_pair", required = 2)
    public abstract static class DeletePairNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean deletePair(RubyConcurrentHashMap self, Object key, Object value,
                                     @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return remove(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), value);
        }

        @TruffleBoundary
        private boolean remove(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                              RubyConcurrentHashMap.Key key, Object value) {
            return hashMap.remove(key, value);
        }
    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected RubyConcurrentHashMap clear(RubyConcurrentHashMap self) {
            self.concurrentHash.clear();
            return self;
        }
    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected int size(RubyConcurrentHashMap self) {
            return self.concurrentHash.size();
        }
    }

    @CoreMethod(names = "get_or_default", required = 2)
    public abstract static class GetOrDefaultNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getOrDefault(RubyConcurrentHashMap self, Object key, Object defaultValue,
                                      @Cached HashingNodes.ToHashByHashCode hashNode) {
            final int hashCode = hashNode.execute(key);
            return getOrDefault(self.concurrentHash, new RubyConcurrentHashMap.Key(key, hashCode), defaultValue);
        }

        @TruffleBoundary
        private Object getOrDefault(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap,
                                    RubyConcurrentHashMap.Key key, Object defaultValue) {
            return hashMap.getOrDefault(key, defaultValue);
        }
    }

    @CoreMethod(names = "each_pair", needsBlock = true)
    public abstract static class EachPairNode extends YieldingCoreMethodNode {

        @Specialization
        protected Object eachPair(RubyConcurrentHashMap self, RubyProc block) {
            for (Map.Entry<RubyConcurrentHashMap.Key, Object> pair : entrySet(self.concurrentHash)) {
                callBlock(block, pair.getKey().key, pair.getValue());
            }
            return self;
        }

        @TruffleBoundary
        private Set<Map.Entry<RubyConcurrentHashMap.Key, Object>> entrySet(ConcurrentHashMap<RubyConcurrentHashMap.Key, Object> hashMap) {
            return hashMap.entrySet();
        }
    }
}
