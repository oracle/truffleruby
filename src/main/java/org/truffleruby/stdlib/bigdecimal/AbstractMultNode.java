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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.numeric.BigDecimalOps;
import org.truffleruby.utils.Utils;

import java.math.BigDecimal;
import java.math.MathContext;

public abstract class AbstractMultNode extends BigDecimalOpNode {

    private final ConditionProfile zeroNormal = ConditionProfile.create();

    private Object multBigDecimalConsideringSignum(RubyBigDecimal a, RubyBigDecimal b, MathContext mathContext) {
        final BigDecimal bBigDecimal = b.value;

        if (zeroNormal.profile(isNormalZero(a) && BigDecimalOps.signum(bBigDecimal) == -1)) {
            return BigDecimalType.NEGATIVE_ZERO;
        }

        return multBigDecimal(a.value, bBigDecimal, mathContext);
    }

    @TruffleBoundary
    private Object multBigDecimal(BigDecimal a, BigDecimal b, MathContext mathContext) {
        return a.multiply(b, mathContext);
    }

    protected RubyBigDecimal mult(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        if (precision == 0) {
            precision = getLimit();
        }
        return createBigDecimal(multBigDecimalConsideringSignum(a, b, BigDecimalOps.newMathContext(
                precision,
                getRoundMode())));
    }

    protected Object multNormalSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        return multSpecialNormal(b, a, precision);
    }

    protected RubyBigDecimal multSpecialNormal(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        Object value = null;

        switch (a.type) {
            case NAN:
                value = BigDecimalType.NAN;
                break;
            case NEGATIVE_ZERO:
                switch (BigDecimalOps.signum(b)) {
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
                switch (BigDecimalOps.signum(b)) {
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
                switch (BigDecimalOps.signum(b)) {
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
                throw Utils.unsupportedOperation("unreachable code branch");
        }

        return createBigDecimal(value);
    }

    protected Object multSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
        final BigDecimalType aType = a.type;
        final BigDecimalType bType = b.type;

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

        throw Utils.unsupportedOperation("unreachable code branch");
    }
}
