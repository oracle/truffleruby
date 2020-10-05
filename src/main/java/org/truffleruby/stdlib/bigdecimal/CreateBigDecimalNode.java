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
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.numeric.BigDecimalOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "value", type = RubyNode.class)
@NodeChild(value = "digits", type = RubyNode.class)
@NodeChild(value = "strict", type = RubyNode.class)
@ImportStatic(BigDecimalType.class)
public abstract class CreateBigDecimalNode extends BigDecimalCoreMethodNode {

    private static final String EXPONENT = "([eE][+-]?)?(\\d*)";
    private static final Pattern NUMBER_PATTERN_STRICT = Pattern.compile("^([+-]?\\d*\\.?\\d*" + EXPONENT + ")$");
    private static final Pattern NUMBER_PATTERN_NON_STRICT = Pattern.compile("^([+-]?\\d*\\.?\\d*" + EXPONENT + ").*");
    private static final Pattern ZERO_PATTERN = Pattern.compile("^[+-]?0*\\.?0*" + EXPONENT + "$");

    @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

    public abstract RubyBigDecimal executeCreate(Object value, Object digits, boolean strict);

    private RubyBigDecimal createNormalBigDecimal(RubyLanguage language, BigDecimal value) {
        final RubyBigDecimal instance = new RubyBigDecimal(
                coreLibrary().bigDecimalClass,
                RubyLanguage.bigDecimalShape,
                value,
                BigDecimalType.NORMAL);
        allocateNode.trace(instance, this, language);
        return instance;
    }

    private RubyBigDecimal createSpecialBigDecimal(RubyLanguage language, BigDecimalType type) {
        final RubyBigDecimal instance = new RubyBigDecimal(
                coreLibrary().bigDecimalClass,
                RubyLanguage.bigDecimalShape,
                BigDecimal.ZERO,
                type);
        allocateNode.trace(instance, this, language);
        return instance;
    }

    @Specialization
    protected RubyBigDecimal create(long value, NotProvided digits, boolean strict) {
        return executeCreate(value, 0, strict);
    }

    @Specialization
    protected RubyBigDecimal create(long value, int digits, boolean strict,
            @Cached BigDecimalCastNode bigDecimalCastNode,
            @CachedLanguage RubyLanguage language) {
        BigDecimal bigDecimal = round(
                (BigDecimal) bigDecimalCastNode.execute(value, digits, getRoundMode()),
                BigDecimalOps.newMathContext(digits, getRoundMode()));
        return createNormalBigDecimal(language, bigDecimal);
    }

    @Specialization
    protected RubyBigDecimal create(double value, NotProvided digits, boolean strict,
            @Cached ConditionProfile finiteValueProfile,
            @Cached BranchProfile nanProfile,
            @Cached BranchProfile positiveInfinityProfile,
            @Cached BranchProfile negativeInfinityProfile,
            @CachedLanguage RubyLanguage language) {
        if (finiteValueProfile.profile(Double.isFinite(value))) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorCantOmitPrecision(this));
        } else {
            return createNonFiniteBigDecimal(
                    language,
                    value,
                    nanProfile,
                    positiveInfinityProfile,
                    negativeInfinityProfile);
        }
    }

    @Specialization(guards = "isNegativeZero(value)")
    protected RubyBigDecimal createNegativeZero(double value, int digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createSpecialBigDecimal(language, BigDecimalType.NEGATIVE_ZERO);
    }

    @Specialization(guards = { "isFinite(value)", "!isNegativeZero(value)" })
    protected RubyBigDecimal createFinite(double value, int digits, boolean strict,
            @Cached BigDecimalCastNode bigDecimalCastNode,
            @CachedLanguage RubyLanguage language) {
        final RoundingMode roundMode = getRoundMode();
        final BigDecimal bigDecimal = (BigDecimal) bigDecimalCastNode.execute(value, digits, roundMode);
        return createNormalBigDecimal(language, round(bigDecimal, BigDecimalOps.newMathContext(digits, roundMode)));
    }

    @Specialization(guards = "!isFinite(value)")
    protected RubyBigDecimal createInfinite(double value, int digits, boolean strict,
            @Cached BranchProfile nanProfile,
            @Cached BranchProfile positiveInfinityProfile,
            @Cached BranchProfile negativeInfinityProfile,
            @CachedLanguage RubyLanguage language) {
        return createNonFiniteBigDecimal(language, value, nanProfile, positiveInfinityProfile, negativeInfinityProfile);
    }

    @Specialization(guards = "type == NEGATIVE_INFINITY || type == POSITIVE_INFINITY")
    protected RubyBigDecimal createInfinity(BigDecimalType type, Object digits, boolean strict,
            @Cached BooleanCastNode booleanCastNode,
            @Cached GetIntegerConstantNode getIntegerConstantNode,
            @Cached DispatchNode modeCallNode,
            @Cached ConditionProfile raiseProfile,
            @CachedLanguage RubyLanguage language) {
        // TODO (pitr 21-Jun-2015): raise on underflow

        final int exceptionConstant = getIntegerConstantNode
                .executeGetIntegerConstant(getBigDecimalClass(), "EXCEPTION_INFINITY");

        final boolean raise = booleanCastNode.executeToBoolean(
                modeCallNode.call(getBigDecimalClass(), "boolean_mode", exceptionConstant));

        if (raiseProfile.profile(raise)) {
            throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
        }

        return createSpecialBigDecimal(language, type);
    }

    @Specialization(guards = "type == NAN")
    protected RubyBigDecimal createNaN(BigDecimalType type, Object digits, boolean strict,
            @Cached BooleanCastNode booleanCastNode,
            @Cached GetIntegerConstantNode getIntegerConstantNode,
            @Cached DispatchNode modeCallNode,
            @Cached ConditionProfile raiseProfile,
            @CachedLanguage RubyLanguage language) {
        // TODO (pitr 21-Jun-2015): raise on underflow

        final int exceptionConstant = getIntegerConstantNode.executeGetIntegerConstant(
                getBigDecimalClass(),
                "EXCEPTION_NaN");

        final boolean raise = booleanCastNode.executeToBoolean(
                modeCallNode.call(getBigDecimalClass(), "boolean_mode", exceptionConstant));

        if (raiseProfile.profile(raise)) {
            throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
        }

        return createSpecialBigDecimal(language, type);
    }

    @Specialization(guards = "type == NEGATIVE_ZERO")
    protected RubyBigDecimal createNegativeZero(BigDecimalType type, Object digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createSpecialBigDecimal(language, type);
    }

    @Specialization
    protected RubyBigDecimal create(BigDecimal value, NotProvided digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return create(value, 0, strict, language);
    }

    @Specialization
    protected RubyBigDecimal create(BigDecimal value, int digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createNormalBigDecimal(language, round(value, BigDecimalOps.newMathContext(digits, getRoundMode())));
    }

    @Specialization
    protected RubyBigDecimal createBignum(RubyBignum value, NotProvided digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createBignum(value, 0, strict, language);
    }

    @Specialization
    protected RubyBigDecimal createBignum(RubyBignum value, int digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createNormalBigDecimal(
                language,
                round(BigDecimalOps.fromBigInteger(value), BigDecimalOps.newMathContext(digits, getRoundMode())));
    }

    @Specialization
    protected RubyBigDecimal createBigDecimal(RubyBigDecimal value, NotProvided digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createBigDecimal(value, 0, strict);
    }

    @Specialization
    protected RubyBigDecimal createBigDecimal(RubyBigDecimal value, int digits, boolean strict,
            @CachedLanguage RubyLanguage language) {
        return createNormalBigDecimal(
                language,
                round(value.value, BigDecimalOps.newMathContext(digits, getRoundMode())));
    }

    @Specialization
    protected RubyBigDecimal createString(RubyString value, NotProvided digits, boolean strict) {
        return createString(value, 0, strict);
    }

    @TruffleBoundary
    @Specialization
    protected RubyBigDecimal createString(RubyString value, int digits, boolean strict) {
        return executeCreate(getValueFromString(value.getJavaString(), digits, strict), digits, strict);
    }

    @Specialization(guards = { "!isRubyBignum(value)", "!isRubyBigDecimal(value)", "!isRubyString(value)" })
    protected RubyBigDecimal create(RubyDynamicObject value, int digits, boolean strict,
            @Cached BigDecimalCastNode bigDecimalCastNode,
            @Cached ConditionProfile castProfile,
            @CachedLanguage RubyLanguage language) {
        final Object castedValue = bigDecimalCastNode.execute(value, digits, getRoundMode());

        if (castProfile.profile(castedValue instanceof BigDecimal)) {
            return createNormalBigDecimal(language, (BigDecimal) castedValue);
        } else {
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantBeCastedToBigDecimal(this));
        }
    }

    @TruffleBoundary
    private BigDecimal round(BigDecimal bigDecimal, MathContext context) {
        return bigDecimal.round(context);
    }

    @TruffleBoundary
    private Object getValueFromString(String string, int digits, boolean strict) {
        String strValue = string;

        strValue = strValue.replaceFirst("^\\s+", "");
        strValue = strValue.replaceFirst("\\s+$", "");

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
        strValue = strValue.replaceFirst("[dD]", "E");                  // MRI allows d and D as exponent separators

        // 1. MRI allows _ before the decimal place
        if (strValue.indexOf('_') != -1) {
            final StringBuilder builder = new StringBuilder(strValue.length());

            for (int n = 0; n < strValue.length(); n++) {
                final char c = strValue.charAt(n);

                if (c == '.') {
                    builder.append(strValue.substring(n));
                    break;
                } else if (c != '_') {
                    builder.append(c);
                }
            }

            strValue = builder.toString();
        }

        final Matcher matcher = (strict ? NUMBER_PATTERN_STRICT : NUMBER_PATTERN_NON_STRICT).matcher(strValue);

        if (!matcher.matches()) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidBigDecimal(string, this));
        }

        final MatchResult result = matcher.toMatchResult();
        strValue = matcher.replaceFirst("$1");

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

            final String exponentPart = result.group(3);
            final BigInteger exponent = new BigInteger(exponentPart);

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

    private RubyBigDecimal createNonFiniteBigDecimal(RubyLanguage language, double value, BranchProfile nanProfile,
            BranchProfile positiveInfinityProfile, BranchProfile negativeInfinityProfile) {
        if (Double.isNaN(value)) {
            nanProfile.enter();
            return createSpecialBigDecimal(language, BigDecimalType.NAN);
        } else {
            if (value == Double.POSITIVE_INFINITY) {
                positiveInfinityProfile.enter();
                return createSpecialBigDecimal(language, BigDecimalType.POSITIVE_INFINITY);
            } else {
                negativeInfinityProfile.enter();
                return createSpecialBigDecimal(language, BigDecimalType.NEGATIVE_INFINITY);
            }
        }
    }

}
