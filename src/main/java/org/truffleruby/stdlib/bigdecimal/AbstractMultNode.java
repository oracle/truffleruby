/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
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

import org.truffleruby.Layouts;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class AbstractMultNode extends BigDecimalOpNode {

    private final ConditionProfile zeroNormal = ConditionProfile.createBinaryProfile();

    private Object multBigDecimalConsideringSignum(DynamicObject a, DynamicObject b, MathContext mathContext) {
        final BigDecimal bBigDecimal = Layouts.BIG_DECIMAL.getValue(b);

        if (zeroNormal.profile(isNormalZero(a) && bBigDecimal.signum() == -1)) {
            return BigDecimalType.NEGATIVE_ZERO;
        }

        return multBigDecimal(Layouts.BIG_DECIMAL.getValue(a), bBigDecimal, mathContext);
    }

    @TruffleBoundary
    private Object multBigDecimal(BigDecimal a, BigDecimal b, MathContext mathContext) {
        return a.multiply(b, mathContext);
    }

    protected Object mult(DynamicObject a, DynamicObject b, int precision) {
        if (precision == 0) {
            precision = getLimit();
        }
        return createBigDecimal(multBigDecimalConsideringSignum(a, b, new MathContext(precision, getRoundMode())));
    }

    protected Object multNormalSpecial(DynamicObject a, DynamicObject b, int precision) {
        return multSpecialNormal(b, a, precision);
    }

    protected Object multSpecialNormal(DynamicObject a, DynamicObject b, int precision) {
        Object value = null;

        switch (Layouts.BIG_DECIMAL.getType(a)) {
            case NAN:
                value = BigDecimalType.NAN;
                break;
            case NEGATIVE_ZERO:
                switch (Layouts.BIG_DECIMAL.getValue(b).signum()) {
                    case 1:
                    case 0:
                        value = BigDecimalType.NEGATIVE_ZERO;
                        break;
                    case -1:
                        value = BigDecimal.ZERO;
                        break;
                }
                break;
            case POSITIVE_INFINITY:
                switch (Layouts.BIG_DECIMAL.getValue(b).signum()) {
                    case 1:
                        value = BigDecimalType.POSITIVE_INFINITY;
                        break;
                    case 0:
                        value = BigDecimalType.NAN;
                        break;
                    case -1:
                        value = BigDecimalType.NEGATIVE_INFINITY;
                        break;
                }
                break;
            case NEGATIVE_INFINITY:
                switch (Layouts.BIG_DECIMAL.getValue(b).signum()) {
                    case 1:
                        value = BigDecimalType.NEGATIVE_INFINITY;
                        break;
                    case 0:
                        value = BigDecimalType.NAN;
                        break;
                    case -1:
                        value = BigDecimalType.POSITIVE_INFINITY;
                        break;
                }
                break;
            default:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new UnsupportedOperationException("unreachable code branch");
        }

        return createBigDecimal(value);
    }

    protected Object multSpecial(DynamicObject a, DynamicObject b, int precision) {
        final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
        final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

        if (aType == BigDecimalType.NAN || bType == BigDecimalType.NAN) {
            return createBigDecimal(BigDecimalType.NAN);
        } else if (aType == BigDecimalType.NEGATIVE_ZERO && bType == BigDecimalType.NEGATIVE_ZERO) {
            return createBigDecimal(BigDecimal.ZERO);
        } else if (aType == BigDecimalType.NEGATIVE_ZERO || bType == BigDecimalType.NEGATIVE_ZERO) {
            return createBigDecimal(BigDecimalType.NAN);
        }

        // a and b are only +-Infinity

        if (aType == BigDecimalType.POSITIVE_INFINITY) {
            if (bType == BigDecimalType.POSITIVE_INFINITY) {
                return a;
            } else {
                return createBigDecimal(BigDecimalType.NEGATIVE_INFINITY);
            }
        } else if (aType == BigDecimalType.NEGATIVE_INFINITY) {
            if (bType == BigDecimalType.POSITIVE_INFINITY) {
                return a;
            } else {
                return createBigDecimal((BigDecimalType.POSITIVE_INFINITY));
            }
        }

        throw new UnsupportedOperationException("unreachable code branch");
    }
}
