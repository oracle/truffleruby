/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.kernel.KernelNodes.ToSNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsTaintedNode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.PropertyFlags;
import org.truffleruby.language.objects.TaintNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

@CoreModule("Truffle::Type")
public abstract class TypeNodes {

    @CoreMethod(names = "object_kind_of?", onSingleton = true, required = 2)
    public static abstract class ObjectKindOfNode extends CoreMethodArrayArgumentsNode {

        @Child private IsANode isANode = IsANode.create();

        @Specialization
        protected boolean objectKindOf(Object object, DynamicObject rubyClass) {
            return isANode.executeIsA(object, rubyClass);
        }

    }

    @CoreMethod(names = "object_class", onSingleton = true, required = 1)
    public static abstract class VMObjectClassNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        protected DynamicObject objectClass(VirtualFrame frame, Object object) {
            return classNode.executeLogicalClass(object);
        }

    }

    @CoreMethod(names = "object_equal", onSingleton = true, required = 2)
    public static abstract class ObjectEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean objectEqual(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode.executeReferenceEqual(a, b);
        }

    }

    @Primitive(name = "object_ivars")
    public abstract static class ObjectInstanceVariablesNode extends PrimitiveArrayArgumentsNode {

        public abstract DynamicObject executeGetIVars(Object self);

        @TruffleBoundary
        @Specialization(guards = { "!isNil(object)", "!isRubySymbol(object)" })
        protected DynamicObject instanceVariables(DynamicObject object) {
            Shape shape = object.getShape();
            List<String> names = new ArrayList<>();

            for (Property property : shape.getProperties()) {
                Object name = property.getKey();
                if (PropertyFlags.isDefined(property) && name instanceof String) {
                    names.add((String) name);
                }
            }

            final int size = names.size();
            final String[] sortedNames = names.toArray(new String[size]);
            Arrays.sort(sortedNames);

            final Object[] nameSymbols = new Object[size];
            for (int i = 0; i < sortedNames.length; i++) {
                nameSymbols[i] = getSymbol(sortedNames[i]);
            }

            return createArray(nameSymbols, size);
        }

        @Specialization
        protected DynamicObject instanceVariables(int object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

        @Specialization
        protected DynamicObject instanceVariables(long object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

        @Specialization
        protected DynamicObject instanceVariables(boolean object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

        @Specialization(guards = "isNil(object)")
        protected DynamicObject instanceVariablesNil(DynamicObject object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

        @Specialization(guards = "isRubySymbol(object)")
        protected DynamicObject instanceVariablesSymbol(DynamicObject object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

        @Fallback
        protected DynamicObject instanceVariables(Object object) {
            return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);
        }

    }

    @Primitive(name = "object_ivar_defined?")
    public abstract static class ObjectIVarIsDefinedNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object ivarIsDefined(DynamicObject object, DynamicObject name) {
            final String ivar = Layouts.SYMBOL.getString(name);
            final Property property = object.getShape().getProperty(ivar);
            return PropertyFlags.isDefined(property);
        }

    }

    @Primitive(name = "object_ivar_get")
    public abstract static class ObjectIVarGetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object ivarGet(DynamicObject object, DynamicObject name,
                @Cached ObjectIVarGetNode iVarGetNode) {
            return iVarGetNode.executeIVarGet(object, Layouts.SYMBOL.getString(name));
        }
    }

    @Primitive(name = "object_ivar_set")
    public abstract static class ObjectIVarSetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object ivarSet(DynamicObject object, DynamicObject name, Object value,
                @Cached ObjectIVarSetNode iVarSetNode) {
            return iVarSetNode.executeIVarSet(object, Layouts.SYMBOL.getString(name), value);
        }
    }

    @Primitive(name = "object_can_contain_object")
    @ImportStatic(ArrayGuards.class)
    public abstract static class CanContainObjectNode extends PrimitiveArrayArgumentsNode {

        @Specialization(
                guards = { "isRubyArray(array)", "strategy.matches(array)", "strategy.isPrimitive()" },
                limit = "STORAGE_STRATEGIES")
        protected boolean primitiveArray(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            return false;
        }

        @Specialization(
                guards = { "isRubyArray(array)", "strategy.matches(array)", "!strategy.isPrimitive()" },
                limit = "STORAGE_STRATEGIES")
        protected boolean objectArray(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            return true;
        }

        @Specialization(guards = "!isRubyArray(object)")
        protected boolean other(Object object) {
            return true;
        }

    }

    @CoreMethod(names = "rb_any_to_s", onSingleton = true, required = 1)
    public abstract static class ObjectToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject toS(Object obj,
                @Cached ToSNode kernelToSNode) {
            return kernelToSNode.executeToS(obj);
        }

    }

    @CoreMethod(names = "infect", onSingleton = true, required = 2)
    public static abstract class InfectNode extends CoreMethodArrayArgumentsNode {

        @Child private IsTaintedNode isTaintedNode;
        @Child private TaintNode taintNode;

        @Specialization
        protected Object infect(Object host, Object source) {
            if (isTaintedNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isTaintedNode = insert(IsTaintedNode.create());
            }

            if (isTaintedNode.executeIsTainted(source)) {
                // This lazy node allocation effectively gives us a branch profile

                if (taintNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    taintNode = insert(TaintNode.create());
                }

                taintNode.executeTaint(host);
            }

            return host;
        }

    }

    @CoreMethod(names = "module_name", onSingleton = true, required = 1)
    public static abstract class ModuleNameNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected DynamicObject moduleName(DynamicObject rubyModule) {
            final String name = Layouts.MODULE.getFields(rubyModule).getName();
            return makeStringNode.executeMake(name, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "double_to_float", onSingleton = true, required = 1)
    public static abstract class DoubleToFloatNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected float doubleToFloat(double value) {
            return (float) value;
        }

    }

}
