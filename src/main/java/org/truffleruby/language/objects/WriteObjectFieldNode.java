/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.FinalLocationException;
import com.oracle.truffle.api.object.IncompatibleLocationException;
import com.oracle.truffle.api.object.Location;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.utilities.NeverValidAssumption;

@ImportStatic({ RubyGuards.class, ShapeCachingGuards.class })
@ReportPolymorphism
@GenerateUncached
public abstract class WriteObjectFieldNode extends RubyBaseWithoutContextNode {

    public static WriteObjectFieldNode create() {
        return WriteObjectFieldNodeGen.create();
    }

    public void write(DynamicObject object, Object name, Object value) {
        executeWithGeneralize(object, name, value, false);
    }

    public abstract void executeWithGeneralize(DynamicObject object, Object name, Object value, boolean generalize);

    @TruffleBoundary
    private void executeBoundary(DynamicObject object, Object name, Object value, boolean generalize) {
        executeWithGeneralize(object, name, value, generalize);
    }

    @Specialization(guards = { "location != null", "object.getShape() == cachedShape", "name == cachedName" }, assumptions = { "cachedShape.getValidAssumption()",
            "validLocation" }, limit = "getCacheLimit()")
    public void writeExistingField(DynamicObject object, Object name, Object value, boolean generalize,
            @Cached("name") Object cachedName,
            @Cached("getLocation(object, cachedName, value)") Location location,
            @Cached("object.getShape()") Shape cachedShape,
            @Cached("createAssumption(cachedShape, location)") Assumption validLocation,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("isShared(context, cachedShape)") boolean shared,
            @Cached("createWriteBarrierNode(shared)") WriteBarrierNode writeBarrierNode,
            @Cached("createProfile(shared)") BranchProfile shapeRaceProfile) {
        try {
            if (shared) {
                writeBarrierNode.executeWriteBarrier(value);
                /*
                 * We need a STORE_STORE memory barrier here, to ensure the value is seen as shared by all threads
                 * when published below by writing the value to a field of the object.
                 * Otherwise, the compiler could theoretically move the write barrier
                 * inside the synchronized block, and then the compiler or hardware could potentially
                 * reorder the writes so that publication would happen before sharing.
                 */
                Pointer.UNSAFE.storeFence();
                synchronized (object) {
                    // Re-check the shape under the monitor as another thread might have changed it
                    // by adding a field (fine) or upgrading an existing field to Object storage
                    // (need to use the new storage)
                    if (object.getShape() != cachedShape) {
                        shapeRaceProfile.enter();
                        executeBoundary(object, cachedName, value, generalize);
                        return;
                    }
                    location.set(object, value, cachedShape);
                }
            } else {
                location.set(object, value, cachedShape);
            }
        } catch (IncompatibleLocationException | FinalLocationException e) {
            // remove this entry
            validLocation.invalidate("for " + location + " for existing ivar " + cachedName + " at " + getEncapsulatingSourceSection());
            // Generalization is handled by Shape#defineProperty as the field already exists
            executeWithGeneralize(object, cachedName, value, generalize);
        }
    }

    @Specialization(guards = { "location == null", "object.getShape() == cachedOldShape", "name == cachedName" }, assumptions = { "cachedOldShape.getValidAssumption()",
            "cachedNewShape.getValidAssumption()", "validLocation" }, limit = "getCacheLimit()")
    public void writeNewField(DynamicObject object, Object name, Object value, boolean generalize,
            @Cached("name") Object cachedName,
            @Cached("getLocation(object, cachedName, value)") Location location,
            @Cached("object.getShape()") Shape cachedOldShape,
            @Cached("defineProperty(cachedOldShape, cachedName, value, generalize)") Shape cachedNewShape,
            @Cached("getNewLocation(cachedName, cachedNewShape)") Location newLocation,
            @Cached("createAssumption(cachedOldShape, cachedNewShape, newLocation)") Assumption validLocation,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("isShared(context, cachedOldShape)") boolean shared,
            @Cached("createWriteBarrierNode(shared)") WriteBarrierNode writeBarrierNode,
            @Cached("createProfile(shared)") BranchProfile shapeRaceProfile) {
        try {
            if (shared) {
                writeBarrierNode.executeWriteBarrier(value);
                synchronized (object) {
                    // Re-check the shape under the monitor as another thread might have changed it
                    // by adding a field or upgrading an existing field to Object storage
                    // (we need to make sure to have the right shape to add the new field)
                    if (object.getShape() != cachedOldShape) {
                        shapeRaceProfile.enter();
                        executeBoundary(object, cachedName, value, generalize);
                        return;
                    }
                    newLocation.set(object, value, cachedOldShape, cachedNewShape);
                }
            } else {
                newLocation.set(object, value, cachedOldShape, cachedNewShape);
            }
        } catch (IncompatibleLocationException e) {
            // remove this entry
            validLocation.invalidate("for " + location + " for new ivar " + cachedName + " at " + getEncapsulatingSourceSection());
            // Make sure to generalize when adding a new field and the value is incompatible.
            // So writing an int and then later a double generalizes to adding an Object field.
            executeWithGeneralize(object, cachedName, value, true);
        }
    }

    @Specialization(guards = "updateShape(object)")
    public void updateShapeAndWrite(DynamicObject object, Object name, Object value, boolean generalize) {
        executeWithGeneralize(object, name, value, generalize);
    }

    @TruffleBoundary
    @Specialization(replaces = { "writeExistingField", "writeNewField", "updateShapeAndWrite" })
    public void writeUncached(DynamicObject object, Object name, Object value, boolean generalize,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        final boolean shared = SharedObjects.isShared(context, object);
        if (shared) {
            SharedObjects.writeBarrier(context, value);
            synchronized (object) {
                Shape shape = object.getShape();
                Shape newShape = defineProperty(shape, name, value, false);
                newShape.getProperty(name).setSafe(object, value, shape, newShape);
            }
        } else {
            object.define(name, value);
        }
    }

    protected Location getLocation(DynamicObject object, Object name, Object value) {
        final Shape oldShape = object.getShape();
        final Property property = oldShape.getProperty(name);

        if (PropertyFlags.isDefined(property) && property.getLocation().canSet(object, value)) {
            return property.getLocation();
        } else {
            return null;
        }
    }

    private static final Object SOME_OBJECT = new Object();

    protected Shape defineProperty(Shape oldShape, Object name, Object value, boolean generalize) {
        if (generalize) {
            value = SOME_OBJECT;
        }
        Property property = oldShape.getProperty(name);
        if (property != null && PropertyFlags.isRemoved(property)) {
            // Do not reuse location of removed properties
            Location location = oldShape.allocator().locationForValue(value);
            return oldShape.replaceProperty(property, property.relocate(location).copyWithFlags(0));
        } else {
            return oldShape.defineProperty(name, value, 0);
        }
    }

    protected Location getNewLocation(Object name, Shape newShape) {
        return newShape.getProperty(name).getLocation();
    }

    // The location is passed here even though it's not used,
    // to make sure the DSL checks this Assumption after all lookups are done in executeAndSpecialize().
    protected Assumption createAssumption(Shape shape, Location location) {
        if (!shape.isValid()) {
            return NeverValidAssumption.INSTANCE;
        }
        return Truffle.getRuntime().createAssumption("object location is valid");
    }

    // The location is passed here even though it's not used,
    // to make sure the DSL checks this Assumption after all lookups are done in executeAndSpecialize().
    protected Assumption createAssumption(Shape oldShape, Shape newShape, Location location) {
        if (!oldShape.isValid() || !newShape.isValid()) {
            return NeverValidAssumption.INSTANCE;
        }
        return Truffle.getRuntime().createAssumption("object location is valid");
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

    protected boolean isShared(RubyContext context, Shape shape) {
        return SharedObjects.isShared(context, shape);
    }

    protected WriteBarrierNode createWriteBarrierNode(boolean shared) {
        if (shared) {
            return WriteBarrierNode.create();
        } else {
            return null;
        }
    }

    protected BranchProfile createProfile(boolean shared) {
        if (shared) {
            return BranchProfile.create();
        } else {
            return null;
        }
    }
}
