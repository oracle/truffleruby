/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** Get a Ruby object's singleton class */
// Specializations are order by their frequency on railsbench using --engine.SpecializationStatistics
@GenerateUncached
public abstract class SingletonClassNode extends RubyBaseNode {

    public static SingletonClassNode getUncached() {
        return SingletonClassNodeGen.getUncached();
    }

    public abstract RubyClass execute(Object value);

    @Specialization(
            // no need to guard on the context, the rubyClass is context-specific
            guards = { "isSingleContext()", "rubyClass == cachedClass", "cachedSingletonClass != null" },
            limit = "1")
    static RubyClass singletonClassClassCached(RubyClass rubyClass,
            @Cached("rubyClass") RubyClass cachedClass,
            @Cached IsFrozenNode isFrozenNode,
            @Cached FreezeNode freezeNode,
            @Bind("this") Node node,
            @Cached("getSingletonClassOfClassOrNull(getContext(node), node, cachedClass, isFrozenNode, freezeNode)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(replaces = "singletonClassClassCached")
    RubyClass singletonClassClassUncached(RubyClass rubyClass) {
        final RubyClass result = ClassNodes.getSingletonClassOfClass(getContext(), rubyClass);

        if (IsFrozenNodeGen.getUncached().execute(rubyClass)) {
            FreezeNode.executeUncached(result);
        }
        return result;
    }

    @Specialization(
            // no need to guard on the context, the RubyDynamicObject is context-specific
            guards = {
                    "isSingleContext()",
                    "object == cachedObject",
                    "!isRubyClass(cachedObject)",
                    "!isRubyIO(cachedObject)" },
            limit = "1")
    RubyClass singletonClassInstanceCached(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("getSingletonClassForInstance(getContext(), object)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(guards = "!isRubyClass(object)", replaces = "singletonClassInstanceCached")
    RubyClass singletonClassInstanceUncached(RubyDynamicObject object) {
        return getSingletonClassForInstance(getContext(), object);
    }

    @Specialization(guards = "value")
    RubyClass singletonClassTrue(boolean value) {
        return coreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    RubyClass singletonClassFalse(boolean value) {
        return coreLibrary().falseClass;
    }

    @Specialization
    RubyClass singletonClassNil(Nil value) {
        return coreLibrary().nilClass;
    }

    @Specialization
    RubyClass singletonClass(int value) {
        return noSingletonClass();
    }

    @Specialization
    RubyClass singletonClass(long value) {
        return noSingletonClass();
    }

    @Specialization
    RubyClass singletonClass(double value) {
        return noSingletonClass();
    }

    @Specialization(guards = "!isNil(value)")
    RubyClass singletonClassImmutableObject(ImmutableRubyObject value) {
        return noSingletonClass();
    }

    private RubyClass noSingletonClass() {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantDefineSingleton(this));
    }

    protected RubyClass getSingletonClassOfClassOrNull(RubyContext context, Node node, RubyClass rubyClass,
            IsFrozenNode isFrozenNode,
            FreezeNode freezeNode) {
        final RubyClass result = ClassNodes.getSingletonClassOfClassOrNull(context, rubyClass);

        if (result != null && isFrozenNode.execute(rubyClass)) {
            freezeNode.execute(node, result);
        }

        return result;
    }

    @TruffleBoundary
    protected RubyClass getSingletonClassForInstance(RubyContext context, RubyDynamicObject object) {
        synchronized (object) {
            RubyClass metaClass = object.getMetaClass();
            if (metaClass.isSingleton) {
                return metaClass;
            }

            final RubyClass logicalClass = object.getLogicalClass();

            final RubyClass singletonClass = ClassNodes.createSingletonClassOfObject(
                    context,
                    getEncapsulatingSourceSection(),
                    logicalClass,
                    object);

            if (IsFrozenNodeGen.getUncached().execute(object)) {
                FreezeNode.executeUncached(singletonClass);
            }

            SharedObjects.propagate(context.getLanguageSlow(), object, singletonClass);
            object.setMetaClass(singletonClass);

            return singletonClass;
        }
    }

    @NodeChild(value = "valueNode", type = RubyNode.class)
    public abstract static class SingletonClassASTNode extends RubyContextSourceNode {

        @Specialization
        Object singletonClass(Object value,
                @Cached SingletonClassNode singletonClassNode) {
            return singletonClassNode.execute(value);
        }

        abstract RubyNode getValueNode();

        @Override
        public RubyNode cloneUninitialized() {
            return SingletonClassNodeGen.SingletonClassASTNodeGen.create(getValueNode().cloneUninitialized())
                    .copyFlags(this);
        }
    }

}
