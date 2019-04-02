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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NodeChild(value = "value", type = RubyNode.class)
@NodeChild(value = "self", type = RubyNode.class)
@NodeChild(value = "digits", type = RubyNode.class)
@ImportStatic(BigDecimalType.class)
public abstract class CreateBigDecimalNode extends BigDecimalCoreMethodNode {

    private static final String exponent = "([eE][+-]?)?(\\d*)";
    private final static Pattern NUMBER_PATTERN = Pattern.compile("^([+-]?\\d*\\.?\\d*" + exponent + ").*");
    private final static Pattern ZERO_PATTERN = Pattern.compile("^[+-]?0*\\.?0*" + exponent);

    @Child private CallDispatchHeadNode allocateNode;

    public abstract DynamicObject executeInitialize(
            Object value,
            DynamicObject alreadyAllocatedSelf,
            Object digits);

    public final DynamicObject executeCreate(Object value) {
        if (allocateNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            allocateNode = insert(CallDispatchHeadNode.createPrivate());
        }

        return executeInitialize(
                value,
                (DynamicObject) allocateNode.call(getBigDecimalClass(), "__allocate__"),
                NotProvided.INSTANCE);
    }

    @Specialization
    public DynamicObject create(long value, DynamicObject self, NotProvided digits) {
        return executeInitialize(value, self, 0);
    }

    @Specialization
    public DynamicObject create(long value, DynamicObject self, int digits,
            @Cached("createBigDecimalCastNode()") BigDecimalCastNode bigDecimalCastNode) {
        Layouts.BIG_DECIMAL.setValue(self,
                round(bigDecimalCastNode.executeBigDecimal(value, getRoundMode()), new MathContext(digits, getRoundMode())));
        return self;
    }

    @TruffleBoundary
    public BigDecimal round(BigDecimal bigDecimal, MathContext context) {
        return bigDecimal.round(context);
    }

    @Specialization
    public DynamicObject create(double value, DynamicObject self, NotProvided digits) {
        throw new RaiseException(getContext(), coreExceptions().argumentErrorCantOmitPrecision(this));
    }

    @Specialization
    public DynamicObject create(double value, DynamicObject self, int digits,
            @Cached("createBigDecimalCastNode()") BigDecimalCastNode bigDecimalCastNode) {
        Layouts.BIG_DECIMAL.setValue(self, bigDecimalCastNode
                .executeBigDecimal(value, getRoundMode())
                .round(new MathContext(digits, getRoundMode())));
        return self;
    }

    @Specialization(guards = "value == NEGATIVE_INFINITY || value == POSITIVE_INFINITY")
    public DynamicObject createInfinity(BigDecimalType value, DynamicObject self, Object digits,
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

        Layouts.BIG_DECIMAL.setType(self, value);
        return self;
    }

    @Specialization(guards = "value == NAN")
    public DynamicObject createNaN(BigDecimalType value, DynamicObject self, Object digits,
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

        Layouts.BIG_DECIMAL.setType(self, value);
        return self;
    }

    @Specialization(guards = "value == NEGATIVE_ZERO")
    public DynamicObject createNegativeZero(BigDecimalType value, DynamicObject self, Object digits) {
        Layouts.BIG_DECIMAL.setType(self, value);
        return self;
    }

    @Specialization
    public DynamicObject create(BigDecimal value, DynamicObject self, NotProvided digits) {
        return create(value, self, 0);
    }

    @Specialization
    public DynamicObject create(BigDecimal value, DynamicObject self, int digits) {
        Layouts.BIG_DECIMAL.setValue(self, round(value, new MathContext(digits, getRoundMode())));
        return self;
    }

    @Specialization(guards = "isRubyBignum(value)")
    public DynamicObject createBignum(DynamicObject value, DynamicObject self, NotProvided digits) {
        return createBignum(value, self, 0);
    }

    @Specialization(guards = "isRubyBignum(value)")
    public DynamicObject createBignum(DynamicObject value, DynamicObject self, int digits) {
        Layouts.BIG_DECIMAL.setValue(self,
                round(new BigDecimal(Layouts.BIGNUM.getValue(value)), new MathContext(digits, getRoundMode())));
        return self;
    }

    @Specialization(guards = "isRubyBigDecimal(value)")
    public DynamicObject createBigDecimal(DynamicObject value, DynamicObject self, NotProvided digits) {
        return createBigDecimal(value, self, 0);
    }

    @Specialization(guards = "isRubyBigDecimal(value)")
    public DynamicObject createBigDecimal(DynamicObject value, DynamicObject self, int digits) {
        Layouts.BIG_DECIMAL.setValue(self,
                round(Layouts.BIG_DECIMAL.getValue(value), new MathContext(digits, getRoundMode())));
        return self;
    }

    @Specialization(guards = "isRubyString(value)")
    public DynamicObject createString(DynamicObject value, DynamicObject self, NotProvided digits) {
        return createString(value, self, 0);
    }

    @TruffleBoundary
    @Specialization(guards = "isRubyString(value)")
    public DynamicObject createString(DynamicObject value, DynamicObject self, int digits) {
        return executeInitialize(getValueFromString(StringOperations.getString(value), digits), self, digits);
    }

    @Specialization(guards = {
            "!isRubyBignum(value)",
            "!isRubyBigDecimal(value)",
            "!isRubyString(value)"
    })
    public DynamicObject create(DynamicObject value, DynamicObject self, int digits,
            @Cached("createBigDecimalCastNode()") BigDecimalCastNode bigDecimalCastNode,
            @Cached("createBinaryProfile()") ConditionProfile nilProfile) {
        final Object castedValue = bigDecimalCastNode.executeObject(value, getRoundMode());

        if (nilProfile.profile(castedValue == nil())) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantBeCastedToBigDecimal(this));
        }

        Layouts.BIG_DECIMAL.setValue(self, round(((BigDecimal) castedValue), new MathContext(digits, getRoundMode())));

        return self;
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

    protected BigDecimalCastNode createBigDecimalCastNode() {
        return BigDecimalCastNodeGen.create(null, null);
    }

}
