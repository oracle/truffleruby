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
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.BigDecimalOps;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class BigDecimalCoreMethodNode extends CoreMethodNode {

    @Child private CreateBigDecimalNode createBigDecimal;
    @Child private DispatchNode limitCall;
    @Child private IntegerCastNode limitIntegerCast;
    @Child private DispatchNode roundModeCall;
    @Child private IntegerCastNode roundModeIntegerCast;

    public static boolean isNormal(RubyBigDecimal value) {
        return value.type == BigDecimalType.NORMAL;
    }

    public static boolean isSpecial(RubyBigDecimal value) {
        return !isNormal(value);
    }

    public static boolean isNormalZero(RubyBigDecimal value) {
        return BigDecimalOps.compare(value.value, BigDecimal.ZERO) == 0;
    }

    public static boolean isNan(RubyBigDecimal value) {
        return value.type == BigDecimalType.NAN;
    }

    protected RubyBigDecimal createBigDecimal(Object value) {
        return createBigDecimal(value, true);
    }

    protected RubyBigDecimal createBigDecimal(Object value, boolean strict) {
        return getCreateBigDecimal().executeCreate(value, NotProvided.INSTANCE, strict);
    }

    protected RubyBigDecimal createBigDecimal(Object value, int digits, boolean strict) {
        return getCreateBigDecimal().executeCreate(value, digits, strict);
    }

    protected RoundingMode getRoundMode() {
        return toRoundingMode(getRoundModeIntegerCast().executeCastInt(
                // TODO (pitr 21-Jun-2015): read the actual constant
                getRoundModeCall().call(getBigDecimalClass(), "mode", 256)));
    }

    protected RubyClass getBigDecimalClass() {
        return coreLibrary().bigDecimalClass;
    }

    protected static RoundingMode toRoundingMode(int constValue) {
        switch (constValue) {
            case 1:
                return RoundingMode.UP;
            case 2:
                return RoundingMode.DOWN;
            case 3:
                return RoundingMode.HALF_UP;
            case 4:
                return RoundingMode.HALF_DOWN;
            case 5:
                return RoundingMode.CEILING;
            case 6:
                return RoundingMode.FLOOR;
            case 7:
                return RoundingMode.HALF_EVEN;
            default:
                throw Utils.unsupportedOperation("unknown value: ", constValue);
        }
    }

    protected static int nearestBiggerMultipleOf4(int value) {
        return ((value / 4) + 1) * 4;
    }

    protected static int nearestBiggerMultipleOf9(int value) {
        return ((value / 9) + 1) * 9;
    }

    protected static int defaultDivisionPrecision(int precisionA, int precisionB, int limit) {
        final int combination = nearestBiggerMultipleOf4(precisionA + precisionB) * 4;
        return (limit > 0 && limit < combination) ? limit : combination;
    }

    @TruffleBoundary
    protected static int defaultDivisionPrecision(BigDecimal a, BigDecimal b, int limit) {
        return defaultDivisionPrecision(a.precision(), b.precision(), limit);
    }

    protected int getLimit() {
        return getLimitIntegerCast().executeCastInt(getLimitCall().call(getBigDecimalClass(), "limit"));
    }

    private CreateBigDecimalNode getCreateBigDecimal() {
        if (createBigDecimal == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            createBigDecimal = insert(CreateBigDecimalNodeFactory.create(null, null, null));
        }

        return createBigDecimal;
    }

    private DispatchNode getLimitCall() {
        if (limitCall == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            limitCall = insert(DispatchNode.create());
        }

        return limitCall;
    }

    private IntegerCastNode getLimitIntegerCast() {
        if (limitIntegerCast == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            limitIntegerCast = insert(IntegerCastNode.create());
        }

        return limitIntegerCast;
    }

    private DispatchNode getRoundModeCall() {
        if (roundModeCall == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            roundModeCall = insert(DispatchNode.create());
        }

        return roundModeCall;
    }

    private IntegerCastNode getRoundModeIntegerCast() {
        if (roundModeIntegerCast == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            roundModeIntegerCast = insert(IntegerCastNode.create());
        }

        return roundModeIntegerCast;
    }

}
