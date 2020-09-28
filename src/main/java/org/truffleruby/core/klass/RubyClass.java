/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.klass;

import java.util.Set;

import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(InteropLibrary.class)
public final class RubyClass extends RubyModule implements ObjectGraphNode {

    public final boolean isSingleton;
    /** If this is an object's metaclass, then nonSingletonClass is the logical class of the object. */
    public final RubyClass nonSingletonClass;
    public final RubyDynamicObject attached;
    public Shape instanceShape = null;
    /* a RubyClass, or nil for BasicObject, or null when not yet initialized */
    public Object superclass;

    public RubyClass(
            RubyClass classClass,
            RubyContext context,
            SourceSection sourceSection,
            RubyModule lexicalParent,
            String givenBaseName,
            boolean isSingleton,
            RubyDynamicObject attached,
            Object superclass) {
        super(classClass, classClass.instanceShape, context, sourceSection, lexicalParent, givenBaseName);
        this.isSingleton = isSingleton;
        this.attached = attached;
        this.superclass = superclass;

        this.nonSingletonClass = computeNonSingletonClass(isSingleton, superclass);
    }

    /** Special constructor to build the 'Class' RubyClass itself */
    RubyClass(RubyContext context, Shape tempShape) {
        super(context, tempShape, "constructor only for the class Class");
        this.isSingleton = false;
        this.attached = null;
        this.superclass = null;

        this.nonSingletonClass = this;
    }

    private RubyClass computeNonSingletonClass(boolean isSingleton, Object superclassObject) {
        if (isSingleton) {
            RubyClass superclass = ((RubyClass) superclassObject);
            if (superclass.isSingleton) {
                return superclass.nonSingletonClass;
            } else {
                return superclass;
            }
        } else {
            return this;
        }
    }

    public boolean isInitialized() {
        return superclass != null;
    }

    public void setSuperClass(RubyClass superclass) {
        assert this.superclass == null || this.superclass == superclass;
        this.superclass = superclass;
        fields.setSuperClass(superclass);
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        super.getAdjacentObjects(reachable);
        ObjectGraph.addProperty(reachable, attached);
        ObjectGraph.addProperty(reachable, superclass);
    }

    // region MetaObject
    @ExportMessage
    public boolean isMetaObject() {
        return true;
    }

    @ExportMessage
    public String getMetaQualifiedName() {
        return fields.getName();
    }

    @ExportMessage
    public String getMetaSimpleName() {
        return fields.getSimpleName();
    }

    @ExportMessage
    public boolean isMetaInstance(Object instance,
            @Cached IsANode isANode) {
        return isANode.executeIsA(instance, this);
    }
    // endregion

}
