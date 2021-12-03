/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodes.ToSNode;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule("Truffle::Type")
public abstract class TypeNodes {

    @Primitive(name = "object_kind_of?")
    public abstract static class ObjectKindOfNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean objectKindOf(Object object, RubyModule module,
                @Cached IsANode isANode) {
            return isANode.executeIsA(object, module);
        }
    }

    @Primitive(name = "object_respond_to?")
    public abstract static class ObjectRespondToNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.RespondToNode respondToNode = KernelNodesFactory.RespondToNodeFactory
                .create(null, null, null);

        @Specialization
        protected boolean objectRespondTo(Object object, Object name, boolean includePrivate) {
            // Do not pass a frame here, we want to ignore refinements and not need to read the caller frame
            return respondToNode.executeDoesRespondTo(null, object, name, includePrivate);
        }

    }

    @CoreMethod(names = "object_class", onSingleton = true, required = 1)
    public abstract static class ObjectClassNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyClass objectClass(Object object,
                @Cached LogicalClassNode logicalClassNode) {
            return logicalClassNode.execute(object);
        }
    }

    @Primitive(name = "class_of")
    public abstract static class ClassOfNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyClass classOf(Object object,
                @Cached MetaClassNode metaClassNode) {
            return metaClassNode.execute(object);
        }
    }

    @Primitive(name = "object_equal")
    public abstract static class ObjectEqualNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean objectEqual(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode.executeReferenceEqual(a, b);
        }
    }

    @Primitive(name = "object_freeze")
    public abstract static class ObjectFreezeNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getRubyLibraryCacheLimit()")
        protected Object freeze(Object self,
                @CachedLibrary("self") RubyLibrary rubyLibrary) {
            assert !(self instanceof RubyDynamicObject && ((RubyDynamicObject) self)
                    .getMetaClass().isSingleton) : "Primitive.object_freeze does not handle instances of singleton classes, see KernelFreezeNode";
            rubyLibrary.freeze(self);
            return self;
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

    @Primitive(name = "object_ivars")
    public abstract static class ObjectInstanceVariablesNode extends PrimitiveArrayArgumentsNode {

        public abstract RubyArray executeGetIVars(Object self);

        @TruffleBoundary
        @Specialization
        protected RubyArray instanceVariables(RubyDynamicObject object) {
            final List<String> names = new ArrayList<>(object.getShape().getPropertyCount());

            for (Object name : DynamicObjectLibrary.getUncached().getKeyArray(object)) {
                if (name instanceof String) {
                    names.add((String) name);
                }
            }

            final int size = names.size();
            final Object[] nameSymbols = new Object[size];
            for (int i = 0; i < size; i++) {
                nameSymbols[i] = getSymbol(names.get(i));
            }

            return createArray(nameSymbols);
        }

        @Specialization(guards = "!isRubyDynamicObject(object)")
        protected RubyArray instanceVariablesNotDynamic(Object object) {
            return createEmptyArray();
        }

    }

    @Primitive(name = "object_ivar_defined?")
    public abstract static class ObjectIVarIsDefinedNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected boolean ivarIsDefined(RubyDynamicObject object, RubySymbol name,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.containsKey(object, name.getString());
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

    @Primitive(name = "object_can_contain_object")
    @ImportStatic(ArrayGuards.class)
    public abstract static class CanContainObjectNode extends PrimitiveArrayArgumentsNode {

        public static CanContainObjectNode create() {
            return TypeNodesFactory.CanContainObjectNodeFactory.create(null);
        }

        public abstract boolean execute(RubyArray array);

        @Specialization(
                guards = {
                        "stores.accepts(array.store)",
                        "stores.isPrimitive(array.store)" })
        protected boolean primitiveArray(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return false;
        }

        @Specialization(
                guards = {
                        "stores.accepts(array.store)",
                        "!stores.isPrimitive(array.store)" })
        protected boolean objectArray(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return true;
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected boolean other(Object array) {
            return true;
        }

    }

    @CoreMethod(names = "rb_any_to_s", onSingleton = true, required = 1)
    public abstract static class ObjectToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString toS(Object obj,
                @Cached ToSNode kernelToSNode) {
            return kernelToSNode.executeToS(obj);
        }

    }

    @CoreMethod(names = "module_name", onSingleton = true, required = 1)
    public abstract static class ModuleNameNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString moduleName(RubyModule module) {
            final String name = module.fields.getName();
            return makeStringNode.executeMake(name, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
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
        protected long numToInt(Object value) {
            return toIntNode.execute(value);
        }
    }

    @Primitive(name = "rb_to_int")
    public abstract static class RbToIntNode extends PrimitiveArrayArgumentsNode {
        @Child private ToRubyIntegerNode toRubyInteger = ToRubyIntegerNode.create();

        @Specialization
        protected Object toRubyInteger(Object value) {
            return toRubyInteger.execute(value);
        }
    }

    @Primitive(name = "check_frozen")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class CheckFrozenNode extends PrimitiveNode {

        public static CheckFrozenNode create() {
            return create(null);
        }

        public static CheckFrozenNode create(RubyNode node) {
            return TypeNodesFactory.CheckFrozenNodeFactory.create(node);
        }

        public abstract void execute(Object object);

        @Specialization(limit = "getRubyLibraryCacheLimit()")
        protected Object check(Object value,
                @CachedLibrary("value") RubyLibrary rubyLibrary,
                @Cached BranchProfile errorProfile) {

            if (rubyLibrary.isFrozen(value)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().frozenError(value, this));
            }

            return value;
        }
    }

    @Primitive(name = "check_mutable_string")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class CheckMutableStringNode extends PrimitiveNode {

        public static CheckMutableStringNode create() {
            return create(null);
        }

        public static CheckMutableStringNode create(RubyNode node) {
            return TypeNodesFactory.CheckMutableStringNodeFactory.create(node);
        }

        public abstract void execute(Object object);


        @Specialization
        protected Object check(RubyString value,
                @Cached BranchProfile errorProfile) {
            if (value.locked) {
                errorProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().runtimeError("can't modify string; temporarily locked", this));
            } else if (value.frozen) {
                errorProfile.enter();
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
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class CheckRealNode extends PrimitiveNode {
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
        protected boolean other(Object value,
                @Cached IsANode isANode,
                @Cached ConditionProfile numericProfile,
                @Cached DispatchNode isRealNode,
                @Cached BooleanCastNode booleanCastNode) {
            return numericProfile.profile(isANode.executeIsA(value, coreLibrary().numericClass)) &&
                    booleanCastNode.executeToBoolean(isRealNode.call(value, "real?"));
        }
    }

    @Primitive(name = "undefined?")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class IsUndefinedNode extends PrimitiveNode {

        @Specialization
        protected boolean isUndefined(Object value) {
            return value == NotProvided.INSTANCE;
        }
    }

    @Primitive(name = "as_boolean")
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class AsBooleanNode extends PrimitiveNode {

        @Specialization
        protected boolean asBoolean(Object value,
                @Cached BooleanCastNode booleanCastNode) {
            return booleanCastNode.executeToBoolean(value);
        }
    }

}
