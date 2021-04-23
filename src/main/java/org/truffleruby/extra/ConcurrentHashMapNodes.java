/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocationTracing;

import java.util.Map;

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
        @TruffleBoundary
        @Specialization
        protected Object getIndex(RubyConcurrentHashMap self, Object key) {
            final Object value = self.concurrentHash.get(new RubyConcurrentHashMap.Key(key));
            if (value == null) {
                return nil;
            } else {
                return value;
            }
        }
    }

    @CoreMethod(names = "[]=", required = 2)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object setIndex(RubyConcurrentHashMap self, Object key, Object value) {
            self.concurrentHash.put(new RubyConcurrentHashMap.Key(key), value);
            return value;
        }
    }

    @CoreMethod(names = "compute_if_absent", required = 1, needsBlock = true)
    public abstract static class ComputeIfAbsentNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected Object computeIfAbsent(RubyConcurrentHashMap self, Object key, RubyProc block) {
            Object returnValue = self.concurrentHash.computeIfAbsent(new RubyConcurrentHashMap.Key(key), k -> {
                return callBlock(block);
            });

            if (returnValue == null) {
                return nil;
            }
            return returnValue;
        }
    }

    @CoreMethod(names = "compute_if_present", required = 1, needsBlock = true)
    public abstract static class ComputeIfPresentNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected Object computeIfPresent(RubyConcurrentHashMap self, Object key, RubyProc block) {
            Object returnValue = self.concurrentHash.computeIfPresent(new RubyConcurrentHashMap.Key(key), (k, v) -> {
                Object newValue = callBlock(block, v);

                if (newValue == nil) {
                    return null;
                } else {
                    return newValue;
                }
            });

            if (returnValue == null) {
                return nil;
            }
            return returnValue;
        }
    }

    @CoreMethod(names = "compute", required = 1, needsBlock = true)
    public abstract static class ComputeNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected Object compute(RubyConcurrentHashMap self, Object key, RubyProc block) {
            Object returnValue = self.concurrentHash.compute(new RubyConcurrentHashMap.Key(key), (k, v) -> {
                Object oldValue;
                if (v == null) {
                    oldValue = nil;
                } else {
                    oldValue = v;
                }

                Object newValue = callBlock(block, oldValue);

                if (newValue == nil) {
                    return null;
                } else {
                    return newValue;
                }
            });

            if (returnValue == null) {
                return nil;
            }
            return returnValue;
        }
    }

    @CoreMethod(names = "merge_pair", required = 2, needsBlock = true)
    public abstract static class MergePairNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected Object mergePair(RubyConcurrentHashMap self, Object key, Object value, RubyProc block) {
            Object returnValue = self.concurrentHash.merge(new RubyConcurrentHashMap.Key(key), value, (k, v) -> {
                final Object oldValue = self.concurrentHash.get(new RubyConcurrentHashMap.Key(key));
                Object newValue = callBlock(block, oldValue);

                if (newValue == nil) {
                    return null;
                } else {
                    return newValue;
                }
            });

            if (returnValue == null) {
                return nil;
            }
            return returnValue;
        }
    }

    @CoreMethod(names = "replace_pair", required = 3)
    public abstract static class ReplacePairNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected boolean replacePair(RubyConcurrentHashMap self, Object key, Object oldValue, Object newValue) {
            return self.concurrentHash.replace(new RubyConcurrentHashMap.Key(key), oldValue, newValue);
        }
    }

    @CoreMethod(names = "replace_if_exists", required = 2)
    public abstract static class ReplaceIfExistsNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object replaceIfExists(RubyConcurrentHashMap self, Object key, Object newValue) {
            final Object oldValue = self.concurrentHash.replace(new RubyConcurrentHashMap.Key(key), newValue);
            if (oldValue == null) {
                return nil;
            } else {
                return oldValue;
            }
        }
    }

    @CoreMethod(names = "get_and_set", required = 2)
    public abstract static class GetAndSetNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object getAndSet(RubyConcurrentHashMap self, Object key, Object value) {
            final Object oldValue = self.concurrentHash.get(new RubyConcurrentHashMap.Key(key));
            self.concurrentHash.put(new RubyConcurrentHashMap.Key(key), value);
            if (oldValue == null) {
                return nil;
            } else {
                return oldValue;
            }
        }
    }

    @CoreMethod(names = "key?", required = 1)
    public abstract static class KeyNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected boolean key(RubyConcurrentHashMap self, Object key) {
            return self.concurrentHash.containsKey(new RubyConcurrentHashMap.Key(key));
        }
    }

    @CoreMethod(names = "delete", required = 1)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object delete(RubyConcurrentHashMap self, Object key) {
            if (self.concurrentHash.containsKey(new RubyConcurrentHashMap.Key(key))) {
                return self.concurrentHash.remove(new RubyConcurrentHashMap.Key(key));
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "delete_pair", required = 2)
    public abstract static class DeletePairNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected boolean deletePair(RubyConcurrentHashMap self, Object key, Object value) {
            return self.concurrentHash.remove(new RubyConcurrentHashMap.Key(key), value);
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
        @TruffleBoundary
        @Specialization
        protected Object getOrDefault(RubyConcurrentHashMap self, Object key, Object defaultValue) {
            return self.concurrentHash.getOrDefault(new RubyConcurrentHashMap.Key(key), defaultValue);
        }
    }

    @CoreMethod(names = "each_pair", needsBlock = true)
    public abstract static class EachPairNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected Object eachPair(RubyConcurrentHashMap self, RubyProc block) {
            for (Map.Entry<RubyConcurrentHashMap.Key, Object> pair : self.concurrentHash.entrySet()) {
                callBlock(block, pair.getKey().key, pair.getValue());
            }
            return self;
        }
    }
}
