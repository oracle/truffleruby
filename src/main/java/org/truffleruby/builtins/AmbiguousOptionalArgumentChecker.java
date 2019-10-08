/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

public class AmbiguousOptionalArgumentChecker {

    public static boolean SUCCESS = true;
    private static boolean AVAILABLE = true;

    public static void verifyNoAmbiguousOptionalArguments(NodeFactory<? extends RubyNode> nodeFactory,
            CoreMethod method) {
        if (!AVAILABLE) {
            return;
        }
        try {
            verifyNoAmbiguousOptionalArgumentsWithReflection(nodeFactory, method);
        } catch (Exception e) {
            e.printStackTrace();
            SUCCESS = false;
        }
    }

    @SuppressFBWarnings("Dm")
    private static void verifyNoAmbiguousOptionalArgumentsWithReflection(NodeFactory<? extends RubyNode> nodeFactory,
            CoreMethod methodAnnotation) {
        if (methodAnnotation.optional() > 0 || methodAnnotation.needsBlock()) {
            final int opt = methodAnnotation.optional();

            final Class<?> node = nodeFactory.getNodeClass();
            for (Method method : specializations(node)) {
                boolean unguardedParams = false;
                StringBuilder errors = new StringBuilder();

                Class<?>[] parameterTypes = method.getParameterTypes();
                Parameter[] parameters = method.getParameters();

                int n = parameterTypes.length - 1;
                // Ignore all the @Cached methods from our consideration.
                while (n >= 0 && parameters[n].isAnnotationPresent(Cached.class)) {
                    n--;
                }
                if (methodAnnotation.needsBlock()) {
                    if (n < 0) {
                        SUCCESS = false;
                        RubyLanguage.LOGGER.warning("invalid block method parameter position for " + method);
                        continue;
                    }
                    unguardedParams = unguardedParams |
                            isParameterUnguarded(method, parameters, parameterTypes, n, errors);
                    n--; // Ignore block argument.
                }
                if (methodAnnotation.rest()) {
                    if (n < 0 || !parameterTypes[n].isArray()) {
                        SUCCESS = false;
                        RubyLanguage.LOGGER.warning("invalid rest method parameter " + n + " for " + method);
                        continue;
                    }
                    n--; // ignore final Object[] argument
                }
                for (int i = 0; i < opt; i++, n--) {
                    if (n < 0) {
                        SUCCESS = false;
                        RubyLanguage.LOGGER.warning("invalid optional paramenter count for " + method);
                        continue;
                    }
                    unguardedParams = unguardedParams |
                            isParameterUnguarded(method, parameters, parameterTypes, n, errors);
                }
                // required arguments are not checked as they should not receive a NotProvided instance.
                if (unguardedParams) {
                    SUCCESS = false;
                    RubyLanguage.LOGGER
                            .warning("ambiguous optional argument in " + node.getCanonicalName() + ":\n" + errors);
                }
            }
        }
    }

    private static boolean isParameterUnguarded(Method method, Parameter[] parameters, Class<?>[] types, int n,
            StringBuilder errors) {
        Class<?> parameterType = types[n];

        Parameter parameter = parameters[n];
        boolean isNamePresent = parameter.isNamePresent();
        if (!isNamePresent) {
            AVAILABLE = SUCCESS = false;
            RubyLanguage.LOGGER.warning("method parameters names are not available for " + method);
            return false;
        }
        String name = parameter.getName();

        // A specialization will only be called if the types of the arguments match its declared parameter
        // types. So a specialization with a declared optional parameter of type NotProvided will only be
        // called if that argument is not supplied. Similarly a specialization with a DynamicObject optional
        // parameter will only be called if the value has been supplied.
        //
        // Since Object is the super type of NotProvided any optional parameter declaration of type Object
        // must have additional guards to check whether this specialization should be called, or must make
        // it clear in the parameter name that it may not have been provided or is not used.
        if (parameterType == Object.class && !name.startsWith("unused") && !name.startsWith("maybe")) {
            String[] guards = method.getAnnotation(Specialization.class).guards();
            if (!isGuarded(name, guards)) {
                errors
                        .append("\"")
                        .append(name)
                        .append("\" in ")
                        .append(methodToString(method, types, parameters))
                        .append("\n");
                return true;
            }
        }
        return false;
    }

    private static List<Method> specializations(Class<?> node) {
        Method[] methods = node.getDeclaredMethods();
        return Arrays.stream(methods).filter(m -> isSpecialization(m)).collect(Collectors.toList());
    }

    @SuppressFBWarnings("Dm")
    private static boolean isSpecialization(Method m) {
        return m.isAnnotationPresent(Specialization.class);
    }

    private static boolean isGuarded(String name, String[] guards) {
        for (String guard : guards) {
            if (guard.equals("wasProvided(" + name + ")") ||
                    guard.equals("wasNotProvided(" + name + ")") ||
                    guard.equals("isNil(" + name + ")")) {
                return true;
            }
        }
        return false;
    }

    private static String methodToString(Method method, Class<?>[] parameterTypes, Parameter[] parameters) {
        StringBuilder str = new StringBuilder();
        str.append(method.getName()).append("(");
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = parameter.getName();
            str.append(parameterTypes[i].getSimpleName()).append(" ").append(name);
            if (i < parameters.length - 1) {
                str.append(", ");
            }
        }
        str.append(")");
        return str.toString();
    }
}
