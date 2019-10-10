/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.util.Locale;

import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.numeric.FloatNodesFactory.ModNodeFactory;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Float", isClass = true)
public abstract class FloatNodes {

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double neg(double value) {
            return -value;
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double add(double a, long b) {
            return a + b;
        }

        @Specialization
        protected double add(double a, double b) {
            return a + b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected double add(double a, DynamicObject b) {
            return a + Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object addCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
        }
    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double sub(double a, long b) {
            return a - b;
        }

        @Specialization
        protected double sub(double a, double b) {
            return a - b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected double sub(double a, DynamicObject b) {
            return a - Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object subCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MulNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double mul(double a, long b) {
            return a * b;
        }

        @Specialization
        protected double mul(double a, double b) {
            return a * b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected double mul(double a, DynamicObject b) {
            return a * Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object mulCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
        }

    }

    @CoreMethod(names = "**", required = 1)
    public abstract static class PowNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode complexConvertNode;
        @Child private CallDispatchHeadNode complexPowNode;

        private final ConditionProfile complexProfile = ConditionProfile.createBinaryProfile();

        @Specialization(
                guards = { "exponent == cachedExponent", "cachedExponent >= 0", "cachedExponent < 10" },
                limit = "10")
        @ExplodeLoop
        protected double powCached(double base, long exponent,
                @Cached("exponent") long cachedExponent) {
            double result = 1.0;
            for (int i = 0; i < cachedExponent; i++) {
                result *= base;
            }
            return result;
        }

        @Specialization(replaces = "powCached")
        protected double pow(double base, long exponent) {
            return Math.pow(base, exponent);
        }

        @Specialization
        protected Object pow(VirtualFrame frame, double base, double exponent) {
            if (complexProfile.profile(base < 0 && exponent != Math.round(exponent))) {
                if (complexConvertNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    complexConvertNode = insert(CallDispatchHeadNode.createPrivate());
                    complexPowNode = insert(CallDispatchHeadNode.createPrivate());
                }

                final Object aComplex = complexConvertNode.call(coreLibrary().getComplexClass(), "convert", base, 0);

                return complexPowNode.call(aComplex, "**", exponent);
            } else {
                return Math.pow(base, exponent);
            }
        }

        @Specialization(guards = "isRubyBignum(exponent)")
        protected double pow(double base, DynamicObject exponent) {
            return Math.pow(base, Layouts.BIGNUM.getValue(exponent).doubleValue());
        }

        @Specialization(guards = "!isRubyNumber(exponent)")
        protected Object powCoerced(double base, Object exponent,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(base, "redo_coerced", coreStrings().POWER.getSymbol(), exponent);
        }

    }

    @CoreMethod(names = "/", required = 1)
    public abstract static class DivNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double div(double a, long b) {
            return a / b;
        }

        @Specialization
        protected double div(double a, double b) {
            return a / b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected double div(double a, DynamicObject b) {
            return a / Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object divCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
        }

    }

    @CoreMethod(names = "%", required = 1)
    public abstract static class ModNode extends CoreMethodArrayArgumentsNode {

        private final ConditionProfile lessThanZeroProfile = ConditionProfile.createBinaryProfile();
        private final BranchProfile zeroProfile = BranchProfile.create();

        public static ModNode create() {
            return ModNodeFactory.create(null);
        }

        public abstract Object executeMod(Object a, Object b);

        @Specialization
        protected double mod(double a, long b) {
            return mod(a, (double) b);
        }

        @Specialization
        protected double mod(double a, double b) {
            if (b == 0) {
                zeroProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }

            double result = Math.IEEEremainder(a, b);

            if (lessThanZeroProfile.profile(b * result < 0)) {
                result += b;
            }

            return result;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected double mod(double a, DynamicObject b) {
            return mod(a, Layouts.BIGNUM.getValue(b).doubleValue());
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object modCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MODULO.getSymbol(), b);
        }

    }

    @CoreMethod(names = "divmod", required = 1)
    public abstract static class DivModNode extends CoreMethodArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        protected DynamicObject divMod(double a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected DynamicObject divMod(double a, double b) {
            return divModNode.execute(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected DynamicObject divMod(double a, DynamicObject b) {
            return divModNode.execute(a, Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyBignum(b)")
        protected Object divModCoerced(double a, DynamicObject b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().DIVMOD.getSymbol(), b);
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class LessNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean less(double a, long b) {
            return a < b;
        }

        @Specialization
        protected boolean less(double a, double b) {
            return a < b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean lessBignum(double a, DynamicObject b) {
            return a < Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().LESS_THAN.getSymbol(), b);
        }
    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class LessEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean lessEqual(double a, long b) {
            return a <= b;
        }

        @Specialization
        protected boolean lessEqual(double a, double b) {
            return a <= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean lessEqual(double a, DynamicObject b) {
            return a <= Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessEqualCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().LESS_OR_EQUAL.getSymbol(), b);
        }
    }

    @CoreMethod(names = "eql?", required = 1)
    public abstract static class EqlNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean eql(double a, double b) {
            return a == b;
        }

        @Specialization(guards = { "!isDouble(b)" })
        protected boolean eqlGeneral(double a, Object b) {
            return false;
        }
    }

    @CoreMethod(names = { "==", "===" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode fallbackCallNode;

        @Specialization
        protected boolean equal(double a, long b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(double a, double b) {
            return a == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean equal(double a, DynamicObject b) {
            return a == Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object equal(VirtualFrame frame, double a, Object b) {
            if (fallbackCallNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fallbackCallNode = insert(CallDispatchHeadNode.createPrivate());
            }

            return fallbackCallNode.call(a, "equal_fallback", b);
        }
    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNaN(a)")
        protected DynamicObject compareFirstNaN(double a, Object b) {
            return nil();
        }

        @Specialization(guards = "isNaN(b)")
        protected DynamicObject compareSecondNaN(Object a, double b) {
            return nil();
        }

        @Specialization(guards = { "!isNaN(a)" })
        protected int compare(double a, long b) {
            return Double.compare(a, b);
        }

        @Specialization(guards = { "isInfinity(a)", "isRubyBignum(b)" })
        protected int compareInfinity(double a, DynamicObject b) {
            if (a < 0) {
                return -1;
            } else {
                return +1;
            }
        }

        @Specialization(guards = { "!isNaN(a)", "!isInfinity(a)", "isRubyBignum(b)" })
        protected int compareBignum(double a, DynamicObject b) {
            return Double.compare(a, Layouts.BIGNUM.getValue(b).doubleValue());
        }

        @Specialization(guards = { "!isNaN(a)", "!isNaN(b)" })
        protected int compare(double a, double b) {
            return Double.compare(a, b);
        }

        @Specialization(guards = { "!isNaN(a)", "!isRubyBignum(b)" })
        protected DynamicObject compare(double a, DynamicObject b) {
            return nil();
        }

    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class GreaterEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greaterEqual(double a, long b) {
            return a >= b;
        }

        @Specialization
        protected boolean greaterEqual(double a, double b) {
            return a >= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greaterEqual(double a, DynamicObject b) {
            return a >= Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterEqualCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().GREATER_OR_EQUAL.getSymbol(), b);
        }

    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class GreaterNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greater(double a, long b) {
            return a > b;
        }

        @Specialization
        protected boolean greater(double a, double b) {
            return a > b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greater(double a, DynamicObject b) {
            return a > Layouts.BIGNUM.getValue(b).doubleValue();
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterCoerced(double a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().GREATER_THAN.getSymbol(), b);
        }
    }

    @CoreMethod(names = { "abs", "magnitude" })
    public abstract static class AbsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double abs(double n) {
            return Math.abs(n);
        }

    }

    @CoreMethod(names = "ceil")
    public abstract static class CeilNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        protected Object ceil(double n) {
            return fixnumOrBignum.fixnumOrBignum(Math.ceil(n));
        }

    }

    @CoreMethod(names = "floor")
    public abstract static class FloorNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        public abstract Object executeFloor(double n);

        @Specialization
        protected Object floor(double n) {
            return fixnumOrBignum.fixnumOrBignum(Math.floor(n));
        }

    }

    @CoreMethod(names = "infinite?")
    public abstract static class InfiniteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object infinite(double value) {
            if (Double.isInfinite(value)) {
                if (value < 0) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return nil();
            }
        }

    }

    @CoreMethod(names = "nan?")
    public abstract static class NaNNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean nan(double value) {
            return Double.isNaN(value);
        }

    }

    @CoreMethod(names = "next_float")
    public abstract static class NextFloatNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double nextFloat(double value) {
            return Math.nextUp(value);
        }

    }

    @CoreMethod(names = "prev_float")
    public abstract static class PrevFloatNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double prevFloat(double value) {
            return Math.nextDown(value);
        }

    }

    protected static class FloatRoundGuards {

        // These are < rather than <=, because we may offset by -1 or +1 to round in the direction that we want

        public static boolean fitsInInteger(double n) {
            return Integer.MIN_VALUE < n && n < Integer.MAX_VALUE;
        }

        public static boolean fitsInLong(double n) {
            return Long.MIN_VALUE < n && n < Long.MAX_VALUE;
        }

    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_up", needsSelf = false)
    public abstract static class FloatRoundUpPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "fitsInInteger(n)", "isPositive(n)" })
        protected int roundFittingIntPositive(double n) {
            int l = (int) n;
            if (n - l >= 0.5) {
                l++;
            }
            return l;
        }

        @Specialization(guards = { "fitsInInteger(n)", "!isPositive(n)" })
        protected int roundFittingIntNegative(double n) {
            int l = (int) n;
            if (l - n >= 0.5) {
                l--;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "isPositive(n)" }, replaces = "roundFittingIntPositive")
        protected long roundFittingLongPositive(double n) {
            long l = (long) n;
            if (n - l >= 0.5) {
                l++;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "!isPositive(n)" }, replaces = "roundFittingIntNegative")
        protected long roundFittingLongNegative(double n) {
            long l = (long) n;
            if (l - n >= 0.5) {
                l--;
            }
            return l;
        }

        @Specialization(guards = "isPositive(n)", replaces = "roundFittingLongPositive")
        protected Object roundPositive(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.floor(n);
            if (n - f >= 0.5) {
                f += 1.0;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

        @Specialization(guards = "!isPositive(n)", replaces = "roundFittingLongNegative")
        protected Object roundNegative(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.ceil(n);
            if (f - n >= 0.5) {
                f -= 1.0;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

    }

    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_even", needsSelf = false)
    public abstract static class FloatRoundEvenPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "fitsInInteger(n)", "isPositive(n)" })
        protected int roundFittingIntPositive(double n) {
            int l = (int) n;
            if (n - l == 0.5) {
                l += l % 2;
            }
            return l;
        }

        @Specialization(guards = { "fitsInInteger(n)", "!isPositive(n)" })
        protected int roundFittingIntNegative(double n) {
            int l = (int) n;
            if (n - l == 0.5) {
                l -= l % 2;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "isPositive(n)" }, replaces = "roundFittingIntPositive")
        protected long roundFittingLongPositive(double n) {
            long l = (long) n;
            if (n - l == 0.5) {
                l += l % 2;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "!isPositive(n)" }, replaces = "roundFittingIntNegative")
        protected long roundFittingLongNegative(double n) {
            long l = (long) n;
            if (n - l == 0.5) {
                l -= l % 2;
            }
            return l;
        }

        @Specialization(guards = "isPositive(n)", replaces = "roundFittingLongPositive")
        protected Object roundPositive(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.floor(n);
            if (n - f == 0.5) {
                f += f % 2;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

        @Specialization(guards = "!isPositive(n)", replaces = "roundFittingLongNegative")
        protected Object roundNegative(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.ceil(n);
            if (n - f == 0.5) {
                f -= f % 2;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_down", needsSelf = false)
    public abstract static class FloatRoundDownPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "fitsInInteger(n)", "isPositive(n)" })
        protected int roundFittingIntPositive(double n) {
            int l = (int) n;
            if (n - l > 0.5) {
                l++;
            }
            return l;
        }

        @Specialization(guards = { "fitsInInteger(n)", "!isPositive(n)" })
        protected int roundFittingIntNegative(double n) {
            int l = (int) n;
            if (l - n > 0.5) {
                l--;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "isPositive(n)" }, replaces = "roundFittingIntPositive")
        protected long roundFittingLongPositive(double n) {
            long l = (long) n;
            if (n - l > 0.5) {
                l++;
            }
            return l;
        }

        @Specialization(guards = { "fitsInLong(n)", "!isPositive(n)" }, replaces = "roundFittingIntNegative")
        protected long roundFittingLongNegative(double n) {
            long l = (long) n;
            if (l - n > 0.5) {
                l--;
            }
            return l;
        }

        @Specialization(guards = "isPositive(n)", replaces = "roundFittingLongPositive")
        protected Object roundPositive(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.floor(n);
            if (n - f > 0.5) {
                f += 1.0;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

        @Specialization(guards = "!isPositive(n)", replaces = "roundFittingLongNegative")
        protected Object roundNegative(double n,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            double f = Math.ceil(n);
            if (f - n > 0.5) {
                f -= 1.0;
            }
            return fixnumOrBignum.fixnumOrBignum(f);
        }

    }

    @CoreMethod(names = { "to_i", "to_int", "truncate" })
    public abstract static class ToINode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        public abstract Object executeToI(double value);

        @Specialization
        protected Object toI(double value,
                @Cached BranchProfile errorProfile) {
            if (Double.isInfinite(value)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("Infinity", this));
            }

            if (Double.isNaN(value)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("NaN", this));
            }

            return fixnumOrBignum.fixnumOrBignum(value);
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double toF(double value) {
            return value;
        }

    }

    @CoreMethod(names = { "to_s", "inspect" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject toS(double value) {
            /*
             * Ruby has complex custom formatting logic for floats. Our logic meets the specs but we suspect it's
             * possibly still not entirely correct. JRuby seems to be correct, but their logic is tied up in their
             * printf implementation. Also see our FormatFloatNode, which I suspect is also deficient or under-tested.
             */

            if (Double.isInfinite(value) || Double.isNaN(value)) {
                return makeStringNode.executeMake(Double.toString(value), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
            }

            String str = StringUtils.format(Locale.ENGLISH, "%.17g", value);

            // If no dot, add one to show it's a floating point number
            if (str.indexOf('.') == -1) {
                assert str.indexOf('e') == -1;
                str += ".0";
            }

            final int dot = str.indexOf('.');
            assert dot != -1;

            final int e = str.indexOf('e');
            final boolean hasE = e != -1;

            // Remove trailing zeroes, but keep at least one after the dot
            final int start = hasE ? e : str.length();
            int i = start - 1; // last digit we keep, inclusive
            while (i > dot + 1 && str.charAt(i) == '0') {
                i--;
            }

            String formatted = str.substring(0, i + 1) + str.substring(start);

            int wholeDigits = 0;
            int n = 0;

            if (formatted.charAt(0) == '-') {
                n++;
            }

            while (formatted.charAt(n) != '.') {
                wholeDigits++;
                n++;
            }

            if (wholeDigits >= 16) {
                formatted = StringUtils.format(Locale.ENGLISH, "%.1e", value);
            }

            return makeStringNode.executeMake(formatted, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @NonStandard
    @CoreMethod(names = "dtoa", visibility = Visibility.PRIVATE)
    public static abstract class DToANode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject dToA(double value) {
            // Large enough to print all digits of Float::MIN.
            String string = StringUtils.format(Locale.ENGLISH, "%.1022f", value);

            if (string.toLowerCase(Locale.ENGLISH).contains("e")) {
                throw new UnsupportedOperationException();
            }

            string = StringUtils.replace(string, "-", "");
            while (string.charAt(string.length() - 1) == '0') {
                string = string.substring(0, string.length() - 1);
            }

            int decimal;

            if (string.startsWith("0.")) {
                string = StringUtils.replace(string, "0.", "");
                decimal = 0;

                while (string.charAt(0) == '0') {
                    string = string.substring(1, string.length());
                    --decimal;
                }
            } else {
                decimal = string.indexOf('.');

                if (decimal == -1) {
                    throw new UnsupportedOperationException();
                }

                string = StringUtils.replace(string, ".", "");
            }

            final int sign = value < 0 ? 1 : 0;

            return createArray(new Object[]{
                    makeStringNode.executeMake(string, UTF8Encoding.INSTANCE, CodeRange.CR_7BIT),
                    decimal,
                    sign,
                    string.length()
            }, 4);
        }

    }

}
