/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import java.math.BigDecimal;
import java.math.MathContext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.numeric.BigDecimalOps;

public abstract class AbstractSubNode extends BigDecimalOpNode {

    private final ConditionProfile nanProfile = ConditionProfile.create();
    private final ConditionProfile posInfinityProfile = ConditionProfile.create();
    private final ConditionProfile negInfinityProfile = ConditionProfile.create();
    private final ConditionProfile normalProfile = ConditionProfile.create();

    protected Object sub(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        if (precision == 0) {
            precision = getLimit();
        }
        return createBigDecimal(subBigDecimal(a, b, BigDecimalOps.newMathContext(precision, getRoundMode())));
    }

    protected Object subSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        final BigDecimalType aType = a.type;
        final BigDecimalType bType = b.type;

        if (nanProfile.profile(aType == BigDecimalType.NAN || bType == BigDecimalType.NAN ||
                (aType == BigDecimalType.POSITIVE_INFINITY && bType == BigDecimalType.POSITIVE_INFINITY) ||
                (aType == BigDecimalType.NEGATIVE_INFINITY && bType == BigDecimalType.NEGATIVE_INFINITY))) {
            return createBigDecimal(BigDecimalType.NAN);
        }

        if (posInfinityProfile
                .profile(aType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY)) {
            return createBigDecimal(BigDecimalType.POSITIVE_INFINITY);
        }

        if (negInfinityProfile
                .profile(aType == BigDecimalType.NEGATIVE_INFINITY || bType == BigDecimalType.POSITIVE_INFINITY)) {
            return createBigDecimal(BigDecimalType.NEGATIVE_INFINITY);
        }

        // One is NEGATIVE_ZERO and second is NORMAL

        if (normalProfile.profile(isNormal(a))) {
            return a;
        } else {
            return createBigDecimal(BigDecimalOps.negate(b.value));
        }
    }

    @TruffleBoundary
    private BigDecimal subBigDecimal(RubyBigDecimal a, RubyBigDecimal b, MathContext mathContext) {
        return a.value.subtract(b.value, mathContext);
    }

}
