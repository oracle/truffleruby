/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.objectspace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.FinalizationService.FinalizerReference;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

@CoreClass("ObjectSpace")
public abstract class ObjectSpaceNodes {

    @CoreMethod(names = "_id2ref", isModuleFunction = true, required = 1)
    @ImportStatic(ObjectIDOperations.class)
    public abstract static class ID2RefNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "id == NIL")
        public Object id2RefNil(long id) {
            return nil();
        }

        @Specialization(guards = "id == TRUE")
        public boolean id2RefTrue(long id) {
            return true;
        }

        @Specialization(guards = "id == FALSE")
        public boolean id2RefFalse(long id) {
            return false;
        }

        @Specialization(guards = "isSmallFixnumID(id)")
        public long id2RefSmallInt(long id) {
            return ObjectIDOperations.toFixnum(id);
        }

        @TruffleBoundary
        @Specialization(guards = "isBasicObjectID(id)")
        public DynamicObject id2Ref(
                final long id,
                @Cached("createReadObjectIDNode()") ReadObjectFieldNode readObjectIdNode) {
            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                final long objectID = (long) readObjectIdNode.execute(object);
                if (objectID == id) {
                    return object;
                }
            }

            throw new RaiseException(getContext(), coreExceptions().rangeError(StringUtils.format("0x%016x is not id value", id), this));
        }

        @Specialization(guards = { "isRubyBignum(id)", "isLargeFixnumID(id)" })
        public Object id2RefLargeFixnum(DynamicObject id) {
            return Layouts.BIGNUM.getValue(id).longValue();
        }

        @Specialization(guards = { "isRubyBignum(id)", "isFloatID(id)" })
        public double id2RefFloat(DynamicObject id) {
            return Double.longBitsToDouble(Layouts.BIGNUM.getValue(id).longValue());
        }

        protected ReadObjectFieldNode createReadObjectIDNode() {
            return ReadObjectFieldNodeGen.create(Layouts.OBJECT_ID_IDENTIFIER, 0L);
        }

        protected boolean isLargeFixnumID(DynamicObject id) {
            return ObjectIDOperations.isLargeFixnumID(Layouts.BIGNUM.getValue(id));
        }

        protected boolean isFloatID(DynamicObject id) {
            return ObjectIDOperations.isFloatID(Layouts.BIGNUM.getValue(id));
        }

    }

    @CoreMethod(names = "each_object", isModuleFunction = true, needsBlock = true, optional = 1, returnsEnumeratorIfNoBlock = true)
    public abstract static class EachObjectNode extends YieldingCoreMethodNode {

        @TruffleBoundary // for the iterator
        @Specialization
        public int eachObject(NotProvided ofClass, DynamicObject block) {
            int count = 0;

            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                if (!isHidden(object)) {
                    yield(block, object);
                    count++;
                }
            }

            return count;
        }

        @TruffleBoundary // for the iterator
        @Specialization(guards = "isRubyModule(ofClass)")
        public int eachObject(DynamicObject ofClass, DynamicObject block,
                @Cached("create()") IsANode isANode) {
            int count = 0;

            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                if (!isHidden(object) && isANode.executeIsA(object, ofClass)) {
                    yield(block, object);
                    count++;
                }
            }

            return count;
        }

        private boolean isHidden(DynamicObject object) {
            return !RubyGuards.isRubyBasicObject(object) || RubyGuards.isSingletonClass(object);
        }

    }

    @CoreMethod(names = "define_finalizer", isModuleFunction = true, required = 2)
    public abstract static class DefineFinalizerNode extends CoreMethodArrayArgumentsNode {

        // MRI would do a dynamic call to #respond_to? but it seems better to warn the user earlier.
        // Wanting #method_missing(:call) to be called for a finalizer seems highly unlikely.
        @Child private DoesRespondDispatchHeadNode respondToCallNode = DoesRespondDispatchHeadNode.create();
        @Child private ReadObjectFieldNode getFinaliserNode = ReadObjectFieldNodeGen.create("finalizerRef", null);
        @Child private WriteObjectFieldNode setFinalizerNode = WriteObjectFieldNodeGen.create("finalizerRef");

        @Specialization
        public DynamicObject defineFinalizer(VirtualFrame frame, DynamicObject object, Object finalizer,
                @Cached("create()") BranchProfile errorProfile) {
            if (respondToCallNode.doesRespondTo(frame, "call", finalizer)) {
                synchronized (getContext().getFinalizationService()) {
                    FinalizerReference ref = (FinalizerReference) getFinaliserNode.execute(object);
                    FinalizerReference newRef = getContext().getObjectSpaceManager().defineFinalizer(object, ref, finalizer);
                    if (ref != newRef) {
                        setFinalizerNode.write(object, newRef);
                    }
                }
                Object[] objects = new Object[] { 0, finalizer };
                return createArray(objects, objects.length);
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorWrongArgumentType(finalizer, "callable", this));
            }
        }

    }

    @CoreMethod(names = "undefine_finalizer", isModuleFunction = true, required = 1)
    public abstract static class UndefineFinalizerNode extends CoreMethodArrayArgumentsNode {
        @Child private ReadObjectFieldNode getFinaliserNode = ReadObjectFieldNodeGen.create("finalizerRef", null);
        @Child private WriteObjectFieldNode setFinalizerNode = WriteObjectFieldNodeGen.create("finalizerRef");

        @Specialization
        public Object undefineFinalizer(VirtualFrame frame, DynamicObject object) {
            FinalizerReference ref = (FinalizerReference) getFinaliserNode.execute(object);
            if (ref != null) {
                FinalizerReference newRef = getContext().getObjectSpaceManager().undefineFinalizer(object, ref);
                if (ref != newRef) {
                    setFinalizerNode.write(object, newRef);
                }
            }
            return object;
        }
    }

}
