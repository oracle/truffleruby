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
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.parser.SafeDoubleParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@CoreClass("BigDecimal")
public abstract class BigDecimalNodes {

    // TODO (pitr 2015-jun-16): lazy setup when required, see https://github.com/jruby/jruby/pull/3048#discussion_r32413656

    // TODO (pitr 21-Jun-2015): Check for missing coerce on OpNodes

    @Primitive(name = "bigdecimal_new", lowerFixnum = 1)
    public abstract static class NewNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization
        public Object newBigDecimal(Object value, NotProvided digits) {
            return createBigDecimal(value);
        }

        @Specialization
        public Object newBigDecimal(Object value, int digits) {
            return createBigDecimal(value, digits);
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddOpNode extends AbstractAddNode {

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object add(DynamicObject a, DynamicObject b) {
            return add(a, b, getLimit());
        }

        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object addSpecial(DynamicObject a, DynamicObject b) {
            return addSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object addCoerced(DynamicObject a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
        }
    }

    @CoreMethod(names = "add", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class AddNode extends AbstractAddNode {

        @Override
        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        protected Object add(DynamicObject a, DynamicObject b, int precision) {
            return super.add(a, b, precision);
        }

        @Override
        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        protected Object addSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.addSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object addCoerced(DynamicObject a, Object b, int precision,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("add"), b, precision);
        }
    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubOpNode extends AbstractSubNode {

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object subNormal(DynamicObject a, DynamicObject b) {
            return subNormal(a, b, getLimit());
        }

        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object subSpecial(DynamicObject a, DynamicObject b) {
            return subSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object subCoerced(DynamicObject a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
        }
    }

    @CoreMethod(names = "sub", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class SubNode extends AbstractSubNode {

        @Override
        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object subNormal(DynamicObject a, DynamicObject b, int precision) {
            return super.subNormal(a, b, precision);
        }

        @Override
        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object subSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.subSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object subCoerced(DynamicObject a, Object b, int precision,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("sub"), b, precision);
        }
    }

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = {
                "isNormal(value)",
                "!isNormalZero(value)"
        })
        public Object negNormal(DynamicObject value) {
            return createBigDecimal(Layouts.BIG_DECIMAL.getValue(value).negate());
        }

        @Specialization(guards = {
                "isNormal(value)",
                "isNormalZero(value)"
        })
        public Object negNormalZero(DynamicObject value) {
            return createBigDecimal(BigDecimalType.NEGATIVE_ZERO);
        }

        @Specialization(guards = "!isNormal(value)")
        public Object negSpecial(DynamicObject value,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile,
                @Cached("createBinaryProfile()") ConditionProfile negZeroProfile,
                @Cached("createBinaryProfile()") ConditionProfile infProfile) {
            final BigDecimalType type = Layouts.BIG_DECIMAL.getType(value);

            if (nanProfile.profile(type == BigDecimalType.NAN)) {
                return value;
            }

            if (negZeroProfile.profile(type == BigDecimalType.NEGATIVE_ZERO)) {
                return createBigDecimal(BigDecimal.ZERO);
            }

            final BigDecimalType resultType;

            if (infProfile.profile(type == BigDecimalType.NEGATIVE_INFINITY)) {
                resultType = BigDecimalType.POSITIVE_INFINITY;
            } else {
                resultType = BigDecimalType.NEGATIVE_INFINITY;
            }

            return createBigDecimal(resultType);
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MultOpNode extends AbstractMultNode {

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object mult(DynamicObject a, DynamicObject b) {
            return mult(a, b, getLimit());
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object multNormalSpecial(DynamicObject a, DynamicObject b) {
            return multSpecialNormal(b, a, 0);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object multSpecialNormal(DynamicObject a, DynamicObject b) {
            return multSpecialNormal(a, b, 0);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object multSpecial(DynamicObject a, DynamicObject b) {
            return multSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object multCoerced(DynamicObject a, Object b,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
        }
    }

    @CoreMethod(names = "mult", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class MultNode extends AbstractMultNode {

        @Override
        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object mult(DynamicObject a, DynamicObject b, int precision) {
            return super.mult(a, b, precision);
        }

        @Override
        @Specialization(guards = {
                "isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object multNormalSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.multNormalSpecial(a, b, precision);
        }

        @Override
        @Specialization(guards = {
                "!isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object multSpecialNormal(DynamicObject a, DynamicObject b, int precision) {
            return super.multSpecialNormal(a, b, precision);
        }

        @Override
        @Specialization(guards = {
                "!isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object multSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.multSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object multCoerced(DynamicObject a, Object b, int precision,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("mult"), b, precision);
        }
    }

    @CoreMethod(names = { "/", "quo" }, required = 1)
    public abstract static class DivOpNode extends AbstractDivNode {

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object div(DynamicObject a, DynamicObject b) {
            final int precision = defaultDivisionPrecision(Layouts.BIG_DECIMAL.getValue(a), Layouts.BIG_DECIMAL.getValue(b), getLimit());
            return div(a, b, precision);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divNormalSpecial(DynamicObject a, DynamicObject b) {
            return divNormalSpecial(a, b, 0);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object divSpecialNormal(DynamicObject a, DynamicObject b) {
            return divSpecialNormal(a, b, 0);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divSpecialSpecial(DynamicObject a, DynamicObject b) {
            return divSpecialSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object divCoerced(DynamicObject a, Object b,
                                  @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
        }
    }

    @CoreMethod(names = "div", required = 1, optional = 1, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class DivNode extends AbstractDivNode {

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object div(DynamicObject a, DynamicObject b, NotProvided precision,
                @Cached("createBinaryProfile()") ConditionProfile bZeroProfile,
                @Cached("createPrivate()") CallDispatchHeadNode floorNode) {
            if (bZeroProfile.profile(isNormalZero(b))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                final Object result = div(a, b, 0);
                return floorNode.call(result, "floor");
            }
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object div(DynamicObject a, DynamicObject b, int precision,
                @Cached("createBinaryProfile()") ConditionProfile zeroPrecisionProfile) {
            final int newPrecision;

            if (zeroPrecisionProfile.profile(precision == 0)) {
                newPrecision = defaultDivisionPrecision(Layouts.BIG_DECIMAL.getValue(a), Layouts.BIG_DECIMAL.getValue(b), getLimit());
            } else {
                newPrecision = precision;
            }

            return super.div(a, b, newPrecision);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divNormalSpecial(DynamicObject a, DynamicObject b, NotProvided precision,
                @Cached("createBinaryProfile()") ConditionProfile negativeZeroProfile,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile) {
            if (negativeZeroProfile.profile(Layouts.BIG_DECIMAL.getType(b) == BigDecimalType.NEGATIVE_ZERO)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(Layouts.BIG_DECIMAL.getType(b) == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else {
                return divNormalSpecial(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = {
                "isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divNormalSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.divNormalSpecial(a, b, precision);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object divSpecialNormal(DynamicObject a, DynamicObject b, NotProvided precision,
                @Cached("createBinaryProfile()") ConditionProfile zeroDivisionProfile,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile,
                @Cached("createBinaryProfile()") ConditionProfile infinityProfile) {
            if (zeroDivisionProfile.profile(isNormalZero(b))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(Layouts.BIG_DECIMAL.getType(a) == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else if (infinityProfile.profile(
                    Layouts.BIG_DECIMAL.getType(a) == BigDecimalType.POSITIVE_INFINITY
                            || Layouts.BIG_DECIMAL.getType(a) == BigDecimalType.NEGATIVE_INFINITY)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
            } else {
                return divSpecialNormal(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = {
                "!isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public Object divSpecialNormal(DynamicObject a, DynamicObject b, int precision) {
            return super.divSpecialNormal(a, b, precision);
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divSpecialSpecial(DynamicObject a, DynamicObject b, NotProvided precision,
                @Cached("createBinaryProfile()") ConditionProfile negZeroProfile,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile) {
            if (negZeroProfile.profile(Layouts.BIG_DECIMAL.getType(b) == BigDecimalType.NEGATIVE_ZERO)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(
                    Layouts.BIG_DECIMAL.getType(a) == BigDecimalType.NAN
                            || Layouts.BIG_DECIMAL.getType(b) == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else {
                return divSpecialSpecial(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = {
                "!isNormal(a)",
                "isSpecialRubyBigDecimal(b)"
        })
        public Object divSpecialSpecial(DynamicObject a, DynamicObject b, int precision) {
            return super.divSpecialSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object divCoerced(DynamicObject a, Object b, NotProvided precision,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("div"), b);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object divCoerced(DynamicObject a, Object b, int precision,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("div"), b, precision);
        }
    }

    @CoreMethod(names = "divmod", required = 1)
    public abstract static class DivModNode extends BigDecimalOpNode {

        @TruffleBoundary
        private BigDecimal[] divmodBigDecimal(BigDecimal a, BigDecimal b) {
            final BigDecimal[] result = a.divideAndRemainder(b);

            if (result[1].signum() * b.signum() < 0) {
                result[0] = result[0].subtract(BigDecimal.ONE);
                result[1] = result[1].add(b);
            }

            return result;
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "!isNormalZero(a)",
                "!isNormalZero(b)"
        })
        public Object divmod(DynamicObject a, DynamicObject b) {
            final BigDecimal[] result = divmodBigDecimal(Layouts.BIG_DECIMAL.getValue(a), Layouts.BIG_DECIMAL.getValue(b));
            final Object[] store = new Object[]{ createBigDecimal(result[0]), createBigDecimal(result[1]) };
            return createArray(store, store.length);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "isNormalZero(a)",
                "!isNormalZero(b)"
        })
        public Object divmodZeroDividend(DynamicObject a, DynamicObject b) {
            final Object[] store = new Object[]{ createBigDecimal(BigDecimal.ZERO), createBigDecimal(BigDecimal.ZERO) };
            return createArray(store, store.length);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "isNormalZero(b)"
        })
        public Object divmodZeroDivisor(DynamicObject a, DynamicObject b) {
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object divmodSpecial(DynamicObject a, DynamicObject b,
                @Cached("createPrivate()") CallDispatchHeadNode signCall,
                @Cached("create()") IntegerCastNode signIntegerCast,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile,
                @Cached("createBinaryProfile()") ConditionProfile normalNegProfile,
                @Cached("createBinaryProfile()") ConditionProfile negNormalProfile,
                @Cached("createBinaryProfile()") ConditionProfile infinityProfile) {
            final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
            final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

            if (nanProfile.profile(aType == BigDecimalType.NAN || bType == BigDecimalType.NAN)) {
                final Object[] store = new Object[]{ createBigDecimal(BigDecimalType.NAN), createBigDecimal(BigDecimalType.NAN) };
                return createArray(store, store.length);
            }

            if (nanProfile.profile(bType == BigDecimalType.NEGATIVE_ZERO || (bType == BigDecimalType.NORMAL && isNormalZero(b)))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }

            if (normalNegProfile.profile(aType == BigDecimalType.NEGATIVE_ZERO || (aType == BigDecimalType.NORMAL && isNormalZero(a)))) {
                final Object[] store = new Object[]{ createBigDecimal(BigDecimal.ZERO), createBigDecimal(BigDecimal.ZERO) };
                return createArray(store, store.length);
            }

            if (negNormalProfile.profile(aType == BigDecimalType.POSITIVE_INFINITY || aType == BigDecimalType.NEGATIVE_INFINITY)) {
                final int signA = aType == BigDecimalType.POSITIVE_INFINITY ? 1 : -1;
                final int signB = Integer.signum(signIntegerCast.executeCastInt(signCall.call(b, "sign")));
                final int sign = signA * signB; // is between -1 and 1, 0 when nan

                final BigDecimalType type = new BigDecimalType[]{ BigDecimalType.NEGATIVE_INFINITY, BigDecimalType.NAN, BigDecimalType.POSITIVE_INFINITY }[sign + 1];

                final Object[] store = new Object[]{ createBigDecimal(type), createBigDecimal(BigDecimalType.NAN) };
                return createArray(store, store.length);
            }

            if (infinityProfile.profile(bType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY)) {
                final Object[] store = new Object[]{ createBigDecimal(BigDecimal.ZERO), createBigDecimal(a) };
                return createArray(store, store.length);
            }

            throw new UnsupportedOperationException("unreachable code branch");
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object divmodCoerced(DynamicObject a, Object b,
                                 @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("divmod"), b);
        }

    }

    @CoreMethod(names = "remainder", required = 1)
    public abstract static class RemainderNode extends BigDecimalOpNode {

        @TruffleBoundary
        public static BigDecimal remainderBigDecimal(BigDecimal a, BigDecimal b) {
            return a.remainder(b);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "!isNormalZero(b)"
        })
        public Object remainder(DynamicObject a, DynamicObject b) {
            return createBigDecimal(remainderBigDecimal(Layouts.BIG_DECIMAL.getValue(a), Layouts.BIG_DECIMAL.getValue(b)));
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "isNormalZero(b)"
        })
        public Object remainderZero(DynamicObject a, DynamicObject b) {
            return createBigDecimal(BigDecimalType.NAN);
        }

        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object remainderSpecial(DynamicObject a, DynamicObject b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
            final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

            if (zeroProfile.profile(aType == BigDecimalType.NEGATIVE_ZERO && bType == BigDecimalType.NORMAL)) {
                return createBigDecimal(BigDecimal.ZERO);
            } else {
                return createBigDecimal(BigDecimalType.NAN);
            }
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object remainderCoerced(DynamicObject a, Object b,
                                    @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("remainder"), b);
        }
    }

    @CoreMethod(names = { "modulo", "%" }, required = 1)
    public abstract static class ModuloNode extends BigDecimalOpNode {

        @TruffleBoundary
        public static BigDecimal moduloBigDecimal(BigDecimal a, BigDecimal b) {
            final BigDecimal modulo = a.remainder(b);

            if (modulo.signum() * b.signum() < 0) {
                return modulo.add(b);
            } else {
                return modulo;
            }
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "!isNormalZero(b)"
        })
        public Object modulo(DynamicObject a, DynamicObject b) {
            return createBigDecimal(moduloBigDecimal(Layouts.BIG_DECIMAL.getValue(a), Layouts.BIG_DECIMAL.getValue(b)));
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)",
                "isNormalZero(b)"
        })
        public Object moduloZero(DynamicObject a, DynamicObject b) {
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)"
        })
        public Object moduloSpecial(DynamicObject a, DynamicObject b,
                @Cached("createBinaryProfile()") ConditionProfile nanProfile,
                @Cached("createBinaryProfile()") ConditionProfile normalNegProfile,
                @Cached("createBinaryProfile()") ConditionProfile negNormalProfile,
                @Cached("createBinaryProfile()") ConditionProfile posNegInfProfile,
                @Cached("createBinaryProfile()") ConditionProfile negPosInfProfile) {
            final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
            final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

            if (nanProfile.profile(aType == BigDecimalType.NAN
                    || bType == BigDecimalType.NAN)) {
                return createBigDecimal(BigDecimalType.NAN);
            }

            if (normalNegProfile.profile(bType == BigDecimalType.NEGATIVE_ZERO
                    || (bType == BigDecimalType.NORMAL && isNormalZero(b)))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }

            if (negNormalProfile.profile(aType == BigDecimalType.NEGATIVE_ZERO
                    || (aType == BigDecimalType.NORMAL && isNormalZero(a)))) {
                return createBigDecimal(BigDecimal.ZERO);
            }

            if (posNegInfProfile.profile(aType == BigDecimalType.POSITIVE_INFINITY
                    || aType == BigDecimalType.NEGATIVE_INFINITY)) {
                return createBigDecimal(BigDecimalType.NAN);
            }

            if (negPosInfProfile.profile(bType == BigDecimalType.POSITIVE_INFINITY
                    || bType == BigDecimalType.NEGATIVE_INFINITY)) {
                return createBigDecimal(a);
            }

            throw new UnsupportedOperationException("unreachable code branch");
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object moduloCoerced(DynamicObject a, Object b,
                                       @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("modulo"), b);
        }
    }

    @CoreMethod(names = { "**", "power" }, required = 1, optional = 1, lowerFixnum = { 1, 2 })
    public abstract static class PowerNode extends BigDecimalCoreMethodArrayArgumentsNode {

        public abstract Object executePower(Object a, Object exponent, Object precision);

        @TruffleBoundary
        private BigDecimal power(BigDecimal value, int exponent, MathContext mathContext) {
            return value.pow(exponent, mathContext);
        }

        @TruffleBoundary
        private int getDigits(BigDecimal value) {
            return value.abs().unscaledValue().toString().length();
        }

        @Specialization(guards = "isNormal(a)")
        public Object power(DynamicObject a, int exponent, NotProvided precision) {
            return executePower(a, exponent, getLimit());
        }

        @Specialization(guards = "isNormal(a)")
        public Object power(DynamicObject a, int exponent, int precision,
                @Cached("createBinaryProfile()") ConditionProfile positiveExponentProfile,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile,
                @Cached("createBinaryProfile()") ConditionProfile zeroExponentProfile) {
            final BigDecimal aBigDecimal = Layouts.BIG_DECIMAL.getValue(a);
            final boolean positiveExponent = positiveExponentProfile.profile(exponent >= 0);

            if (zeroProfile.profile(aBigDecimal.compareTo(BigDecimal.ZERO) == 0)) {
                final Object value;

                if (positiveExponent) {
                    if (zeroExponentProfile.profile(exponent == 0)) {
                        value = BigDecimal.ONE;
                    } else {
                        value = BigDecimal.ZERO;
                    }
                } else {
                    value = BigDecimalType.POSITIVE_INFINITY;
                }

                return createBigDecimal(value);
            } else {
                final int newPrecision;

                if (positiveExponent) {
                    newPrecision = precision;
                } else {
                    newPrecision = (-exponent + 4) * (getDigits(aBigDecimal) + 4);
                }

                return createBigDecimal(power(Layouts.BIG_DECIMAL.getValue(a), exponent,
                        new MathContext(newPrecision, getRoundMode())));
            }
        }

        @Specialization(guards = "!isNormal(a)")
        public Object power(DynamicObject a, int exponent, Object unusedPrecision,
                @Cached("create()") BranchProfile nanProfile,
                @Cached("create()") BranchProfile posInfinityProfile,
                @Cached("create()") BranchProfile negInfinityProfile,
                @Cached("create()") BranchProfile negZeroProfile) {
            final Object value;

            switch (Layouts.BIG_DECIMAL.getType(a)) {
                case NAN:
                    nanProfile.enter();
                    value = BigDecimalType.NAN;
                    break;
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    value = exponent >= 0 ? BigDecimalType.POSITIVE_INFINITY : BigDecimal.ZERO;
                    break;
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    value = Integer.signum(exponent) == 1
                            ? (exponent % 2 == 0
                                ? BigDecimalType.POSITIVE_INFINITY
                                : BigDecimalType.NEGATIVE_INFINITY)
                            : BigDecimal.ZERO;
                    break;
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    value = Integer.signum(exponent) == 1 ? BigDecimal.ZERO : BigDecimalType.NAN;
                    break;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(a));
            }

            return createBigDecimal(value);
        }
    }

    @CoreMethod(names = "sqrt", required = 1, lowerFixnum = 1)
    public abstract static class SqrtNode extends BigDecimalCoreMethodArrayArgumentsNode {

        public abstract Object executeSqrt(DynamicObject value, int precision);

        private BigDecimal sqrt(BigDecimal value, MathContext mathContext) {
            return bigSqrt(value, mathContext);
        }

        private static final BigDecimal TWO = new BigDecimal(2);
        private static final double SQRT_10 = 3.162277660168379332;

        @TruffleBoundary
        private static BigDecimal bigSqrt(BigDecimal squarD, MathContext rootMC) {
            // General number and precision checking
            int sign = squarD.signum();
            if (sign == -1) {
                throw new ArithmeticException("Square root of a negative number: " + squarD);
            }
            if (sign == 0) {
                return squarD.round(rootMC);
            }

            int prec = rootMC.getPrecision();           // the requested precision
            if (prec == 0) {
                throw new IllegalArgumentException("Most roots won't have infinite precision = 0");
            }

            // Initial precision is that of double numbers 2^63/2 ~ 4E18
            int BITS = 62;                              // 63-1 an even number of number bits
            int nInit = 16;                             // precision seems 16 to 18 digits
            MathContext nMC = new MathContext(18, RoundingMode.HALF_DOWN);

            // Estimate the square root with the foremost 62 bits of squarD
            BigInteger bi = squarD.unscaledValue();     // bi and scale are a tandem
            int biLen = bi.bitLength();
            int shift = Math.max(0, biLen - BITS + (biLen % 2 == 0 ? 0 : 1)); // even shift..
            bi = bi.shiftRight(shift);                  // ..floors to 62 or 63 bit BigInteger

            double root = Math.sqrt(SafeDoubleParser.doubleValue(bi));
            BigDecimal halfBack = new BigDecimal(BigInteger.ONE.shiftLeft(shift / 2));

            int scale = squarD.scale();
            assert scale >= 0 : "unexpected negative scale";
            if (scale % 2 != 0) {
                root *= SQRT_10; // 5 -> 2, -5 -> -3 need half a scale more..
            }

            scale = (int) Math.ceil(scale / 2.); // ..where 100 -> 10 shifts the scale

            // Initial x - use double root - multiply by halfBack to unshift - set new scale
            BigDecimal x = new BigDecimal(root, nMC);
            x = x.multiply(halfBack, nMC);              // x0 ~ sqrt()
            if (scale != 0) {
                x = x.movePointLeft(scale);
            }

            if (prec < nInit) {                // for prec 15 root x0 must surely be OK
                return x.round(rootMC);        // return small prec roots without iterations
            }

            // Initial v - the reciprocal
            BigDecimal v = BigDecimal.ONE.divide(TWO.multiply(x), nMC);        // v0 = 1/(2*x)

            // Collect iteration precisions beforehand
            List<Integer> nPrecs = new ArrayList<>();

            assert nInit > 3 : "Never ending loop!";                // assume nInit = 16 <= prec

            // Let m be the exact digits precision in an earlier! loop
            for (int m = prec + 1; m > nInit; m = m / 2 + (m > 100 ? 1 : 2)) {
                nPrecs.add(m);
            }

            // The loop of "Square Root by Coupled Newton Iteration"
            for (int i = nPrecs.size() - 1; i > -1; i--) {
                // Increase precision - next iteration supplies n exact digits
                nMC = new MathContext(nPrecs.get(i), i % 2 != 0 ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN);

                // Next x                                        // e = d - x^2
                BigDecimal e = squarD.subtract(x.multiply(x, nMC), nMC);
                if (i != 0) {
                    x = x.add(e.multiply(v, nMC));               // x += e*v     ~ sqrt()
                } else {
                    x = x.add(e.multiply(v, rootMC), rootMC);    // root x is ready!
                    break;
                }

                // Next v                                        // g = 1 - 2*x*v
                BigDecimal g = BigDecimal.ONE.subtract(TWO.multiply(x).multiply(v, nMC));

                v = v.add(g.multiply(v, nMC));                   // v += g*v     ~ 1/2/sqrt()
            }

            return x;                      // return sqrt(squarD) with precision of rootMC
        }

        @Specialization(guards = "precision < 0")
        public Object sqrtNegativePrecision(DynamicObject a, int precision) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("precision must be positive", this));
        }

        @Specialization(guards = "precision == 0")
        public Object sqrtZeroPrecision(DynamicObject a, int precision) {
            return executeSqrt(a, 1);
        }

        @Specialization(guards = {
                "isNormal(a)",
                "precision > 0"
        })
        public Object sqrt(DynamicObject a, int precision,
                @Cached("createBinaryProfile()") ConditionProfile positiveValueProfile) {
            final BigDecimal valueBigDecimal = Layouts.BIG_DECIMAL.getValue(a);
            if (positiveValueProfile.profile(valueBigDecimal.signum() >= 0)) {
                return createBigDecimal(sqrt(valueBigDecimal, new MathContext(precision, getRoundMode())));
            } else {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorSqrtNegative(this));
            }
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "precision > 0"
        })
        public Object sqrtSpecial(DynamicObject a, int precision,
                @Cached("create()") BranchProfile nanProfile,
                @Cached("create()") BranchProfile posInfProfile,
                @Cached("create()") BranchProfile negInfProfile,
                @Cached("create()") BranchProfile negZeroProfile) {
            switch (Layouts.BIG_DECIMAL.getType(a)) {
                case NAN:
                    nanProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorSqrtNegative(this));
                case POSITIVE_INFINITY:
                    posInfProfile.enter();
                    return createBigDecimal(BigDecimalType.POSITIVE_INFINITY);
                case NEGATIVE_INFINITY:
                    negInfProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorSqrtNegative(this));
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return createBigDecimal(sqrt(BigDecimal.ZERO, new MathContext(precision, getRoundMode())));
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(a));
            }
        }
    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        private int compareBigDecimal(DynamicObject a, BigDecimal b) {
            return Layouts.BIG_DECIMAL.getValue(a).compareTo(b);
        }

        @Specialization(guards = "isNormal(a)")
        public int compare(DynamicObject a, long b) {
            return compareBigDecimal(a, BigDecimal.valueOf(b));
        }

        @Specialization(guards = "isNormal(a)")
        public int compare(DynamicObject a, double b) {
            return compareBigDecimal(a, valueOf(b));
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isRubyBignum(b)"
        })
        public int compare(DynamicObject a, DynamicObject b) {
            return compareBigDecimal(a, new BigDecimal(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = {
                "isNormal(a)",
                "isNormalRubyBigDecimal(b)"
        })
        public int compareNormal(DynamicObject a, DynamicObject b) {
            return compareBigDecimal(a, Layouts.BIG_DECIMAL.getValue(b));
        }

        @Specialization(guards = "!isNormal(a)")
        public Object compareSpecial(DynamicObject a, long b) {
            return compareSpecial(a, createBigDecimal(BigDecimal.valueOf(b)));
        }

        @Specialization(guards = "!isNormal(a)")
        public Object compareSpecial(DynamicObject a, double b) {
            return compareSpecial(a, createBigDecimal(valueOf(b)));
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isRubyBignum(b)"
        })
        public Object compareSpecialBignum(DynamicObject a, DynamicObject b) {
            return compareSpecial(a, createBigDecimal(new BigDecimal(Layouts.BIGNUM.getValue(b))));
        }

        @Specialization(guards = {
                "!isNormal(a)",
                "isNan(a)"
        })
        public Object compareSpecialNan(DynamicObject a, DynamicObject b) {
            return nil();
        }

        @TruffleBoundary
        @Specialization(guards = {
                "isRubyBigDecimal(b)",
                "!isNormal(a) || !isNormal(b)",
                "isNormal(a) || !isNan(a)" })
        public Object compareSpecial(DynamicObject a, DynamicObject b) {
            final BigDecimalType aType = Layouts.BIG_DECIMAL.getType(a);
            final BigDecimalType bType = Layouts.BIG_DECIMAL.getType(b);

            if (aType == BigDecimalType.NAN || bType == BigDecimalType.NAN) {
                return nil();
            }
            if (aType == bType) {
                return 0;
            }
            if (aType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY) {
                return 1;
            }
            if (aType == BigDecimalType.NEGATIVE_INFINITY || bType == BigDecimalType.POSITIVE_INFINITY) {
                return -1;
            }

            // a and b have finite value

            final BigDecimal aCompare;
            final BigDecimal bCompare;

            if (aType == BigDecimalType.NEGATIVE_ZERO) {
                aCompare = BigDecimal.ZERO;
            } else {
                aCompare = Layouts.BIG_DECIMAL.getValue(a);
            }
            if (bType == BigDecimalType.NEGATIVE_ZERO) {
                bCompare = BigDecimal.ZERO;
            } else {
                bCompare = Layouts.BIG_DECIMAL.getValue(b);
            }

            return aCompare.compareTo(bCompare);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        public Object compareCoerced(DynamicObject a, DynamicObject b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare_no_error", b);
        }

        @TruffleBoundary
        private BigDecimal valueOf(double val) {
            return BigDecimal.valueOf(val);
        }

    }

    // TODO (pitr 20-May-2015): compare Ruby implementation of #== with a Java one

    @CoreMethod(names = "zero?")
    public abstract static class ZeroNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        public boolean zeroNormal(DynamicObject value) {
            return Layouts.BIG_DECIMAL.getValue(value).compareTo(BigDecimal.ZERO) == 0;
        }

        @Specialization(guards = "!isNormal(value)")
        public boolean zeroSpecial(DynamicObject value) {
            return Layouts.BIG_DECIMAL.getType(value) == BigDecimalType.NEGATIVE_ZERO;
        }
    }

    @CoreMethod(names = "sign")
    public abstract static class SignNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Child private GetIntegerConstantNode sign;

        @Specialization(guards = {
                "isNormal(value)",
                "isNormalZero(value)"
        })
        public int signNormalZero(DynamicObject value) {
            return getConstant("SIGN_POSITIVE_ZERO");
        }

        @Specialization(guards = {
                "isNormal(value)",
                "!isNormalZero(value)"
        })
        public int signNormal(DynamicObject value,
                @Cached("createBinaryProfile()") ConditionProfile positiveProfile) {
            final String name;

            if (positiveProfile.profile(Layouts.BIG_DECIMAL.getValue(value).signum() > 0)) {
                name = "SIGN_POSITIVE_FINITE";
            } else {
                name = "SIGN_NEGATIVE_FINITE";
            }

            return getConstant(name);
        }

        @Specialization(guards = "!isNormal(value)")
        public int signSpecial(DynamicObject value) {
            final String name;

            switch (Layouts.BIG_DECIMAL.getType(value)) {
                case NEGATIVE_INFINITY:
                    name = "SIGN_NEGATIVE_INFINITE";
                    break;
                case POSITIVE_INFINITY:
                    name = "SIGN_POSITIVE_INFINITE";
                    break;
                case NEGATIVE_ZERO:
                    name = "SIGN_NEGATIVE_ZERO";
                    break;
                case NAN:
                    name = "SIGN_NaN";
                    break;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(value));
            }

            return getConstant(name);
        }

        private int getConstant(String name) {
            if (sign == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                sign = insert(GetIntegerConstantNode.create());
            }

            return sign.executeGetIntegerConstant(getBigDecimalClass(), name);
        }

    }

    @CoreMethod(names = "nan?")
    public abstract static class NanNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        public boolean nanNormal(DynamicObject value) {
            return false;
        }

        @Specialization(guards = "!isNormal(value)")
        public boolean nanSpecial(DynamicObject value) {
            return Layouts.BIG_DECIMAL.getType(value) == BigDecimalType.NAN;
        }

    }

    @CoreMethod(names = "exponent")
    public abstract static class ExponentNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = {
                "isNormal(value)",
                "!isNormalZero(value)"
        })
        public long exponent(DynamicObject value) {
            final BigDecimal val = Layouts.BIG_DECIMAL.getValue(value).abs().stripTrailingZeros();
            return val.precision() - val.scale();
        }

        @Specialization(guards = {
                "isNormal(value)",
                "isNormalZero(value)"
        })
        public int exponentZero(DynamicObject value) {
            return 0;
        }

        @Specialization(guards = "!isNormal(value)")
        public int exponentSpecial(DynamicObject value) {
            return 0;
        }

    }

    @CoreMethod(names = "abs")
    public abstract static class AbsNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        private BigDecimal abs(DynamicObject value) {
            return Layouts.BIG_DECIMAL.getValue(value).abs();
        }

        @Specialization(guards = "isNormal(value)")
        public Object absNormal(DynamicObject value) {
            return createBigDecimal(abs(value));
        }

        @Specialization(guards = "!isNormal(value)")
        public Object absSpecial(DynamicObject value,
                @Cached("create()") BranchProfile negInfProfile,
                @Cached("create()") BranchProfile negZeroProfile,
                @Cached("create()") BranchProfile posInfProfile) {
            final BigDecimalType type = Layouts.BIG_DECIMAL.getType(value);

            final Object result;

            switch (type) {
                case NEGATIVE_INFINITY:
                    negInfProfile.enter();
                    result = BigDecimalType.POSITIVE_INFINITY;
                    break;
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    result = BigDecimal.ZERO;
                    break;
                case POSITIVE_INFINITY:
                case NAN:
                    posInfProfile.enter();
                    result = type;
                    break;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + type);
            }

            return createBigDecimal(result);
        }

    }

    @CoreMethod(names = "round", optional = 2, lowerFixnum = { 1, 2 })
    public abstract static class RoundNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        private BigDecimal round(DynamicObject value, int digit, RoundingMode roundingMode) {
            final BigDecimal valueBigDecimal = Layouts.BIG_DECIMAL.getValue(value);

            if (digit <= valueBigDecimal.scale()) {
                return valueBigDecimal.
                        movePointRight(digit).
                        setScale(0, roundingMode).
                        movePointLeft(digit);
            } else {
                // Do not perform rounding when not required
                return valueBigDecimal;
            }
        }

        @Specialization(guards = "isNormal(value)")
        public Object round(DynamicObject value, NotProvided digit, NotProvided roundingMode,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode) {
            return fixnumOrBignumNode.fixnumOrBignum(round(value, 0, getRoundMode()));
        }

        @Specialization(guards = "isNormal(value)")
        public Object round(DynamicObject value, int digit, NotProvided roundingMode) {
            return createBigDecimal(round(value, digit, getRoundMode()));
        }

        @Specialization(guards = {"isNormal(value)", "isRubySymbol(roundingMode)"})
        public Object round(DynamicObject value, int digit, DynamicObject roundingMode) {
            return createBigDecimal(round(value, digit, toRoundingMode(getContext(), this, roundingMode)));
        }

        @Specialization(guards = "isNormal(value)")
        public Object round(DynamicObject value, int digit, int roundingMode) {
            return createBigDecimal(round(value, digit, toRoundingMode(roundingMode)));
        }

        @Specialization(guards = "!isNormal(value)")
        public Object roundSpecial(DynamicObject value, NotProvided precision, Object roundingMode,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached("create()") BranchProfile negInfinityProfile,
                @Cached("create()") BranchProfile posInfinityProfile,
                @Cached("create()") BranchProfile negZeroProfile,
                @Cached("create()") BranchProfile nanProfile) {
            switch (Layouts.BIG_DECIMAL.getType(value)) {
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNegInfinity(this));
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return fixnumOrBignumNode.fixnumOrBignum(Layouts.BIG_DECIMAL.getValue(value));
                case NAN:
                    nanProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(value));

            }
        }

        @Specialization(guards = {
                "!isNormal(value)",
                "wasProvided(precision)"
        })
        public Object roundSpecial(DynamicObject value, Object precision, Object roundingMode) {
            return value;
        }
    }

    @CoreMethod(names = "finite?")
    public abstract static class FiniteNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        public boolean finiteNormal(DynamicObject value) {
            return true;
        }

        @Specialization(guards = "!isNormal(value)")
        public boolean finiteSpecial(DynamicObject value) {
            switch (Layouts.BIG_DECIMAL.getType(value)) {
                case POSITIVE_INFINITY:
                case NEGATIVE_INFINITY:
                case NAN:
                    return false;
                default:
                    return true;
            }
        }

    }

    @CoreMethod(names = "infinite?")
    public abstract static class InfiniteNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        public Object infiniteNormal(DynamicObject value) {
            return nil();
        }

        @Specialization(guards = "!isNormal(value)")
        public Object infiniteSpecial(DynamicObject value) {
            switch (Layouts.BIG_DECIMAL.getType(value)) {
                case POSITIVE_INFINITY:
                    return +1;
                case NEGATIVE_INFINITY:
                    return -1;
                default:
                    return nil();
            }
        }

    }

    @CoreMethod(names = "precs")
    public abstract static class PrecsNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        public Object precsNormal(DynamicObject value) {
            final BigDecimal bigDecimalValue = Layouts.BIG_DECIMAL.getValue(value).abs();
            return createArray(new int[] {
                    bigDecimalValue.stripTrailingZeros().unscaledValue().toString().length(),
                    nearestBiggerMultipleOf4(bigDecimalValue.unscaledValue().toString().length()) }, 2);
        }

        @Specialization(guards = "!isNormal(value)")
        public Object precsSpecial(DynamicObject value) {
            return createArray(new int[] { 1, 1 }, 2);
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        public double toFNormal(DynamicObject value) {
            return Layouts.BIG_DECIMAL.getValue(value).doubleValue();
        }

        @Specialization(guards = "!isNormal(value)")
        public double toFSpecial(DynamicObject value,
                @Cached("create()") BranchProfile negInfinityProfile,
                @Cached("create()") BranchProfile posInfinityProfile,
                @Cached("create()") BranchProfile negZeroProfile,
                @Cached("create()") BranchProfile nanProfile) {
            switch (Layouts.BIG_DECIMAL.getType(value)) {
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    return Double.NEGATIVE_INFINITY;
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    return Double.POSITIVE_INFINITY;
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return 0.0;
                case NAN:
                    nanProfile.enter();
                    return Double.NaN;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(value));
            }
        }

    }

    @CoreMethod(names = "to_r")
    public abstract static class ToRNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode createRationalNode = CallDispatchHeadNode.createPrivate();

        @Specialization(guards = "isNormal(value)")
        public Object toR(DynamicObject value,
                @Cached("new()") FixnumOrBignumNode numeratorConversionNode,
                @Cached("new()") FixnumOrBignumNode denominatorConversionNode) {
            final BigDecimal bigDecimalValue = Layouts.BIG_DECIMAL.getValue(value);

            final BigInteger numerator = getNumerator(bigDecimalValue);
            final BigInteger denominator = getDenominator(bigDecimalValue);

            final Object numeratorAsRubyValue = numeratorConversionNode.fixnumOrBignum(numerator);
            final Object denominatorAsRubyValue = denominatorConversionNode.fixnumOrBignum(denominator);

            return createRationalNode.call(getContext().getCoreLibrary().getRationalClass(), "convert",
                    numeratorAsRubyValue, denominatorAsRubyValue);
        }

        @Specialization(guards = "!isNormal(value)")
        public Object toRSpecial(DynamicObject value,
                @Cached("create()") BranchProfile negInfinityProfile,
                @Cached("create()") BranchProfile posInfinityProfile,
                @Cached("create()") BranchProfile negZeroProfile,
                @Cached("create()") BranchProfile nanProfile) {
            final BigDecimalType type = Layouts.BIG_DECIMAL.getType(value);

            switch (type) {
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNegInfinity(this));
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
                case NAN:
                    nanProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return createRationalNode.call(getContext().getCoreLibrary().getRationalClass(), "convert", 0, 1);
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(value));
            }
        }

        @TruffleBoundary
        private BigInteger getNumerator(BigDecimal value) {
            return value.scaleByPowerOfTen(value.scale()).toBigInteger();
        }

        @TruffleBoundary
        private BigInteger getDenominator(BigDecimal value) {
            return BigInteger.TEN.pow(value.scale());
        }

    }

    @NonStandard
    @CoreMethod(names = "unscaled", visibility = Visibility.PRIVATE)
    public abstract static class UnscaledNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        public Object unscaled(DynamicObject value) {
            return makeStringNode.executeMake(Layouts.BIG_DECIMAL.getValue(value).abs().stripTrailingZeros().unscaledValue().toString(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        @TruffleBoundary
        @Specialization(guards = "!isNormal(value)")
        public Object unscaledSpecial(DynamicObject value) {
            final String type = Layouts.BIG_DECIMAL.getType(value).getRepresentation();
            String string = type.startsWith("-") ? type.substring(1) : type;
            return makeStringNode.executeMake(string, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = { "to_i", "to_int" })
    public abstract static class ToINode extends BigDecimalCoreMethodArrayArgumentsNode {

        private BigInteger toBigInteger(BigDecimal bigDecimal) {
            return bigDecimal.toBigInteger();
        }

        @Specialization(guards = "isNormal(value)")
        public Object toINormal(DynamicObject value,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode) {
            return fixnumOrBignumNode.fixnumOrBignum(toBigInteger(Layouts.BIG_DECIMAL.getValue(value)));
        }

        @TruffleBoundary
        @Specialization(guards = "!isNormal(value)")
        public int toISpecial(DynamicObject value) {
            final BigDecimalType type = Layouts.BIG_DECIMAL.getType(value);
            switch (type) {
                case NEGATIVE_INFINITY:
                    throw new RaiseException(getContext(), coreExceptions().floatDomainError(type.getRepresentation(), this));
                case POSITIVE_INFINITY:
                    throw new RaiseException(getContext(), coreExceptions().floatDomainError(type.getRepresentation(), this));
                case NAN:
                    throw new RaiseException(getContext(), coreExceptions().floatDomainError(type.getRepresentation(), this));
                case NEGATIVE_ZERO:
                    return 0;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException("unreachable code branch for value: " + Layouts.BIG_DECIMAL.getType(value));
            }
        }
    }

}
