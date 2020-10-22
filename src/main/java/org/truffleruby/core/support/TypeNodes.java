/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodes.ToSNode;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives;
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
    public static abstract class ObjectKindOfNode extends PrimitiveArrayArgumentsNode {

        @Child private IsANode isANode = IsANode.create();

        @Specialization
        protected boolean objectKindOf(Object object, RubyModule module) {
            return isANode.executeIsA(object, module);
        }

    }

    @Primitive(name = "object_respond_to?")
    public static abstract class ObjectRespondToNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.RespondToNode respondToNode = KernelNodesFactory.RespondToNodeFactory
                .create(null, null, null);

        @Specialization
        protected boolean objectRespondTo(Object object, Object name, boolean includePrivate) {
            // Do not pass a frame here, we want to ignore refinements and not need to read the caller frame
            return respondToNode.executeDoesRespondTo(null, object, name, includePrivate);
        }

    }

    @CoreMethod(names = "object_class", onSingleton = true, required = 1)
    public static abstract class ObjectClassNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        protected RubyClass objectClass(Object object) {
            return classNode.executeLogicalClass(object);
        }

    }

    @Primitive(name = "object_equal")
    public static abstract class ObjectEqualNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean objectEqual(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode.executeReferenceEqual(a, b);
        }

    }

    @Primitive(name = "nil?")
    public static abstract class IsNilNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isNil(Object value) {
            return value == nil;
        }
    }

    @Primitive(name = "object_ivars")
    public abstract static class ObjectInstanceVariablesNode extends PrimitiveArrayArgumentsNode {

        public abstract RubyArray executeGetIVars(Object self);

        @TruffleBoundary
        @Specialization
        protected RubyArray instanceVariables(RubyDynamicObject object) {
            final List<String> names = new ArrayList<>();

            for (Object name : DynamicObjectLibrary.getUncached().getKeyArray(object)) {
                if (name instanceof String) {
                    names.add((String) name);
                }
            }

            final int size = names.size();
            final String[] sortedNames = names.toArray(StringUtils.EMPTY_STRING_ARRAY);
            Arrays.sort(sortedNames);

            final Object[] nameSymbols = new Object[size];
            for (int i = 0; i < sortedNames.length; i++) {
                nameSymbols[i] = getSymbol(sortedNames[i]);
            }

            return createArray(nameSymbols);
        }

        @Specialization
        protected RubyArray instanceVariables(int object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization
        protected RubyArray instanceVariables(long object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization
        protected RubyArray instanceVariables(double object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization
        protected RubyArray instanceVariables(boolean object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization
        protected RubyArray instanceVariablesNil(Nil object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization
        protected RubyArray instanceVariablesSymbol(RubySymbol object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization(guards = "isForeignObject(object)")
        protected RubyArray instanceVariablesForeign(Object object) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

    }

    @Primitive(name = "object_ivar_defined?")
    public abstract static class ObjectIVarIsDefinedNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean ivarIsDefined(RubyDynamicObject object, RubySymbol name) {
            final String ivar = name.getString();
            return object.getShape().hasProperty(ivar);
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

        abstract public boolean execute(RubyArray array);

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

    @Primitive(name = "infect")
    public static abstract class InfectNode extends PrimitiveArrayArgumentsNode {

        @Child private RubyLibrary rubyLibraryTaint;

        @Specialization(limit = "getRubyLibraryCacheLimit()")
        protected Object infect(Object host, Object source,
                @CachedLibrary("source") RubyLibrary rubyLibrary) {
            if (rubyLibrary.isTainted(source)) {
                // This lazy node allocation effectively gives us a branch profile

                if (rubyLibraryTaint == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    rubyLibraryTaint = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
                }
                rubyLibraryTaint.taint(host);
            }

            return host;
        }

    }

    @CoreMethod(names = "module_name", onSingleton = true, required = 1)
    public static abstract class ModuleNameNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString moduleName(RubyModule module) {
            final String name = module.fields.getName();
            return makeStringNode.executeMake(name, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @Primitive(name = "rb_num2long")
    public static abstract class RbNum2LongPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Child private ToLongNode toLongNode = ToLongNode.create();

        @Specialization
        protected long numToLong(Object value) {
            return toLongNode.execute(value);
        }
    }

    @Primitive(name = "rb_num2int")
    public static abstract class RbNum2IntPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Child private ToIntNode toIntNode = ToIntNode.create();

        @Specialization
        protected long numToInt(Object value) {
            return toIntNode.execute(value);
        }
    }

    @Primitive(name = "rb_to_int")
    public static abstract class RbToIntNode extends PrimitiveArrayArgumentsNode {
        @Child private ToRubyIntegerNode toRubyInteger = ToRubyIntegerNode.create();

        @Specialization
        protected Object toRubyInteger(Object value) {
            return toRubyInteger.execute(value);
        }
    }

    @Primitive(name = "double_to_float")
    public static abstract class DoubleToFloatNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected float doubleToFloat(double value) {
            return (float) value;
        }

    }

    @Primitive(name = "check_frozen")
    @NodeChild(value = "value", type = RubyNode.class)
    public static abstract class CheckFrozenNode extends PrimitiveNode {

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

    @Primitive(name = "undefined?")
    @NodeChild(value = "value", type = RubyNode.class)
    public static abstract class IsUndefinedNode extends PrimitiveNode {

        @Specialization
        protected boolean isUndefined(Object value) {
            return value == NotProvided.INSTANCE;
        }
    }
}
