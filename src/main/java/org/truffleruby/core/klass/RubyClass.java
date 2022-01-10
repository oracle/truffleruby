/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.klass;

import java.util.Arrays;
import java.util.Set;

import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyLanguage;
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

    private static final RubyClass[] EMPTY_CLASS_ARRAY = new RubyClass[0];

    public final boolean isSingleton;
    /** If this is an object's metaclass, then nonSingletonClass is the logical class of the object. */
    public final RubyClass nonSingletonClass;
    public final RubyDynamicObject attached;
    /* a RubyClass, or nil for BasicObject, or null when not yet initialized */
    public Object superclass;
    public RubyClass[] ancestorClasses;

    /** Depth from BasicObject (= 0) in the inheritance hierarchy. */
    public int depth;

    public RubyClass(
            RubyClass classClass,
            RubyLanguage language,
            SourceSection sourceSection,
            RubyModule lexicalParent,
            String givenBaseName,
            boolean isSingleton,
            RubyDynamicObject attached,
            Object superclass) {
        super(classClass, language.classShape, language, sourceSection, lexicalParent, givenBaseName);
        assert !isSingleton || givenBaseName == null;
        this.isSingleton = isSingleton;
        this.attached = attached;

        if (superclass instanceof RubyClass) {
            updateSuperclass((RubyClass) superclass);
        } else { // BasicObject (nil superclass) or uninitialized class (null)
            this.depth = 0;
            this.superclass = superclass;
            this.ancestorClasses = EMPTY_CLASS_ARRAY;
        }

        this.nonSingletonClass = computeNonSingletonClass(isSingleton, superclass);
    }


    /** Special constructor to build the 'Class' RubyClass itself. The superclass is set later. */
    RubyClass(RubyLanguage language, Shape classShape) {
        super(language, classShape, "constructor only for the class Class");
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
        updateSuperclass(superclass);
        fields.setSuperClass(superclass);
    }

    private void updateSuperclass(RubyClass superclass) {
        final RubyClass[] superAncestors = superclass.ancestorClasses;
        final RubyClass[] ancestors = Arrays.copyOf(superAncestors, superAncestors.length + 1);
        ancestors[superAncestors.length] = superclass;
        this.superclass = superclass;
        this.depth = superclass.depth + 1;
        this.ancestorClasses = ancestors;
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
