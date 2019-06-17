/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NodeChild(value = "value", type = RubyNode.class)
@NodeChild(value = "digits", type = RubyNode.class)
@ImportStatic(BigDecimalType.class)
public abstract class CreateBigDecimalNode extends BigDecimalCoreMethodNode {

    private static final String EXPONENT = "([eE][+-]?)?(\\d*)";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^([+-]?\\d*\\.?\\d*" + EXPONENT + ").*");
    private static final Pattern ZERO_PATTERN = Pattern.compile("^[+-]?0*\\.?0*" + EXPONENT);

    @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

    public abstract DynamicObject executeCreate(Object value, Object digits);

    private DynamicObject createNormalBigDecimal(BigDecimal value) {
        return allocateNode.allocate(getBigDecimalClass(), Layouts.BIG_DECIMAL.build(value, BigDecimalType.NORMAL));
    }

    private DynamicObject createSpecialBigDecimal(BigDecimalType type) {
        return allocateNode.allocate(getBigDecimalClass(), Layouts.BIG_DECIMAL.build(BigDecimal.ZERO, type));
    }

    @Specialization
    public DynamicObject create(long value, NotProvided digits) {
        return executeCreate(value, 0);
    }

    @Specialization
    public DynamicObject create(long value, int digits,
            @Cached("create()") BigDecimalCastNode bigDecimalCastNode) {
        BigDecimal bigDecimal = round((BigDecimal) bigDecimalCastNode.execute(value, getRoundMode()),
                new MathContext(digits, getRoundMode()));
        return createNormalBigDecimal(bigDecimal);
    }

    @Specialization
    public DynamicObject create(double value, NotProvided digits,
            @Cached("createBinaryProfile()") ConditionProfile finiteValueProfile,
            @Cached("create()") BranchProfile nanProfile,
            @Cached("create()") BranchProfile positiveInfinityProfile,
            @Cached("create()") BranchProfile negativeInfinityProfile) {
        if (finiteValueProfile.profile(Double.isFinite(value))) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorCantOmitPrecision(this));
        } else {
            return createNonFiniteBigDecimal(value, nanProfile, positiveInfinityProfile, negativeInfinityProfile);
        }
    }

    @Specialization
    public DynamicObject create(double value, int digits,
            @Cached("create()") BigDecimalCastNode bigDecimalCastNode,
            @Cached("createBinaryProfile()") ConditionProfile finiteValueProfile,
            @Cached("create()") BranchProfile nanProfile,
            @Cached("create()") BranchProfile positiveInfinityProfile,
            @Cached("create()") BranchProfile negativeInfinityProfile) {
        if (finiteValueProfile.profile(Double.isFinite(value))) {
            final RoundingMode roundMode = getRoundMode();
            final BigDecimal bigDecimal = (BigDecimal) bigDecimalCastNode.execute(value, roundMode);

            return createNormalBigDecimal(round(bigDecimal, new MathContext(digits, roundMode)));
        } else {
            return createNonFiniteBigDecimal(value, nanProfile, positiveInfinityProfile, negativeInfinityProfile);
        }
    }

    @Specialization(guards = "type == NEGATIVE_INFINITY || type == POSITIVE_INFINITY")
    public DynamicObject createInfinity(BigDecimalType type, Object digits,
            @Cached("create()") BooleanCastNode booleanCastNode,
            @Cached("create()") GetIntegerConstantNode getIntegerConstantNode,
            @Cached("createPrivate()") CallDispatchHeadNode modeCallNode,
            @Cached("createBinaryProfile()") ConditionProfile raiseProfile) {
        // TODO (pitr 21-Jun-2015): raise on underflow

        final int exceptionConstant = getIntegerConstantNode
                .executeGetIntegerConstant(getBigDecimalClass(), "EXCEPTION_INFINITY");

        final boolean raise = booleanCastNode.executeToBoolean(
                modeCallNode.call(getBigDecimalClass(), "boolean_mode", exceptionConstant));

        if (raiseProfile.profile(raise)) {
            throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
        }

        return createSpecialBigDecimal(type);
    }

    @Specialization(guards = "type == NAN")
    public DynamicObject createNaN(BigDecimalType type, Object digits,
            @Cached("create()") BooleanCastNode booleanCastNode,
            @Cached("create()") GetIntegerConstantNode getIntegerConstantNode,
            @Cached("createPrivate()") CallDispatchHeadNode modeCallNode,
            @Cached("createBinaryProfile()") ConditionProfile raiseProfile) {
        // TODO (pitr 21-Jun-2015): raise on underflow

        final int exceptionConstant = getIntegerConstantNode.executeGetIntegerConstant(getBigDecimalClass(),
                "EXCEPTION_NaN");

        final boolean raise = booleanCastNode.executeToBoolean(
                modeCallNode.call(getBigDecimalClass(), "boolean_mode", exceptionConstant));

        if (raiseProfile.profile(raise)) {
            throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
        }

        return createSpecialBigDecimal(type);
    }

    @Specialization(guards = "type == NEGATIVE_ZERO")
    public DynamicObject createNegativeZero(BigDecimalType type, Object digits) {
        return createSpecialBigDecimal(type);
    }

    @Specialization
    public DynamicObject create(BigDecimal value, NotProvided digits) {
        return create(value, 0);
    }

    @Specialization
    public DynamicObject create(BigDecimal value, int digits) {
        return createNormalBigDecimal(round(value, new MathContext(digits, getRoundMode())));
    }

    @Specialization(guards = "isRubyBignum(value)")
    public DynamicObject createBignum(DynamicObject value, NotProvided digits) {
        return createBignum(value, 0);
    }

    @Specialization(guards = "isRubyBignum(value)")
    public DynamicObject createBignum(DynamicObject value, int digits) {
        return createNormalBigDecimal(
                round(new BigDecimal(Layouts.BIGNUM.getValue(value)), new MathContext(digits, getRoundMode())));
    }

    @Specialization(guards = "isRubyBigDecimal(value)")
    public DynamicObject createBigDecimal(DynamicObject value, NotProvided digits) {
        return createBigDecimal(value, 0);
    }

    @Specialization(guards = "isRubyBigDecimal(value)")
    public DynamicObject createBigDecimal(DynamicObject value, int digits) {
        return createNormalBigDecimal(
                round(Layouts.BIG_DECIMAL.getValue(value), new MathContext(digits, getRoundMode())));
    }

    @Specialization(guards = "isRubyString(value)")
    public DynamicObject createString(DynamicObject value, NotProvided digits) {
        return createString(value, 0);
    }

    @TruffleBoundary
    @Specialization(guards = "isRubyString(value)")
    public DynamicObject createString(DynamicObject value, int digits) {
        return executeCreate(getValueFromString(StringOperations.getString(value), digits), digits);
    }

    @Specialization(guards = {
            "!isRubyBignum(value)",
            "!isRubyBigDecimal(value)",
            "!isRubyString(value)"
    })
    public DynamicObject create(DynamicObject value, int digits,
            @Cached("create()") BigDecimalCastNode bigDecimalCastNode,
            @Cached("createBinaryProfile()") ConditionProfile castProfile) {
        final Object castedValue = bigDecimalCastNode.execute(value, getRoundMode());

        if (castProfile.profile(castedValue instanceof BigDecimal)) {
            return createNormalBigDecimal(round((BigDecimal) castedValue, new MathContext(digits, getRoundMode())));
        } else {
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantBeCastedToBigDecimal(this));
        }
    }

    @TruffleBoundary
    private BigDecimal round(BigDecimal bigDecimal, MathContext context) {
        return bigDecimal.round(context);
    }

    @TruffleBoundary
    private Object getValueFromString(String string, int digits) {
        String strValue = string.trim();

        // TODO (pitr 26-May-2015): create specialization without trims and other cleanups, use rewriteOn

        switch (strValue) {
            case "NaN":
                return BigDecimalType.NAN;
            case "Infinity":
            case "+Infinity":
                return BigDecimalType.POSITIVE_INFINITY;
            case "-Infinity":
                return BigDecimalType.NEGATIVE_INFINITY;
            case "-0":
                return BigDecimalType.NEGATIVE_ZERO;
        }

        // Convert String to Java understandable format (for BigDecimal).
        strValue = strValue.replaceFirst("[dD]", "E");                  // 1. MRI allows d and D as exponent separators
        strValue = strValue.replaceAll("_", "");                        // 2. MRI allows underscores anywhere

        final MatchResult result;
        {
            final Matcher matcher = NUMBER_PATTERN.matcher(strValue);
            strValue = matcher.replaceFirst("$1"); // 3. MRI ignores the trailing junk
            result = matcher.toMatchResult();
        }

        try {
            final BigDecimal value = new BigDecimal(strValue, new MathContext(digits));
            if (value.compareTo(BigDecimal.ZERO) == 0 && strValue.startsWith("-")) {
                return BigDecimalType.NEGATIVE_ZERO;
            } else {
                return value;
            }

        } catch (NumberFormatException e) {
            if (ZERO_PATTERN.matcher(strValue).matches()) {
                return BigDecimal.ZERO;
            }

            final BigInteger exponent = new BigInteger(result.group(3));

            if (exponent.signum() == 1) {
                return BigDecimalType.POSITIVE_INFINITY;
            }

            // TODO (pitr 21-Jun-2015): raise on underflow
            if (exponent.signum() == -1) {
                return BigDecimal.ZERO;
            }

            throw e;
        }
    }

    private DynamicObject createNonFiniteBigDecimal(double value, BranchProfile nanProfile,
            BranchProfile positiveInfinityProfile, BranchProfile negativeInfinityProfile) {
        if (Double.isNaN(value)) {
            nanProfile.enter();
            return createSpecialBigDecimal(BigDecimalType.NAN);
        } else {
            if (value == Double.POSITIVE_INFINITY) {
                positiveInfinityProfile.enter();
                return createSpecialBigDecimal(BigDecimalType.POSITIVE_INFINITY);
            } else {
                negativeInfinityProfile.enter();
                return createSpecialBigDecimal(BigDecimalType.NEGATIVE_INFINITY);
            }
        }
    }

}
