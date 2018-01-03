package org.truffleruby.core.objectspace;

import java.util.ArrayList;
import java.util.Set;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ObjectGraph;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreClass("Truffle::ObjectSpaceOperations")
public abstract class TruffleObjectSpaceNodes {

    @CoreMethod(names = "all_objects", isModuleFunction = true, required = 1)
    public abstract static class EachObjectNode extends YieldingCoreMethodNode {

        @TruffleBoundary // for the iterator
        @Specialization(guards = "isNil(ofClass)")
        public DynamicObject eachObject(DynamicObject ofClass) {
            Set<DynamicObject> objects = ObjectGraph.stopAndGetAllObjects(this, getContext());
            ArrayList<DynamicObject> objectList = new ArrayList<>(objects.size());

            for (DynamicObject object : objects) {
                if (!isHidden(object)) {
                    objectList.add(object);
                }
            }

            return createArray(objectList.toArray(), objectList.size());
        }

        @TruffleBoundary // for the iterator
        @Specialization(guards = "isRubyModule(ofClass)")
        public DynamicObject eachObjectOfClass(DynamicObject ofClass,
                @Cached("create()") IsANode isANode) {
            Set<DynamicObject> objects = ObjectGraph.stopAndGetAllObjects(this, getContext());
            ArrayList<DynamicObject> objectList = new ArrayList<>(objects.size());

            for (DynamicObject object : objects) {
                if (!isHidden(object) && isANode.executeIsA(object, ofClass)) {
                    objectList.add(object);
                }
            }

            return createArray(objectList.toArray(), objectList.size());
        }

        private boolean isHidden(DynamicObject object) {
            return !RubyGuards.isRubyBasicObject(object) || RubyGuards.isSingletonClass(object);
        }

    }

}
