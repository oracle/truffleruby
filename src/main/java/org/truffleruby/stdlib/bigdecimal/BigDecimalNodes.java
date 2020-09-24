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
import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.numeric.BigDecimalOps;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.parser.SafeDoubleParser;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;


@CoreModule(value = "BigDecimal", isClass = true)
public abstract class BigDecimalNodes {

    // TODO (pitr 2015-jun-16): lazy setup when required, see https://github.com/jruby/jruby/pull/3048#discussion_r32413656

    @Primitive(name = "bigdecimal_new", lowerFixnum = 1)
    public abstract static class NewNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyBigDecimal newBigDecimal(Object value, NotProvided digits, boolean strict) {
            return createBigDecimal(value, strict);
        }

        @Specialization
        protected RubyBigDecimal newBigDecimal(Object value, int digits, boolean strict) {
            return createBigDecimal(value, digits, strict);
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddOpNode extends AbstractAddNode {

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object add(RubyBigDecimal a, RubyBigDecimal b) {
            return add(a, b, getLimit());
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object addSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return addSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object addCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced,
                @CachedLanguage RubyLanguage language) {
            return redoCoerced.call(a, "redo_coerced", language.coreSymbols.PLUS, b);
        }
    }

    @CoreMethod(names = "add", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class AddNode extends AbstractAddNode {

        @Override
        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object add(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.add(a, b, precision);
        }

        @Override
        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object addSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.addSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object addCoerced(RubyBigDecimal a, Object b, int precision,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("add"), b, precision);
        }
    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubOpNode extends AbstractSubNode {

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object sub(RubyBigDecimal a, RubyBigDecimal b) {
            return sub(a, b, getLimit());
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object subSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return subSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object subCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced,
                @CachedLanguage RubyLanguage language) {
            return redoCoerced.call(a, "redo_coerced", language.coreSymbols.MINUS, b);
        }
    }

    @CoreMethod(names = "sub", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class SubNode extends AbstractSubNode {

        @Override
        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object sub(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.sub(a, b, precision);
        }

        @Override
        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object subSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.subSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object subCoerced(RubyBigDecimal a, Object b, int precision,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("sub"), b, precision);
        }
    }

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isNormal(value)", "!isNormalZero(value)" })
        protected Object negNormal(RubyBigDecimal value) {
            return createBigDecimal(BigDecimalOps.negate(value.value));
        }

        @Specialization(guards = { "isNormal(value)", "isNormalZero(value)" })
        protected Object negNormalZero(RubyBigDecimal value) {
            return createBigDecimal(BigDecimalType.NEGATIVE_ZERO);
        }

        @Specialization(guards = "!isNormal(value)")
        protected Object negSpecial(RubyBigDecimal value,
                @Cached ConditionProfile nanProfile,
                @Cached ConditionProfile negZeroProfile,
                @Cached ConditionProfile infProfile) {
            final BigDecimalType type = value.type;

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

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object mult(RubyBigDecimal a, RubyBigDecimal b) {
            return mult(a, b, getLimit());
        }

        @Specialization(guards = { "isNormal(a)", "isSpecial(b)" })
        protected Object multNormalSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return multSpecialNormal(b, a, 0);
        }

        @Specialization(guards = { "!isNormal(a)", "isNormal(b)" })
        protected Object multSpecialNormal(RubyBigDecimal a, RubyBigDecimal b) {
            return multSpecialNormal(a, b, 0);
        }

        @Specialization(guards = { "!isNormal(a)", "isSpecial(b)" })
        protected Object multSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return multSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object multCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced,
                @CachedLanguage RubyLanguage language) {
            return redoCoerced.call(a, "redo_coerced", language.coreSymbols.MULTIPLY, b);
        }
    }

    @CoreMethod(names = "mult", required = 2, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class MultNode extends AbstractMultNode {

        @Override
        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected RubyBigDecimal mult(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.mult(a, b, precision);
        }

        @Override
        @Specialization(guards = { "isNormal(a)", "isSpecial(b)" })
        protected Object multNormalSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.multNormalSpecial(a, b, precision);
        }

        @Override
        @Specialization(guards = { "!isNormal(a)", "isNormal(b)" })
        protected RubyBigDecimal multSpecialNormal(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.multSpecialNormal(a, b, precision);
        }

        @Override
        @Specialization(guards = { "!isNormal(a)", "isSpecial(b)" })
        protected Object multSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.multSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object multCoerced(RubyBigDecimal a, Object b, int precision,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("mult"), b, precision);
        }
    }

    @CoreMethod(names = { "/", "quo" }, required = 1)
    public abstract static class DivOpNode extends AbstractDivNode {

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object div(RubyBigDecimal a, RubyBigDecimal b) {
            final int precision = defaultDivisionPrecision(a.value, b.value, getLimit());
            return div(a, b, precision);
        }

        @Specialization(guards = { "isNormal(a)", "isSpecial(b)" })
        protected Object divNormalSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return divNormalSpecial(a, b, 0);
        }

        @Specialization(guards = { "!isNormal(a)", "isNormal(b)" })
        protected Object divSpecialNormal(RubyBigDecimal a, RubyBigDecimal b) {
            return divSpecialNormal(a, b, 0);
        }

        @Specialization(guards = { "!isNormal(a)", "isSpecial(b)" })
        protected Object divSpecialSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            return divSpecialSpecial(a, b, 0);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object divCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced,
                @CachedLanguage RubyLanguage language) {
            return redoCoerced.call(a, "redo_coerced", language.coreSymbols.DIVIDE, b);
        }
    }

    @CoreMethod(names = "div", required = 1, optional = 1, lowerFixnum = 2)
    @NodeChild(value = "precision", type = RubyNode.class)
    public abstract static class DivNode extends AbstractDivNode {

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object div(RubyBigDecimal a, RubyBigDecimal b, NotProvided precision,
                @Cached ConditionProfile bZeroProfile,
                @Cached DispatchNode floorNode) {
            if (bZeroProfile.profile(isNormalZero(b))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                final Object result = div(a, b, 0);
                return floorNode.call(result, "floor");
            }
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected Object div(RubyBigDecimal a, RubyBigDecimal b, int precision,
                @Cached ConditionProfile zeroPrecisionProfile) {
            final int newPrecision;

            if (zeroPrecisionProfile.profile(precision == 0)) {
                newPrecision = defaultDivisionPrecision(a.value, b.value, getLimit());
            } else {
                newPrecision = precision;
            }

            return super.div(a, b, newPrecision);
        }

        @Specialization(guards = { "isNormal(a)", "isSpecial(b)" })
        protected Object divNormalSpecial(RubyBigDecimal a, RubyBigDecimal b, NotProvided precision,
                @Cached ConditionProfile negativeZeroProfile,
                @Cached ConditionProfile nanProfile) {
            if (negativeZeroProfile.profile(b.type == BigDecimalType.NEGATIVE_ZERO)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(b.type == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else {
                return divNormalSpecial(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = { "isNormal(a)", "isSpecial(b)" })
        protected Object divNormalSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.divNormalSpecial(a, b, precision);
        }

        @Specialization(guards = { "!isNormal(a)", "isNormal(b)" })
        protected Object divSpecialNormal(RubyBigDecimal a, RubyBigDecimal b, NotProvided precision,
                @Cached ConditionProfile zeroDivisionProfile,
                @Cached ConditionProfile nanProfile,
                @Cached ConditionProfile infinityProfile) {
            if (zeroDivisionProfile.profile(isNormalZero(b))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(a.type == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else if (infinityProfile.profile(
                    a.type == BigDecimalType.POSITIVE_INFINITY ||
                            a.type == BigDecimalType.NEGATIVE_INFINITY)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
            } else {
                return divSpecialNormal(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = { "!isNormal(a)", "isNormal(b)" })
        protected Object divSpecialNormal(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.divSpecialNormal(a, b, precision);
        }

        @Specialization(guards = { "!isNormal(a)", "isSpecial(b)" })
        protected Object divSpecialSpecial(RubyBigDecimal a, RubyBigDecimal b, NotProvided precision,
                @Cached ConditionProfile negZeroProfile,
                @Cached ConditionProfile nanProfile) {
            if (negZeroProfile.profile(b.type == BigDecimalType.NEGATIVE_ZERO)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else if (nanProfile.profile(
                    a.type == BigDecimalType.NAN ||
                            b.type == BigDecimalType.NAN)) {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
            } else {
                return divSpecialSpecial(a, b, 0);
            }
        }

        @Override
        @Specialization(guards = { "!isNormal(a)", "isSpecial(b)" })
        protected Object divSpecialSpecial(RubyBigDecimal a, RubyBigDecimal b, int precision) {
            return super.divSpecialSpecial(a, b, precision);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object divCoerced(RubyBigDecimal a, Object b, NotProvided precision,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("div"), b);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object divCoerced(RubyBigDecimal a, Object b, int precision,
                @Cached DispatchNode redoCoerced) {
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

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "!isNormalZero(a)", "!isNormalZero(b)" })
        protected RubyArray divmod(RubyBigDecimal a, RubyBigDecimal b) {
            final BigDecimal[] result = divmodBigDecimal(a.value, b.value);
            return createArray(new Object[]{ createBigDecimal(result[0]), createBigDecimal(result[1]) });
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "isNormalZero(a)", "!isNormalZero(b)" })
        protected RubyArray divmodZeroDividend(RubyBigDecimal a, RubyBigDecimal b) {
            final Object[] store = new Object[]{ createBigDecimal(BigDecimal.ZERO), createBigDecimal(BigDecimal.ZERO) };
            return createArray(store);
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "isNormalZero(b)" })
        protected RubyArray divmodZeroDivisor(RubyBigDecimal a, RubyBigDecimal b) {
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected RubyArray divmodSpecial(RubyBigDecimal a, RubyBigDecimal b,
                @Cached DispatchNode signCall,
                @Cached IntegerCastNode signIntegerCast,
                @Cached ConditionProfile nanProfile,
                @Cached ConditionProfile normalNegProfile,
                @Cached ConditionProfile negNormalProfile,
                @Cached ConditionProfile infinityProfile) {
            final BigDecimalType aType = a.type;
            final BigDecimalType bType = b.type;

            if (nanProfile.profile(aType == BigDecimalType.NAN || bType == BigDecimalType.NAN)) {
                return createArray(new Object[]{
                        createBigDecimal(BigDecimalType.NAN),
                        createBigDecimal(BigDecimalType.NAN) });
            }

            if (nanProfile.profile(
                    bType == BigDecimalType.NEGATIVE_ZERO || (bType == BigDecimalType.NORMAL && isNormalZero(b)))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }

            if (normalNegProfile.profile(
                    aType == BigDecimalType.NEGATIVE_ZERO || (aType == BigDecimalType.NORMAL && isNormalZero(a)))) {
                return createArray(new Object[]{
                        createBigDecimal(BigDecimal.ZERO),
                        createBigDecimal(BigDecimal.ZERO) });
            }

            if (negNormalProfile
                    .profile(aType == BigDecimalType.POSITIVE_INFINITY || aType == BigDecimalType.NEGATIVE_INFINITY)) {
                final int signA = aType == BigDecimalType.POSITIVE_INFINITY ? 1 : -1;
                final int signB = Integer.signum(signIntegerCast.executeCastInt(signCall.call(b, "sign")));
                final int sign = signA * signB; // is between -1 and 1, 0 when nan

                final BigDecimalType type = new BigDecimalType[]{
                        BigDecimalType.NEGATIVE_INFINITY,
                        BigDecimalType.NAN,
                        BigDecimalType.POSITIVE_INFINITY }[sign + 1];

                return createArray(new Object[]{ createBigDecimal(type), createBigDecimal(BigDecimalType.NAN) });
            }

            if (infinityProfile
                    .profile(bType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY)) {
                return createArray(new Object[]{ createBigDecimal(BigDecimal.ZERO), createBigDecimal(a) });
            }

            throw Utils.unsupportedOperation("unreachable code branch");
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object divmodCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", getSymbol("divmod"), b);
        }

    }

    @CoreMethod(names = "remainder", required = 1)
    public abstract static class RemainderNode extends BigDecimalOpNode {

        @TruffleBoundary
        public static BigDecimal remainderBigDecimal(BigDecimal a, BigDecimal b) {
            return a.remainder(b);
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "!isNormalZero(b)" })
        protected Object remainder(RubyBigDecimal a, RubyBigDecimal b) {
            return createBigDecimal(
                    remainderBigDecimal(a.value, b.value));
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "isNormalZero(b)" })
        protected Object remainderZero(RubyBigDecimal a, RubyBigDecimal b) {
            return createBigDecimal(BigDecimalType.NAN);
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object remainderSpecial(RubyBigDecimal a, RubyBigDecimal b,
                @Cached ConditionProfile zeroProfile) {
            final BigDecimalType aType = a.type;
            final BigDecimalType bType = b.type;

            if (zeroProfile.profile(aType == BigDecimalType.NEGATIVE_ZERO && bType == BigDecimalType.NORMAL)) {
                return createBigDecimal(BigDecimal.ZERO);
            } else {
                return createBigDecimal(BigDecimalType.NAN);
            }
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object remainderCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced) {
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

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "!isNormalZero(b)" })
        protected Object modulo(RubyBigDecimal a, RubyBigDecimal b) {
            return createBigDecimal(moduloBigDecimal(a.value, b.value));
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)", "isNormalZero(b)" })
        protected Object moduloZero(RubyBigDecimal a, RubyBigDecimal b) {
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)" })
        protected Object moduloSpecial(RubyBigDecimal a, RubyBigDecimal b,
                @Cached ConditionProfile nanProfile,
                @Cached ConditionProfile normalNegProfile,
                @Cached ConditionProfile negNormalProfile,
                @Cached ConditionProfile posNegInfProfile,
                @Cached ConditionProfile negPosInfProfile) {
            final BigDecimalType aType = a.type;
            final BigDecimalType bType = b.type;

            if (nanProfile.profile(aType == BigDecimalType.NAN || bType == BigDecimalType.NAN)) {
                return createBigDecimal(BigDecimalType.NAN);
            }

            if (normalNegProfile.profile(
                    bType == BigDecimalType.NEGATIVE_ZERO || (bType == BigDecimalType.NORMAL && isNormalZero(b)))) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }

            if (negNormalProfile.profile(
                    aType == BigDecimalType.NEGATIVE_ZERO || (aType == BigDecimalType.NORMAL && isNormalZero(a)))) {
                return createBigDecimal(BigDecimal.ZERO);
            }

            if (posNegInfProfile
                    .profile(aType == BigDecimalType.POSITIVE_INFINITY || aType == BigDecimalType.NEGATIVE_INFINITY)) {
                return createBigDecimal(BigDecimalType.NAN);
            }

            if (negPosInfProfile
                    .profile(bType == BigDecimalType.POSITIVE_INFINITY || bType == BigDecimalType.NEGATIVE_INFINITY)) {
                return createBigDecimal(a);
            }

            throw Utils.unsupportedOperation("unreachable code branch");
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object moduloCoerced(RubyBigDecimal a, Object b,
                @Cached DispatchNode redoCoerced) {
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
        protected Object power(RubyBigDecimal a, int exponent, NotProvided precision) {
            return executePower(a, exponent, getLimit());
        }

        @Specialization(guards = "isNormal(a)")
        protected Object power(RubyBigDecimal a, int exponent, int precision,
                @Cached ConditionProfile positiveExponentProfile,
                @Cached ConditionProfile zeroProfile,
                @Cached ConditionProfile zeroExponentProfile) {
            final BigDecimal aBigDecimal = a.value;
            final boolean positiveExponent = positiveExponentProfile.profile(exponent >= 0);

            if (zeroProfile.profile(BigDecimalOps.compare(aBigDecimal, BigDecimal.ZERO) == 0)) {
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

                return createBigDecimal(
                        power(a.value, exponent, BigDecimalOps.newMathContext(newPrecision, getRoundMode())));
            }
        }

        @Specialization(guards = "!isNormal(a)")
        protected Object power(RubyBigDecimal a, int exponent, Object unusedPrecision,
                @Cached BranchProfile nanProfile,
                @Cached BranchProfile posInfinityProfile,
                @Cached BranchProfile negInfinityProfile,
                @Cached BranchProfile negZeroProfile) {
            final Object value;

            switch (a.type) {
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
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + a.type);
            }

            return createBigDecimal(value);
        }
    }

    @CoreMethod(names = "sqrt", required = 1, lowerFixnum = 1)
    public abstract static class SqrtNode extends BigDecimalCoreMethodArrayArgumentsNode {

        public abstract Object executeSqrt(RubyBigDecimal value, int precision);

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
        protected Object sqrtNegativePrecision(RubyBigDecimal a, int precision) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("precision must be positive", this));
        }

        @Specialization(guards = "precision == 0")
        protected Object sqrtZeroPrecision(RubyBigDecimal a, int precision) {
            return executeSqrt(a, 1);
        }

        @Specialization(guards = { "isNormal(a)", "precision > 0" })
        protected Object sqrt(RubyBigDecimal a, int precision,
                @Cached ConditionProfile positiveValueProfile) {
            final BigDecimal valueBigDecimal = a.value;
            if (positiveValueProfile.profile(BigDecimalOps.signum(valueBigDecimal) >= 0)) {
                return createBigDecimal(sqrt(valueBigDecimal, BigDecimalOps.newMathContext(precision, getRoundMode())));
            } else {
                throw new RaiseException(getContext(), coreExceptions().floatDomainErrorSqrtNegative(this));
            }
        }

        @Specialization(guards = { "!isNormal(a)", "precision > 0" })
        protected Object sqrtSpecial(RubyBigDecimal a, int precision,
                @Cached BranchProfile nanProfile,
                @Cached BranchProfile posInfProfile,
                @Cached BranchProfile negInfProfile,
                @Cached BranchProfile negZeroProfile) {
            switch (a.type) {
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
                    return createBigDecimal(
                            sqrt(BigDecimal.ZERO, BigDecimalOps.newMathContext(precision, getRoundMode())));
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + a.type);
            }
        }
    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends BigDecimalOpNode {

        @Child private DispatchNode redoCompare;

        @Specialization(guards = "isNormal(a)")
        protected int compare(RubyBigDecimal a, long b) {
            return BigDecimalOps.compare(a, BigDecimalOps.valueOf(b));
        }

        @Specialization(guards = { "isNormal(a)", "isFinite(b)" })
        protected int compareFinite(RubyBigDecimal a, double b) {
            return BigDecimalOps.compare(a, BigDecimalOps.valueOf(b));
        }

        @Specialization(guards = { "isNormal(a)", "!isFinite(b)" })
        protected Object compareNotFinite(RubyBigDecimal a, double b) {
            if (Double.isNaN(b)) {
                return nil;
            } else {
                assert Double.isInfinite(b);
                return b < 0 ? +1 : -1;
            }
        }

        @Specialization(guards = { "isNormal(a)" })
        protected int compare(RubyBigDecimal a, RubyBignum b) {
            return BigDecimalOps.compare(a, BigDecimalOps.fromBigInteger(b));
        }

        @Specialization(guards = { "isNormal(a)", "isNormal(b)" })
        protected int compareNormal(RubyBigDecimal a, RubyBigDecimal b) {
            return BigDecimalOps.compare(a, b.value);
        }

        @Specialization(guards = "!isNormal(a)")
        protected Object compareSpecial(RubyBigDecimal a, long b) {
            return compareSpecial(a, createBigDecimal(BigDecimalOps.valueOf(b)));
        }

        @Specialization(guards = { "!isNormal(a)", "isFinite(b)" })
        protected Object compareSpecialFinite(RubyBigDecimal a, double b) {
            return compareSpecial(a, createBigDecimal(BigDecimalOps.valueOf(b)));
        }

        @Specialization(guards = { "!isNormal(a)", "!isFinite(b)" })
        protected Object compareSpecialInfinite(RubyBigDecimal a, double b) {
            final BigDecimalType type = a.type;

            if (type == BigDecimalType.NAN || Double.isNaN(b)) {
                return nil;
            } else {
                return b < 0 ? +1 : -1;
            }
        }

        @Specialization(guards = { "!isNormal(a)" })
        protected Object compareSpecialBignum(RubyBigDecimal a, RubyBignum b) {
            return compareSpecial(a, createBigDecimal(BigDecimalOps.fromBigInteger(b)));
        }

        @Specialization(guards = { "!isNormal(a)", "isNan(a)" })
        protected Object compareSpecialNan(RubyBigDecimal a, RubyBigDecimal b) {
            return nil;
        }

        @Specialization(guards = { "!isNormal(a) || !isNormal(b)", "isNormal(a) || !isNan(a)" })
        protected Object compareSpecial(RubyBigDecimal a, RubyBigDecimal b) {
            final BigDecimalType aType = a.type;
            final BigDecimalType bType = b.type;

            if (aType == BigDecimalType.NAN || bType == BigDecimalType.NAN) {
                return nil;
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
                aCompare = a.value;
            }
            if (bType == BigDecimalType.NEGATIVE_ZERO) {
                bCompare = BigDecimal.ZERO;
            } else {
                bCompare = b.value;
            }

            return BigDecimalOps.compare(aCompare, bCompare);
        }

        @Specialization(guards = "!isRubyBigDecimal(b)")
        protected Object compareCoerced(RubyBigDecimal a, RubyDynamicObject b) {
            return redoCompare(a, b);
        }

        @Fallback
        protected Object compareCoercedFallback(Object a, Object b) {
            return redoCompare(a, b);
        }

        private Object redoCompare(Object a, Object b) {
            if (redoCompare == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                redoCompare = insert(DispatchNode.create());
            }
            return redoCompare.call(a, "redo_compare_no_error", b);
        }
    }

    // TODO (pitr 20-May-2015): compare Ruby implementation of #== with a Java one

    @CoreMethod(names = "zero?")
    public abstract static class ZeroNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        protected boolean zeroNormal(RubyBigDecimal value) {
            return BigDecimalOps.compare(value.value, BigDecimal.ZERO) == 0;
        }

        @Specialization(guards = "!isNormal(value)")
        protected boolean zeroSpecial(RubyBigDecimal value) {
            return value.type == BigDecimalType.NEGATIVE_ZERO;
        }
    }

    @CoreMethod(names = "sign")
    public abstract static class SignNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Child private GetIntegerConstantNode sign;

        @Specialization(guards = { "isNormal(value)", "isNormalZero(value)" })
        protected int signNormalZero(RubyBigDecimal value) {
            return getConstant("SIGN_POSITIVE_ZERO");
        }

        @Specialization(guards = { "isNormal(value)", "!isNormalZero(value)" })
        protected int signNormal(RubyBigDecimal value,
                @Cached ConditionProfile positiveProfile) {
            final String name;

            if (positiveProfile.profile(BigDecimalOps.signum(value) > 0)) {
                name = "SIGN_POSITIVE_FINITE";
            } else {
                name = "SIGN_NEGATIVE_FINITE";
            }

            return getConstant(name);
        }

        @Specialization(guards = "!isNormal(value)")
        protected int signSpecial(RubyBigDecimal value) {
            final String name;

            switch (value.type) {
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
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + value.value);
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
        protected boolean nanNormal(RubyBigDecimal value) {
            return false;
        }

        @Specialization(guards = "!isNormal(value)")
        protected boolean nanSpecial(RubyBigDecimal value) {
            return value.type == BigDecimalType.NAN;
        }

    }

    @CoreMethod(names = "exponent")
    public abstract static class ExponentNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isNormal(value)", "!isNormalZero(value)" })
        protected long exponent(RubyBigDecimal value) {
            final BigDecimal val = value.value.abs().stripTrailingZeros();
            return val.precision() - val.scale();
        }

        @Specialization(guards = { "isNormal(value)", "isNormalZero(value)" })
        protected int exponentZero(RubyBigDecimal value) {
            return 0;
        }

        @Specialization(guards = "!isNormal(value)")
        protected int exponentSpecial(RubyBigDecimal value) {
            return 0;
        }

    }

    @CoreMethod(names = "abs")
    public abstract static class AbsNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        private BigDecimal abs(RubyBigDecimal value) {
            return value.value.abs();
        }

        @Specialization(guards = "isNormal(value)")
        protected Object absNormal(RubyBigDecimal value) {
            return createBigDecimal(abs(value));
        }

        @Specialization(guards = "!isNormal(value)")
        protected Object absSpecial(RubyBigDecimal value,
                @Cached BranchProfile negInfProfile,
                @Cached BranchProfile negZeroProfile,
                @Cached BranchProfile posInfProfile) {
            final BigDecimalType type = value.type;

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

        @Specialization(guards = "isNormal(value)")
        protected Object round(RubyBigDecimal value, NotProvided ndigits, NotProvided roundingMode,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode) {
            return fixnumOrBignumNode.fixnumOrBignum(round(value, 0, getRoundMode()));
        }

        @Specialization(guards = "isNormal(value)")
        protected Object round(RubyBigDecimal value, int ndigits, NotProvided roundingMode) {
            return createBigDecimal(round(value, ndigits, getRoundMode()));
        }

        @Specialization(guards = "isNormal(value)")
        protected Object round(RubyBigDecimal value, int ndigits, RubySymbol roundingMode,
                @Cached DispatchNode callRoundModeFromSymbol) {
            return createBigDecimal(round(
                    value,
                    ndigits,
                    toRoundingMode((int) callRoundModeFromSymbol.call(value, "round_mode_from_symbol", roundingMode))));
        }

        @Specialization(guards = "isNormal(value)")
        protected Object round(RubyBigDecimal value, int ndigits, int roundingMode) {
            return createBigDecimal(round(value, ndigits, toRoundingMode(roundingMode)));
        }

        @Specialization(guards = "!isNormal(value)")
        protected Object roundSpecial(RubyBigDecimal value, NotProvided ndigits, NotProvided roundingMode,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BranchProfile negInfinityProfile,
                @Cached BranchProfile posInfinityProfile,
                @Cached BranchProfile negZeroProfile,
                @Cached BranchProfile nanProfile) {
            switch (value.type) {
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNegInfinity(this));
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToInfinity(this));
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return fixnumOrBignumNode.fixnumOrBignum(value.value);
                case NAN:
                    nanProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().floatDomainErrorResultsToNaN(this));
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + value.value);

            }
        }

        @Specialization(guards = { "!isNormal(value)", "wasProvided(ndigits)", "wasProvided(roundingMode)" })
        protected Object roundSpecial(RubyBigDecimal value, Object ndigits, Object roundingMode) {
            return value;
        }

        @Specialization(guards = { "!isNormal(value)", "wasProvided(ndigits)" })
        protected Object roundSpecial(RubyBigDecimal value, Object ndigits, NotProvided roundingMode) {
            return value;
        }

        @TruffleBoundary
        private BigDecimal round(RubyBigDecimal value, int ndigits, RoundingMode roundingMode) {
            final BigDecimal valueBigDecimal = value.value;

            if (ndigits <= valueBigDecimal.scale()) {
                return valueBigDecimal.movePointRight(ndigits).setScale(0, roundingMode).movePointLeft(ndigits);
            } else {
                // Do not perform rounding when not required
                return valueBigDecimal;
            }
        }

    }

    @CoreMethod(names = "finite?")
    public abstract static class FiniteNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        protected boolean finiteNormal(RubyBigDecimal value) {
            return true;
        }

        @Specialization(guards = "!isNormal(value)")
        protected boolean finiteSpecial(RubyBigDecimal value) {
            switch (value.type) {
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
        protected Object infiniteNormal(RubyBigDecimal value) {
            return nil;
        }

        @Specialization(guards = "!isNormal(value)")
        protected Object infiniteSpecial(RubyBigDecimal value) {
            switch (value.type) {
                case POSITIVE_INFINITY:
                    return +1;
                case NEGATIVE_INFINITY:
                    return -1;
                default:
                    return nil;
            }
        }

    }

    @CoreMethod(names = "precs")
    public abstract static class PrecsNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        protected RubyArray precsNormal(RubyBigDecimal value) {
            final BigDecimal bigDecimalValue = value.value.abs();
            final int precs = nearestBiggerMultipleOf9(
                    bigDecimalValue.stripTrailingZeros().unscaledValue().toString().length());
            return createArray(new int[]{ precs, precs + 9 });
        }

        @Specialization(guards = "!isNormal(value)")
        protected RubyArray precsSpecial(RubyBigDecimal value) {
            return createArray(new int[]{ 9, 9 });
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        protected double toFNormal(RubyBigDecimal value) {
            return value.value.doubleValue();
        }

        @Specialization(guards = "!isNormal(value)")
        protected double toFSpecial(RubyBigDecimal value,
                @Cached BranchProfile negInfinityProfile,
                @Cached BranchProfile posInfinityProfile,
                @Cached BranchProfile negZeroProfile,
                @Cached BranchProfile nanProfile) {
            switch (value.type) {
                case NEGATIVE_INFINITY:
                    negInfinityProfile.enter();
                    return Double.NEGATIVE_INFINITY;
                case POSITIVE_INFINITY:
                    posInfinityProfile.enter();
                    return Double.POSITIVE_INFINITY;
                case NEGATIVE_ZERO:
                    negZeroProfile.enter();
                    return -0.0;
                case NAN:
                    nanProfile.enter();
                    return Double.NaN;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + value.value);
            }
        }

    }

    @CoreMethod(names = "to_r")
    public abstract static class ToRNode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Child private DispatchNode createRationalNode = DispatchNode.create();

        @Specialization(guards = "isNormal(value)")
        protected Object toR(RubyBigDecimal value,
                @Cached("new()") FixnumOrBignumNode numeratorConversionNode,
                @Cached("new()") FixnumOrBignumNode denominatorConversionNode) {
            final BigDecimal bigDecimalValue = value.value;

            final BigInteger numerator = getNumerator(bigDecimalValue);
            final BigInteger denominator = getDenominator(bigDecimalValue);

            final Object numeratorAsRubyValue = numeratorConversionNode.fixnumOrBignum(numerator);
            final Object denominatorAsRubyValue = denominatorConversionNode.fixnumOrBignum(denominator);

            return createRationalNode.call(
                    getContext().getCoreLibrary().rationalClass,
                    "convert",
                    numeratorAsRubyValue,
                    denominatorAsRubyValue);
        }

        @Specialization(guards = "!isNormal(value)")
        protected Object toRSpecial(RubyBigDecimal value,
                @Cached BranchProfile negInfinityProfile,
                @Cached BranchProfile posInfinityProfile,
                @Cached BranchProfile negZeroProfile,
                @Cached BranchProfile nanProfile) {
            final BigDecimalType type = value.type;

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
                    return createRationalNode.call(getContext().getCoreLibrary().rationalClass, "convert", 0, 1);
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + value.value);
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
        protected Object unscaled(RubyBigDecimal value) {
            return makeStringNode.executeMake(
                    value.value.abs().stripTrailingZeros().unscaledValue().toString(),
                    UTF8Encoding.INSTANCE,
                    CodeRange.CR_UNKNOWN);
        }

        @TruffleBoundary
        @Specialization(guards = "!isNormal(value)")
        protected Object unscaledSpecial(RubyBigDecimal value) {
            final String type = value.type.getRepresentation();
            String string = type.startsWith("-") ? type.substring(1) : type;
            return makeStringNode.executeMake(string, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = { "to_i", "to_int" })
    public abstract static class ToINode extends BigDecimalCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNormal(value)")
        protected Object toINormal(RubyBigDecimal value,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode) {
            return fixnumOrBignumNode.fixnumOrBignum(BigDecimalOps.toBigInteger(value.value));
        }

        @TruffleBoundary
        @Specialization(guards = "!isNormal(value)")
        protected int toISpecial(RubyBigDecimal value) {
            final BigDecimalType type = value.type;
            switch (type) {
                case NEGATIVE_INFINITY:
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().floatDomainError(type.getRepresentation(), this));
                case POSITIVE_INFINITY:
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().floatDomainError(type.getRepresentation(), this));
                case NAN:
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().floatDomainError(type.getRepresentation(), this));
                case NEGATIVE_ZERO:
                    return 0;
                default:
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new UnsupportedOperationException(
                            "unreachable code branch for value: " + value.value);
            }
        }
    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends BigDecimalCoreMethodArrayArgumentsNode {

        private static final int CLASS_SALT = 1468180038; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes.

        @TruffleBoundary
        @Specialization(guards = "isNormal(value)")
        protected Object hashNormal(RubyBigDecimal value) {
            // Ruby treats trailing zeroes as insignificant for hash calculation. Java's BigDecimal, however,
            // may return different hash values for two numerically equivalent values with a different number
            // of trailing zeroes. Stripping them away avoids the issue.
            final BigDecimal bigDecimalValue = value.value.stripTrailingZeros();

            return getContext().getHashing(this).hash(CLASS_SALT, bigDecimalValue.hashCode());
        }

        @TruffleBoundary
        @Specialization(guards = "!isNormal(value)")
        protected Object hashSpecial(RubyBigDecimal value) {
            final BigDecimalType type = value.type;

            return getContext().getHashing(this).hash(CLASS_SALT, type.hashCode());
        }

    }

}
