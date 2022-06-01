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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.SharedIndicesMap;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.debug.SingleElementArray;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.object.Shape;

import static org.truffleruby.language.RubyBaseNode.nil;

@ExportLibrary(InteropLibrary.class)
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

    /** Maps method names to indices in the methods array. Shared per language instance. */
    public SharedIndicesMap methodNamesToIndex;

    /** Array of methods. Different for each context. */
    public InternalMethod[] methodVTable;

    /** Subclasses of this class. These need to be updated when methods are added or removed. */
    public final Set<RubyClass> includedBy = Collections
            .newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    @TruffleBoundary
    public RubyClass(
            RubyClass classClass,
            RubyLanguage language,
            SourceSection sourceSection,
            RubyModule lexicalParent,
            String givenBaseName,
            boolean isSingleton,
            RubyDynamicObject attached,
            Object superclass,
            SharedIndicesMap methodNamesToIndex) {
        super(classClass, language.classShape, language, sourceSection, lexicalParent, givenBaseName);
        assert !isSingleton || givenBaseName == null;
        this.isSingleton = isSingleton;
        this.attached = attached;

        this.methodNamesToIndex = methodNamesToIndex;
        if (superclass instanceof RubyClass) {
            if (lexicalParent == null && givenBaseName != null) {
                fields.setFullName(givenBaseName);
            }
            // superclass should be set after "full name"
            this.superclass = superclass;
            this.ancestorClasses = computeAncestorClasses((RubyClass) superclass);
            this.depth = ((RubyClass) superclass).depth + 1;
            fields.setSuperClass((RubyClass) superclass);
            updateMethodVTable((RubyClass) superclass);
        } else { // BasicObject (nil superclass)
            assert superclass == nil;
            this.methodVTable = new InternalMethod[0];
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

        RubyClass basicObjectClass = ClassNodes.createBootClass(language, this, nil, "BasicObject",
                language.basicObjectMethodNamesMap);
        RubyClass objectClass = ClassNodes.createBootClass(language, this, basicObjectClass, "Object",
                language.objectMethodNamesMap);
        RubyClass moduleClass = ClassNodes.createBootClass(language, this, objectClass, "Module",
                language.moduleMethodNamesMap);

        RubyClass superclass = moduleClass;
        this.superclass = superclass;
        this.ancestorClasses = computeAncestorClasses(superclass);
        this.depth = superclass.depth + 1;
        fields.setSuperClass(superclass);
        this.methodVTable = new InternalMethod[0];
        this.methodNamesToIndex = new SharedIndicesMap();
        updateMethodVTable(superclass);
    }

    @TruffleBoundary
    public static void copyVTable(RubyClass selfMetaClass, RubyClass fromMetaClass) {
        selfMetaClass.methodNamesToIndex = fromMetaClass.methodNamesToIndex.getCopy();
        selfMetaClass.methodVTable = Arrays.copyOf(fromMetaClass.methodVTable,
                fromMetaClass.methodVTable.length);
        for (String name : fromMetaClass.fields.getMethodNames()) {
            final int index = selfMetaClass.methodNamesToIndex.lookup(name);
            final InternalMethod method = selfMetaClass.methodVTable[index];
            selfMetaClass.methodVTable[index] = method.withDeclaringModule(selfMetaClass).withOwner(selfMetaClass);
        }
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

    private void updateMethodVTable(RubyClass superclass) {
        // Merge existing and superclass methods by attempting a lookup
        List<Map.Entry<String, Integer>> names = new ArrayList<>(
                superclass.methodNamesToIndex.nameToIndex.entrySet());
        names.sort(Map.Entry.comparingByValue());
        for (Map.Entry<String, Integer> entry : names) {
            this.methodNamesToIndex.lookup(entry.getKey());
        }

        this.methodVTable = new InternalMethod[methodNamesToIndex.size()];
        for (Map.Entry<String, Integer> entry : this.methodNamesToIndex.nameToIndex.entrySet()) {
            this.methodVTable[entry.getValue()] = ModuleOperations.lookupMethodUncached(this, entry.getKey());
        }
        synchronized (superclass) {
            superclass.includedBy.add(this);
        }
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        super.getAdjacentObjects(reachable);
        ObjectGraph.addProperty(reachable, attached);
        ObjectGraph.addProperty(reachable, superclass);
    }

    // region MetaObject
    @ExportMessage
    public boolean hasMetaParents() {
        return true;
    }

    @ExportMessage
    public Object getMetaParents() {
        return new SingleElementArray(superclass);
    }
    // endregion

}
