/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop.java;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jcodings.specific.UTF8Encoding;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;

import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreClass("Truffle::Interop::Java")
public class JavaUtilitiesNodes {

    @CoreMethod(names = "to_ruby_string", required = 1, isModuleFunction = true)
    public static abstract class ToRubyStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        public DynamicObject toRubyString(String target) {
            return createString(getRope(target));
        }

        protected Rope getRope(String value) {
            return StringOperations.encodeRope(value, UTF8Encoding.INSTANCE);
        }
    }

    @CoreMethod(names = "java_class_by_name", isModuleFunction = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class JavaClassByNameNode extends CoreMethodNode {
        @CreateCast("name")
        public RubyNode coercetNameToString(RubyNode newName) {
            return NameToJavaStringNodeGen.create(newName);
        }

        @Specialization
        public Object javaClassByName(String name,
                                      @Cached("create()") BranchProfile errorProfile) {
            if (!TruffleOptions.AOT) {
                try {
                    Class<?> klass =  Class.forName(name);
                    return klass;
                } catch (Exception e) {
                    errorProfile.enter();
                    throw new RaiseException(coreExceptions().nameErrorImportNotFound(name, this));
                }
            } else {
                throw new RaiseException(coreExceptions().runtimeError("Not available on SVM.", this));
            }
        }
    }

    @CoreMethod(names = "to_java_string", required = 1, isModuleFunction = true)
    public static abstract class ToJavaStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        public String toJavaString(VirtualFrame frame, DynamicObject string,
                                   @Cached("create()") NameToJavaStringNode toJavaStringNode) {
            return toJavaStringNode.executeToJavaString(frame, string);
        }
    }

    @CoreMethod(names = "java_hash", required = 1, isModuleFunction = true)
    public static abstract class JavaHashNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        public int toJavaHash(Object target) {
            return target.hashCode();
        }
    }

    @CoreMethod(names = "java_refs_equal?", required = 2, isModuleFunction = true)
    public static abstract class JavaEqlNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        public boolean toJavaHash(Object a, Object b) {
            return a == b;
        }
    }

    @CoreMethod(names = "get_java_method", required = 4, rest = true, isModuleFunction = true)
    public static abstract class JavaGetMethodNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object getJavaMethod(VirtualFrame frame, Object target, Object name,
                boolean staticMethod, Object returnType, Object[] rest,
                @Cached("create()") NameToJavaStringNode toJavaStringNode) {
            if (!TruffleOptions.AOT) {
                String methodName = toJavaStringNode.executeToJavaString(frame, name);
                Class<?> klass = (Class<?>) target;
                Class<?> returnClass = (Class<?>) returnType;
                Class<?>[] args = new Class[rest.length];
                for (int i = 0; i < rest.length; i++) {
                    args[i] = (Class<?>) rest[i];
                }
                try {
                    if (staticMethod) {
                        return MethodHandles.lookup().findStatic(klass, methodName, MethodType.methodType(returnClass, args));
                    } else {
                        return MethodHandles.lookup().findVirtual(klass, methodName, MethodType.methodType(returnClass, args));
                    }
                } catch (Exception e) {
                    throw new RaiseException(coreExceptions().nameError(StringUtils.format("Method %s cannot be accessed.", methodName), null, methodName, this));
                }
            } else {
                throw new RaiseException(coreExceptions().runtimeError("Not available on SVM.", this));
            }
        }
    }

    @CoreMethod(names = "get_lookup", required = 0, rest = false, isModuleFunction = true)
    public static abstract class GetLookupNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object getLookup() {
            if (!TruffleOptions.AOT) {
                return MethodHandles.lookup();
            } else {
                throw new RaiseException(coreExceptions().runtimeError("Not available on SVM.", this));
            }
        }
    }

    @CoreMethod(names = "invoke_java_method", required = 1, rest = true, isModuleFunction = true)
    public static abstract class JavaInvokeMethodNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object invokeJavaMethod(MethodHandle method, Object[] rest) {
            if (!TruffleOptions.AOT) {
                try {
                    Object res = method.invokeWithArguments(rest);
                    return res == null ? nil() : res;
                } catch (Throwable e) {
                    throw new JavaException(e);
                }
            } else {
                throw new RaiseException(coreExceptions().runtimeError("Not available on SVM.", this));
            }
        }
    }
}
