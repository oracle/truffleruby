/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import java.math.BigDecimal;
import java.math.MathContext;

import org.truffleruby.Layouts;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class AbstractAddNode extends BigDecimalOpNode {

    private final ConditionProfile nanProfile = ConditionProfile.createBinaryProfile();
    private final ConditionProfile posInfinityProfile = ConditionProfile.createBinaryProfile();
    private final ConditionProfile negInfinityProfile = ConditionProfile.createBinaryProfile();
    private final ConditionProfile normalProfile = ConditionProfile.createBinaryProfile();

    protected Object add(DynamicObject a, DynamicObject b, int precision) {
        if (precision == 0) {
            precision = getLimit();
        }
        return createBigDecimal(addBigDecimal(a, b, new MathContext(precision, getRoundMode())));
    }

    protected Object addSpecial(DynamicObject a, DynamicObject b, int precision) {
        final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
        final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

        if (nanProfile.profile(aType == BigDecimalType.NAN || bType == BigDecimalType.NAN ||
                (aType == BigDecimalType.POSITIVE_INFINITY && bType == BigDecimalType.NEGATIVE_INFINITY) ||
                (aType == BigDecimalType.NEGATIVE_INFINITY && bType == BigDecimalType.POSITIVE_INFINITY))) {
            return createBigDecimal(BigDecimalType.NAN);
        }

        if (posInfinityProfile
                .profile(aType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.POSITIVE_INFINITY)) {
            return createBigDecimal(BigDecimalType.POSITIVE_INFINITY);
        }

        if (negInfinityProfile
                .profile(aType == BigDecimalType.NEGATIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY)) {
            return createBigDecimal(BigDecimalType.NEGATIVE_INFINITY);
        }

        // One is NEGATIVE_ZERO and second is NORMAL

        if (normalProfile.profile(isNormal(a))) {
            return a;
        } else {
            return b;
        }
    }

    @TruffleBoundary
    private BigDecimal addBigDecimal(DynamicObject a, DynamicObject b, MathContext mathContext) {
        return Layouts.BIG_DECIMAL.getValue(a).add(Layouts.BIG_DECIMAL.getValue(b), mathContext);
    }

}
