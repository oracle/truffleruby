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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.object.Shape;

public final class RubyClass extends RubyModule implements ObjectGraphNode {

    private static final RubyClass[] EMPTY_CLASS_ARRAY = new RubyClass[0];

    public final boolean isSingleton;
    /** If this is an object's metaclass, then nonSingletonClass is the logical class of the object. */
    public final RubyClass nonSingletonClass;
    public final RubyDynamicObject attached;
    /* a RubyClass or nil for BasicObject */
    public final Object superclass;
    public final RubyClass[] ancestorClasses;
    /** Depth from BasicObject (= 0) in the inheritance hierarchy. */
    public final int depth;

    @TruffleBoundary
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
            if (lexicalParent == null && givenBaseName != null) {
                fields.setFullName(givenBaseName);
            }
            // superclass should be set after "full name"
            this.superclass = superclass;
            this.ancestorClasses = computeAncestorClasses((RubyClass) superclass);
            this.depth = ((RubyClass) superclass).depth + 1;
            fields.setSuperClass((RubyClass) superclass);
        } else { // BasicObject (nil superclass)
            assert Nil.is(superclass);
            this.superclass = superclass;
            this.ancestorClasses = EMPTY_CLASS_ARRAY;
            this.depth = 0;
        }

        this.nonSingletonClass = computeNonSingletonClass(isSingleton, superclass);
    }


    /** Special constructor to build the 'Class' RubyClass itself. */
    RubyClass(RubyLanguage language, Shape classShape) {
        super(language, classShape, "constructor only for the class Class");
        this.isSingleton = false;
        this.attached = null;
        this.nonSingletonClass = this;

        RubyClass basicObjectClass = ClassNodes.createBootClass(language, this, Nil.get(), "BasicObject");
        RubyClass objectClass = ClassNodes.createBootClass(language, this, basicObjectClass, "Object");
        RubyClass moduleClass = ClassNodes.createBootClass(language, this, objectClass, "Module");

        RubyClass superclass = moduleClass;
        this.superclass = superclass;
        this.ancestorClasses = computeAncestorClasses(superclass);
        this.depth = superclass.depth + 1;
        fields.setSuperClass(superclass);
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

    private RubyClass[] computeAncestorClasses(RubyClass superclass) {
        final RubyClass[] superAncestors = superclass.ancestorClasses;
        final RubyClass[] ancestors = Arrays.copyOf(superAncestors, superAncestors.length + 1);
        ancestors[superAncestors.length] = superclass;
        return ancestors;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        super.getAdjacentObjects(reachable);
        ObjectGraph.addProperty(reachable, attached);
        ObjectGraph.addProperty(reachable, superclass);
    }

}
