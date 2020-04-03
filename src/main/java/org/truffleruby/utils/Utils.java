/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

import java.util.Objects;

/** General purpose utility functions that do not fit in other utility classes. */
public final class Utils {

    /** Build a {@link UnsupportedOperationException} behind a {@link TruffleBoundary} so as to avoid the
     * SVM-blacklisted {@link Throwable#fillInStackTrace()}. */
    @TruffleBoundary
    public static UnsupportedOperationException unsupportedOperation(String msg) {
        return new UnsupportedOperationException(msg);
    }

    /** Build a {@link UnsupportedOperationException} behind a {@link TruffleBoundary} so as to avoid the
     * SVM-blacklisted {@link Throwable#fillInStackTrace()} and the implicit {@link String} methods. */
    @TruffleBoundary
    public static UnsupportedOperationException unsupportedOperation(Object... msgParts) {
        return new UnsupportedOperationException(concat(msgParts));
    }

    /** Performs {@link Objects#equals(Object, Object)} behind a {@link TruffleBoundary} so as to avoid performance
     * warnings, since {@link Object#equals} is blacklisted. */
    @TruffleBoundary
    public static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /** Converts the arguments to strings and concatenate them behind a {@link TruffleBoundary}, so as to avoid the
     * implicit calls to SVM-blacklisted {@link String} methods. */
    @TruffleBoundary
    public static String concat(Object one, Object two) {
        return String.valueOf(one) + two;
    }

    /** Converts the {@code parts} to strings and concatenate them behind a {@link TruffleBoundary}, so as to avoid the
     * implicit calls to SVM-blacklisted {@link String} methods. */
    @TruffleBoundary
    public static String concat(Object... parts) {
        StringBuilder builder = new StringBuilder();
        for (Object part : parts) {
            builder.append(part);
        }
        return builder.toString();
    }

    /** Returns a exception to be thrown in unreachable code paths and calls
     * {@link CompilerDirectives#transferToInterpreterAndInvalidate()}. */
    public static UnreachableCodeException unreachable() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return new UnreachableCodeException();
    }

    /** Returns a exception to be thrown in unreachable code paths and calls
     * {@link CompilerDirectives#transferToInterpreterAndInvalidate()}. */
    public static UnreachableCodeException unreachable(String msg) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return new UnreachableCodeException(msg);
    }

    /** Returns a exception to be thrown in unreachable code paths and calls
     * {@link CompilerDirectives#transferToInterpreterAndInvalidate()}. */
    @SuppressWarnings("RedundantCast")
    public static UnreachableCodeException unreachable(String... msgParts) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        // The cast avoids ECJ complaining about ambiguity.
        return new UnreachableCodeException(concat((Object[]) msgParts));
    }

    // TODO review - fill Truffle issue?
    // example backtrace
    // https://graal.us.oracle.com/buildbot2/builders/ci_executor/builds/5405184/steps/Run%20executor/logs/stdio/text
    // Blacklisted method
    //   java.lang.Object.equals(Object)
    // called from
    //   com.oracle.truffle.object.enterprise.EnterpriseLocations$EnterpriseLocation.valueEquals(EnterpriseLocations.java:243)
    //   com.oracle.truffle.object.enterprise.EnterpriseLocations$ValueLocation.canStore(EnterpriseLocations.java:304)
    //   com.oracle.truffle.object.enterprise.EnterpriseLocations$ValueLocation.setInternal(EnterpriseLocations.java:316)
    //   com.oracle.truffle.object.PropertyImpl.setInternal(PropertyImpl.java:126)
    //   com.oracle.truffle.object.ShapeImpl$DynamicObjectFactoryImpl.newInstance(ShapeImpl.java:1102)
    //   org.truffleruby.core.hash.HashLiteralNode$GenericHashLiteralNode.execute(HashLiteralNode.java:160)
    //   org.truffleruby.language.RubyRootNode.execute(RubyRootNode.java:61)
    //   org.graalvm.compiler.truffle.runtime.OptimizedCallTarget.callProxy(OptimizedCallTarget.java:479)
    //   org.graalvm.compiler.truffle.runtime.OptimizedCallTarget.callRoot(OptimizedCallTarget.java:454)

    // Even worse, this also happens in generated code:
    // https://graal.us.oracle.com/buildbot2/builders/ci_executor/builds/5415638/steps/Run%20executor/logs/stdio/text

    /** Calls {@link DynamicObjectFactory#newInstance(Object...)}} behind a {@link TruffleBoundary} because of a
     * potential call to the blacklisted {@link Object#equals(Object)}. */
    @TruffleBoundary
    public static DynamicObject newInstance(DynamicObjectFactory factory, Object... initialValues) {
        return factory.newInstance(initialValues);
    }
}
