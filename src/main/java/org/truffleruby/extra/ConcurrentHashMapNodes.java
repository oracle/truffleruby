package org.truffleruby.extra;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.inlined.InlinedDispatchNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.yield.CallBlockNode;

@CoreModule(value = "TruffleRuby::ConcurrentHashMap", isClass = true)
public class ConcurrentHashMapNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyConcurrentHashMap allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().concurrentHashMapShape;
            final RubyHash hash = HashOperations.newEmptyHash(getContext(), getLanguage());
            final RubyConcurrentHashMap instance = new RubyConcurrentHashMap(rubyClass, shape, null, hash);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyConcurrentHashMap initializeNoValue(RubyConcurrentHashMap self, NotProvided options) {
            self.hash = HashOperations.newEmptyHash(getContext(), getLanguage());
            return self;
        }

        @Specialization
        protected RubyConcurrentHashMap initializeNil(RubyConcurrentHashMap self, Nil options) {
            self.hash = HashOperations.newEmptyHash(getContext(), getLanguage());
            return self;
        }

        @Specialization(guards = { "!isNil(options)", "wasProvided(options)" })
        protected RubyConcurrentHashMap initializeWithOptions(RubyConcurrentHashMap self, RubyBasicObject options) {
            self.hash = HashOperations.newEmptyHash(getContext(), getLanguage());
            return self;
        }
    }

    @CoreMethod(names = "get_internal_hash")
    public abstract static class GetInternalHashNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyHash getInternalHash(RubyConcurrentHashMap self) {
            return self.hash;
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyConcurrentHashMap initializeCopy(RubyConcurrentHashMap self, RubyConcurrentHashMap other) {
            DispatchNode.getUncached().call((Object) self.hash, "replace", other.hash);
            return self;
        }
    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getIndex(RubyConcurrentHashMap self, Object key) {
            return DispatchNode.getUncached().call((Object) self.hash, "[]", key);
        }
    }

    @CoreMethod(names = "[]=", required = 2)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object setIndex(RubyConcurrentHashMap self, Object key, Object value) {
            synchronized (self) {
                return DispatchNode.getUncached().call((Object) self.hash, "[]=", key, value);
            }
        }
    }

    @CoreMethod(names = "compute_if_absent", required = 1, needsBlock = true)
    public abstract static class ComputeIfAbsentNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object computeIfAbsent(RubyConcurrentHashMap self, Object key, RubyProc block) {
            synchronized (self) {
                if ((boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key)) {
                    return DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                } else {
                    Object newValue = callBlock(block);
                    return DispatchNode.getUncached().call((Object) self.hash, "[]=", key, newValue);
                }
            }
        }
    }

    @CoreMethod(names = "compute_if_present", required = 1, needsBlock = true)
    public abstract static class ComputeIfPresentNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object computeIfPresent(RubyConcurrentHashMap self, Object key, RubyProc block) {
            synchronized (self) {
                if ((boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key)) {
                    Object oldValue = DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                    Object newValue = callBlock(block, oldValue);
                    return DispatchNode.getUncached().call((Object) self, "store_computed_value", key, newValue);
                }
                return Nil.INSTANCE;
            }
        }
    }

    @CoreMethod(names = "compute", required = 1, needsBlock = true)
    public abstract static class ComputeNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object compute(RubyConcurrentHashMap self, Object key, RubyProc block) {
            synchronized (self) {
                Object oldValue = DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                Object newValue = callBlock(block, oldValue);
                return DispatchNode.getUncached().call((Object) self, "store_computed_value", key, newValue);
            }
        }
    }

    @CoreMethod(names = "store_computed_value", required = 2, visibility = Visibility.PRIVATE)
    public abstract static class StoreComputedValueNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected static Object storeComputedValue(RubyConcurrentHashMap self, Object key, Object newValue) {
            if (newValue == Nil.INSTANCE) {
                DispatchNode.getUncached().call((Object) self.hash, "delete", key);
                return Nil.INSTANCE;
            } else {
                DispatchNode.getUncached().call((Object) self.hash, "[]=", key, newValue);
                return newValue;
            }
        }
    }

    @CoreMethod(names = "merge_pair", required = 2, needsBlock = true)
    public abstract static class MergePairNode extends YieldingCoreMethodNode {
        @Specialization
        protected Object mergePair(RubyConcurrentHashMap self, Object key, Object value, RubyProc block) {
            synchronized (self) {
                if ((boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key)) {
                    Object oldValue = DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                    Object newValue = callBlock(block, oldValue);

                    Object computedValue = DispatchNode.getUncached().call((Object) self, "store_computed_value", key, newValue);
                    return computedValue;
                } else {
                    DispatchNode.getUncached().call((Object) self.hash, "[]=", key, value);
                    return value;
                }
            }
        }
    }

    @CoreMethod(names = "replace_pair", required = 3)
    public abstract static class ReplacePairNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean replacePair(RubyConcurrentHashMap self, Object key, Object oldValue, Object newValue) {
            synchronized (self) {
                if (((boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key))
                    && (DispatchNode.getUncached().call((Object) self.hash, "[]", key) == oldValue)) {
                    DispatchNode.getUncached().call((Object) self.hash, "[]=", key, newValue);
                    return true;
                }
                return false;
            }
        }
    }

    @CoreMethod(names = "replace_if_exists", required = 2)
    public abstract static class ReplaceIfExistsNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object replaceIfExists(RubyConcurrentHashMap self, Object key, Object newValue) {
            synchronized (self) {
                if ((boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key)) {
                    Object old_value = DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                    DispatchNode.getUncached().call((Object) self.hash, "[]=", key, newValue);
                    return old_value;
                }
                return Nil.INSTANCE;
            }
        }
    }

    @CoreMethod(names = "get_and_set", required = 2)
    public abstract static class GetAndSetNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getAndSet(RubyConcurrentHashMap self, Object key, Object value) {
            synchronized (self) {
                Object old_value = DispatchNode.getUncached().call((Object) self.hash, "[]", key);
                DispatchNode.getUncached().call((Object) self.hash, "[]=", key, value);
                return old_value;
            }
        }
    }

    @CoreMethod(names = "key?", required = 1)
    public abstract static class KeyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean key(RubyConcurrentHashMap self, Object key) {
            return (boolean) DispatchNode.getUncached().call((Object) self.hash, "key?", key);
        }
    }

    @CoreMethod(names = "delete", required = 1)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object delete(RubyConcurrentHashMap self, Object key) {
            synchronized (self) {
                return DispatchNode.getUncached().call((Object) self.hash, "delete", key);
            }
        }
    }

    @CoreMethod(names = "delete_pair", required = 2)
    public abstract static class DeletePairNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean deletePair(RubyConcurrentHashMap self, Object key, Object value) {
            synchronized (self) {
                if ((boolean) (DispatchNode.getUncached().call((Object) self.hash, "key?", key)) &&
                        (DispatchNode.getUncached().call((Object) self.hash, "[]", key) == value)) {
                    DispatchNode.getUncached().call((Object) self.hash, "delete", key);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyConcurrentHashMap clear(RubyConcurrentHashMap self) {
            synchronized (self) {
                DispatchNode.getUncached().call((Object) self.hash, "clear");
                return self;
            }
        }
    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected int size(RubyConcurrentHashMap self) {
            return self.hash.size;
        }
    }

    @CoreMethod(names = "get_or_default", required = 2)
    public abstract static class GetOrDefaultNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getOrDefault(RubyConcurrentHashMap self, Object key, Object defaultValue) {
            return DispatchNode.getUncached().call((Object) self.hash, "fetch", key, defaultValue);
        }
    }
}
