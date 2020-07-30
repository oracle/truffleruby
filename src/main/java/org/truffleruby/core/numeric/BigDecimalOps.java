/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.stdlib.bigdecimal.RubyBigDecimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/** Wrapper for methods of {@link BigDecimal} decorated with a {@link TruffleBoundary} annotation, as these methods are
 * blacklisted by SVM. */
public final class BigDecimalOps {

    @TruffleBoundary
    public static BigDecimal fromBigInteger(BigInteger value) {
        return new BigDecimal(value);
    }

    public static BigDecimal fromBigInteger(RubyBignum value) {
        return fromBigInteger(value.value);
    }

    @TruffleBoundary
    public static int compare(BigDecimal a, BigDecimal b) {
        return a.compareTo(b);
    }

    public static int compare(RubyBigDecimal a, BigDecimal b) {
        return compare(a.value, b);
    }

    @TruffleBoundary
    public static BigDecimal negate(BigDecimal value) {
        return value.negate();
    }

    @TruffleBoundary
    public static int signum(BigDecimal value) {
        return value.signum();
    }

    public static int signum(RubyBigDecimal value) {
        return signum(value.value);
    }

    @TruffleBoundary
    public static BigInteger toBigInteger(BigDecimal value) {
        return value.toBigInteger();
    }

    @TruffleBoundary
    public static BigDecimal valueOf(long value) {
        return BigDecimal.valueOf(value);
    }

    @TruffleBoundary
    public static BigDecimal valueOf(double value) {
        return BigDecimal.valueOf(value);
    }

    @TruffleBoundary
    public static MathContext newMathContext(int precision, RoundingMode roundingMode) {
        return new MathContext(precision, roundingMode);
    }
}
