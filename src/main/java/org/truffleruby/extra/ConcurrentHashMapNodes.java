package org.truffleruby.extra;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocationTracing;

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
}
