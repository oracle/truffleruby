/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.ReferenceEqualNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodes.ToSNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.FreezeNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.HiddenKey;

/** All nodes in this class should be Primitive for efficiency (avoiding an extra call and constant lookup) */
@CoreModule("Truffle::Type")
public abstract class TypeNodes {

    @Primitive(name = "is_a?")
    public abstract static class IsAPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean isA(Object object, RubyModule module,
                @Cached IsANode isANode) {
            return isANode.executeIsA(object, module);
        }
    }

    @Primitive(name = "respond_to?")
    public abstract static class RespondToPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean respondTo(Object object, Object name, boolean includePrivate,
                @Cached KernelNodes.RespondToNode respondToNode) {
            // Do not pass a frame here, we want to ignore refinements and not need to read the caller frame
            return respondToNode.executeDoesRespondTo(object, name, includePrivate);
        }
    }

    @Primitive(name = "class")
    public abstract static class ClassPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyClass objectClass(Object object,
                @Cached LogicalClassNode logicalClassNode) {
            return logicalClassNode.execute(object);
        }
    }

    @Primitive(name = "metaclass")
    public abstract static class MetaClassPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyClass metaClass(Object object,
                @Cached MetaClassNode metaClassNode) {
            return metaClassNode.execute(this, object);
        }
    }

    @Primitive(name = "singleton_class")
    public abstract static class SingletonClassPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyClass singletonClass(Object object,
                @Cached SingletonClassNode singletonClassNode) {
            return singletonClassNode.execute(object);
        }
    }

    @Primitive(name = "equal?")
    public abstract static class EqualPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean equal(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode.execute(a, b);
        }
    }

    // GR-44289: Using only dispatched libraries in this node as a workaround for that issue
    public abstract static class ObjectFreezeNode extends RubyBaseNode {

        public abstract Object execute(Object self);

        @Specialization(guards = "!isRubyDynamicObject(self)")
        protected Object freeze(Object self,
                @Cached @Exclusive FreezeNode freezeNode) {
            freezeNode.execute(this, self);
            return self;
        }

        @Specialization(guards = "!metaClass.isSingleton", limit = "1")
        protected Object freezeNormalObject(RubyDynamicObject self,
                @Cached @Shared FreezeNode freezeNode,
                @Cached @Shared MetaClassNode metaClassNode,
                @Bind("metaClassNode.execute(this, self)") RubyClass metaClass) {
            freezeNode.execute(this, self);
            return self;
        }

        @Specialization(guards = "metaClass.isSingleton", limit = "1")
        protected static Object freezeSingletonObject(RubyDynamicObject self,
                @Cached @Shared FreezeNode freezeNode,
                @Cached @Exclusive FreezeNode freezeMetaClasNode,
                @Cached IsFrozenNode isFrozenMetaClassNode,
                @Cached InlinedConditionProfile singletonClassUnfrozenProfile,
                @Cached @Shared MetaClassNode metaClassNode,
                @Bind("metaClassNode.execute(this, self)") RubyClass metaClass,
                @Bind("this") Node node) {
            if (singletonClassUnfrozenProfile.profile(node,
                    !RubyGuards.isSingletonClass(self) && !isFrozenMetaClassNode.execute(metaClass))) {
                freezeMetaClasNode.execute(node, metaClass);
            }
            freezeNode.execute(node, self);
            return self;
        }
    }

    @Primitive(name = "freeze")
    public abstract static class FreezePrimitive extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object freeze(Object self,
                @Cached ObjectFreezeNode objectFreezeNode) {
            return objectFreezeNode.execute(self);
        }
    }

    @Primitive(name = "immediate_value?")
    public abstract static class IsImmediateValueNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean doBoolean(boolean value) {
            return true;
        }

        @Specialization
        protected boolean doInt(int value) {
            return true;
        }

        @Specialization
        protected boolean doLong(long value) {
            return true;
        }

        @Specialization
        protected boolean doFloat(double value) {
            return true;
        }

        @Specialization
        protected boolean doSymbol(RubySymbol value) {
            return true;
        }

        @Specialization
        protected boolean doNil(Nil value) {
            return true;
        }

        @Fallback
        protected boolean doFallback(Object value) {
            return false;
        }

    }

    @Primitive(name = "nil?")
    public abstract static class IsNilNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean isNil(Object value) {
            return value == nil;
        }
    }

    @Primitive(name = "boolean_or_nil?")
    public abstract static class IsBooleanOrNilNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean bool(boolean value) {
            return true;
        }

        @Specialization
        protected boolean nil(Nil value) {
            return true;
        }

        @Fallback
        protected boolean other(Object value) {
            return false;
        }
    }

    @Primitive(name = "true?")
    public abstract static class IsTrue extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean bool(boolean value) {
            return value;
        }

        @Fallback
        protected boolean other(Object value) {
            return false;
        }
    }

    @Primitive(name = "false?")
    public abstract static class IsFalse extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean bool(boolean value) {
            return !value;
        }

        @Fallback
        protected boolean other(Object value) {
            return false;
        }
    }

    @Primitive(name = "object_ivars")
    public abstract static class ObjectInstanceVariablesNode extends PrimitiveArrayArgumentsNode {

        public abstract RubyArray executeGetIVars(Object self);

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected static RubyArray instanceVariables(RubyDynamicObject object,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary,
                @Cached InlinedConditionProfile noPropertiesProfile,
                @Bind("this") Node node) {
            var shape = objectLibrary.getShape(object);

            if (noPropertiesProfile.profile(node, shape.getPropertyCount() == 0)) {
                return createEmptyArray(node);
            }

            return createIVarNameArray(node, objectLibrary.getKeyArray(object));
        }

        @Specialization(guards = "!isRubyDynamicObject(object)")
        protected RubyArray instanceVariablesNotDynamic(Object object) {
            return createEmptyArray();
        }

        @TruffleBoundary
        private static RubyArray createIVarNameArray(Node node, Object[] keys) {
            final List<String> names = new ArrayList<>(keys.length);

            for (Object name : keys) {
                if (name instanceof String) {
                    names.add((String) name);
                }
            }

            final int size = names.size();
            final Object[] nameSymbols = new Object[size];
            for (int i = 0; i < size; i++) {
                nameSymbols[i] = getSymbol(node, names.get(i));
            }

            return createArray(node, nameSymbols);
        }

    }

    @Primitive(name = "object_ivar_defined?")
    public abstract static class ObjectIVarIsDefinedNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected boolean ivarIsDefined(RubyDynamicObject object, RubySymbol name,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.containsKey(object, name.getString());
        }

        @Fallback
        protected boolean ivarIsDefinedNonDynamic(Object object, Object name) {
            return false;
        }

    }

    @Primitive(name = "object_ivar_get")
    public abstract static class ObjectIVarGetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object ivarGet(RubyDynamicObject object, RubySymbol name,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.getOrDefault(object, name.getString(), nil);
        }
    }

    @Primitive(name = "object_ivar_set")
    public abstract static class ObjectIVarSetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object ivarSet(RubyDynamicObject object, RubySymbol name, Object value,
                @Cached WriteObjectFieldNode writeNode) {
            writeNode.execute(object, name.getString(), value);
            return value;
        }
    }

    // Those primitives store the key as either a HiddenKey or a Ruby Symbol, so they have
    // a different namespace than normal ivars which use java.lang.String.

    @Primitive(name = "object_hidden_var_create")
    public abstract static class ObjectHiddenVarCreateNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected HiddenKey createHiddenVar(RubySymbol identifier) {
            return new HiddenKey(identifier.getString());
        }
    }

    @Primitive(name = "object_hidden_var_defined?")
    public abstract static class ObjectHiddenVarDefinedNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected boolean objectHiddenVarDefined(RubyDynamicObject object, Object identifier,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.containsKey(object, identifier);
        }

        @Fallback
        protected boolean immutable(Object object, Object identifier) {
            return false;
        }
    }

    @Primitive(name = "object_hidden_var_get")
    public abstract static class ObjectHiddenVarGetNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object objectHiddenVarGet(RubyDynamicObject object, Object identifier,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.getOrDefault(object, identifier, nil);
        }

        @Fallback
        protected Object immutable(Object object, Object identifier) {
            return nil;
        }
    }

    @Primitive(name = "object_hidden_var_set")
    public abstract static class ObjectHiddenVarSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object objectHiddenVarSet(RubyDynamicObject object, Object identifier, Object value,
                @Cached WriteObjectFieldNode writeNode) {
            writeNode.execute(object, identifier, value);
            return value;
        }
    }

    @Primitive(name = "rb_any_to_s")
    public abstract static class ObjectToSNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyString toS(Object obj,
                @Cached ToSNode kernelToSNode) {
            return kernelToSNode.execute(obj);
        }
    }

    @Primitive(name = "module_name")
    public abstract static class ModuleNameNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object moduleName(RubyModule module) {
            return module.fields.getRubyStringName();
        }

    }

    @Primitive(name = "rb_num2long")
    public abstract static class RbNum2LongPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Child private ToLongNode toLongNode = ToLongNode.create();

        @Specialization
        protected long numToLong(Object value) {
            return toLongNode.execute(value);
        }
    }

    @Primitive(name = "rb_num2int")
    public abstract static class RbNum2IntPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Child private ToIntNode toIntNode = ToIntNode.create();

        @Specialization
        protected int numToInt(Object value) {
            return toIntNode.execute(value);
        }
    }

    @Primitive(name = "rb_to_int")
    public abstract static class RbToIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object toRubyInteger(Object value,
                @Cached ToRubyIntegerNode toRubyInteger) {
            return toRubyInteger.execute(this, value);
        }
    }

    @Primitive(name = "check_frozen")
    public abstract static class TypeCheckFrozenNode extends PrimitiveArrayArgumentsNode {

        @NeverDefault
        public static TypeCheckFrozenNode create(RubyNode rubyNode) {
            return TypeNodesFactory.TypeCheckFrozenNodeFactory.create(new RubyNode[]{ rubyNode });
        }

        @Specialization
        protected Object check(Object value,
                @Cached CheckFrozenNode checkFrozenNode) {
            checkFrozenNode.execute(value);

            return value;
        }
    }

    @GenerateUncached
    public abstract static class CheckFrozenNode extends RubyBaseNode {

        @NeverDefault
        public static CheckFrozenNode create() {
            return TypeNodesFactory.CheckFrozenNodeGen.create();
        }

        public abstract void execute(Object object);

        @Specialization
        protected void check(Object value,
                @Cached IsFrozenNode isFrozenNode,
                @Cached InlinedBranchProfile errorProfile) {

            if (isFrozenNode.execute(value)) {
                errorProfile.enter(this);
                throw new RaiseException(getContext(), coreExceptions().frozenError(value, this));
            }
        }
    }


    @Primitive(name = "check_mutable_string")
    public abstract static class CheckMutableStringNode extends PrimitiveArrayArgumentsNode {

        public static CheckMutableStringNode create() {
            return create(null);
        }

        public static CheckMutableStringNode create(RubyNode node) {
            return TypeNodesFactory.CheckMutableStringNodeFactory.create(new RubyNode[]{ node });
        }

        public abstract void execute(Object object);


        @Specialization
        protected Object check(RubyString value,
                @Cached InlinedBranchProfile errorProfile) {
            if (value.locked) {
                errorProfile.enter(this);
                throw new RaiseException(getContext(),
                        coreExceptions().runtimeError("can't modify string; temporarily locked", this));
            } else if (value.frozen) {
                errorProfile.enter(this);
                throw new RaiseException(getContext(), coreExceptions().frozenError(value, this));
            }
            return value;
        }

        @Specialization
        protected Object checkImmutable(ImmutableRubyString value) {
            throw new RaiseException(getContext(), coreExceptions().frozenError(value, this));
        }

    }

    @Primitive(name = "check_real?")
    public abstract static class CheckRealNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean check(int value) {
            return true;
        }

        @Specialization
        protected boolean check(long value) {
            return true;
        }

        @Specialization
        protected boolean check(double value) {
            return true;
        }

        @Fallback
        protected static boolean other(Object value,
                @Cached IsANode isANode,
                @Cached InlinedConditionProfile numericProfile,
                @Cached DispatchNode isRealNode,
                @Cached BooleanCastNode booleanCastNode,
                @Bind("this") Node node) {
            return numericProfile.profile(node, isANode.executeIsA(value, coreLibrary(node).numericClass)) &&
                    booleanCastNode.execute(isRealNode.call(value, "real?"));
        }
    }

    @Primitive(name = "undefined?")
    public abstract static class IsUndefinedNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isUndefined(Object value) {
            return value == NotProvided.INSTANCE;
        }
    }

    @Primitive(name = "as_boolean")
    public abstract static class AsBooleanNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean asBoolean(Object value,
                @Cached BooleanCastNode booleanCastNode) {
            return booleanCastNode.execute(value);
        }
    }

}
