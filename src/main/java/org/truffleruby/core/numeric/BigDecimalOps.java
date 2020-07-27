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
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.stdlib.bigdecimal.RubyBigDecimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.truffleruby.Layouts.BIGNUM;
import static org.truffleruby.language.RubyGuards.isRubyBignum;

/** Wrapper for methods of {@link BigDecimal} decorated with a {@link TruffleBoundary} annotation, as these methods are
 * blacklisted by SVM. */
public final class BigDecimalOps {

    @TruffleBoundary
    public static BigDecimal fromBigInteger(BigInteger value) {
        return new BigDecimal(value);
    }

    public static BigDecimal fromBigInteger(DynamicObject value) {
        assert isRubyBignum(value);
        return fromBigInteger(BIGNUM.getValue(value));
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
