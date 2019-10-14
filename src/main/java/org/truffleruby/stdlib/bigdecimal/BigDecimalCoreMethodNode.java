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
import java.math.RoundingMode;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class BigDecimalCoreMethodNode extends CoreMethodNode {

    @Child private CreateBigDecimalNode createBigDecimal;
    @Child private CallDispatchHeadNode limitCall;
    @Child private IntegerCastNode limitIntegerCast;
    @Child private CallDispatchHeadNode roundModeCall;
    @Child private IntegerCastNode roundModeIntegerCast;

    public static boolean isNormal(DynamicObject value) {
        return Layouts.BIG_DECIMAL.getType(value) == BigDecimalType.NORMAL;
    }

    public static boolean isNormalRubyBigDecimal(DynamicObject value) {
        return RubyGuards.isRubyBigDecimal(value) && Layouts.BIG_DECIMAL.getType(value) == BigDecimalType.NORMAL;
    }

    public static boolean isSpecialRubyBigDecimal(DynamicObject value) {
        return RubyGuards.isRubyBigDecimal(value) && Layouts.BIG_DECIMAL.getType(value) != BigDecimalType.NORMAL;
    }

    public static boolean isNormalZero(DynamicObject value) {
        return Layouts.BIG_DECIMAL.getValue(value).compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNan(DynamicObject value) {
        return Layouts.BIG_DECIMAL.getType(value) == BigDecimalType.NAN;
    }

    protected DynamicObject createBigDecimal(Object value) {
        return createBigDecimal(value, true);
    }

    protected DynamicObject createBigDecimal(Object value, boolean strict) {
        return getCreateBigDecimal().executeCreate(value, NotProvided.INSTANCE, strict);
    }

    protected DynamicObject createBigDecimal(Object value, int digits, boolean strict) {
        return getCreateBigDecimal().executeCreate(value, digits, strict);
    }

    protected RoundingMode getRoundMode() {
        return toRoundingMode(getRoundModeIntegerCast().executeCastInt(
                // TODO (pitr 21-Jun-2015): read the actual constant
                getRoundModeCall().call(getBigDecimalClass(), "mode", 256)));
    }

    protected DynamicObject getBigDecimalClass() {
        return coreLibrary().getBigDecimalClass();
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
                throw new UnsupportedOperationException("unknown value: " + constValue);
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

    private CallDispatchHeadNode getLimitCall() {
        if (limitCall == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            limitCall = insert(CallDispatchHeadNode.createPrivate());
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

    private CallDispatchHeadNode getRoundModeCall() {
        if (roundModeCall == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            roundModeCall = insert(CallDispatchHeadNode.createPrivate());
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
