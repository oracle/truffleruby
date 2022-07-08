/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.numeric.FloatNodesFactory.ModNodeFactory;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@CoreModule(value = "Float", isClass = true)
public abstract class FloatNodes {

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double neg(double value) {
            return -value;
        }

    }

    /** See {@link org.truffleruby.core.inlined.InlinedAddNode} */
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

        @Specialization
        protected double add(double a, RubyBignum b) {
            return a + BigIntegerOps.doubleValue(b);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object addCoerced(double a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().PLUS, b);
        }
    }

    /** See {@link org.truffleruby.core.inlined.InlinedSubNode} */
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

        @Specialization
        protected double sub(double a, RubyBignum b) {
            return a - BigIntegerOps.doubleValue(b);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object subCoerced(double a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MINUS, b);
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

        @Specialization
        protected double mul(double a, RubyBignum b) {
            return a * BigIntegerOps.doubleValue(b);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object mulCoerced(double a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MULTIPLY, b);
        }

    }

    @CoreMethod(names = "**", required = 1)
    public abstract static class PowNode extends CoreMethodArrayArgumentsNode {

        @Child private DispatchNode complexConvertNode;
        @Child private DispatchNode complexPowNode;

        private final ConditionProfile complexProfile = ConditionProfile.create();

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
                    complexConvertNode = insert(DispatchNode.create());
                    complexPowNode = insert(DispatchNode.create());
                }

                final Object aComplex = complexConvertNode.call(coreLibrary().complexClass, "convert", base, 0);

                return complexPowNode.call(aComplex, "**", exponent);
            } else {
                return Math.pow(base, exponent);
            }
        }

        @Specialization
        protected double pow(double base, RubyBignum exponent) {
            return Math.pow(base, BigIntegerOps.doubleValue(exponent));
        }

        @Specialization(guards = "!isRubyNumber(exponent)")
        protected Object powCoerced(double base, Object exponent,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(base, "redo_coerced", coreSymbols().POW, exponent);
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

        @Specialization
        protected double div(double a, RubyBignum b) {
            return a / BigIntegerOps.doubleValue(b);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object divCoerced(double a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().DIVIDE, b);
        }

    }

    @CoreMethod(names = "%", required = 1)
    public abstract static class ModNode extends CoreMethodArrayArgumentsNode {

        private final ConditionProfile lessThanZeroProfile = ConditionProfile.create();
        private final BranchProfile zeroProfile = BranchProfile.create();

        public static ModNode create() {
            return ModNodeFactory.create(null);
        }

        public abstract Object executeMod(double a, double b);

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

        @Specialization
        protected double mod(double a, RubyBignum b) {
            return mod(a, BigIntegerOps.doubleValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object modCoerced(double a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MODULO, b);
        }

    }

    @CoreMethod(names = "divmod", required = 1)
    public abstract static class DivModNode extends CoreMethodArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        protected RubyArray divMod(double a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected RubyArray divMod(double a, double b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected RubyArray divMod(double a, RubyBignum b) {
            return divModNode.execute(a, b.value);
        }

        @Specialization
        protected Object divModCoerced(double a, RubyDynamicObject b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().DIVMOD, b);
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

        @Specialization
        protected boolean lessBignum(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) < 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessCoerced(double a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().LESS_THAN, b);
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

        @Specialization
        protected boolean lessEqual(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) <= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessEqualCoerced(double a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().LEQ, b);
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

        @Child private DispatchNode fallbackCallNode;

        @Specialization
        protected boolean equal(double a, long b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(double a, double b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) == 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object equal(VirtualFrame frame, double a, Object b) {
            if (fallbackCallNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fallbackCallNode = insert(DispatchNode.create());
            }

            return fallbackCallNode.call(a, "equal_fallback", b);
        }
    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNaN(a)")
        protected Object compareFirstNaN(double a, Object b) {
            return nil;
        }

        @Specialization(guards = "isNaN(b)")
        protected Object compareSecondNaN(Object a, double b) {
            return nil;
        }

        @Specialization(guards = { "!isNaN(a)", "!isNaN(b)" })
        protected int compareDoubleDouble(double a, double b,
                @Cached ConditionProfile equalProfile) {
            return compareDoubles(a, b, equalProfile);
        }

        @Specialization(guards = { "!isNaN(a)" })
        protected int compareDoubleLong(double a, long b,
                @Cached ConditionProfile equalProfile) {
            return compareDoubles(a, b, equalProfile);
        }

        @Specialization(guards = { "isInfinity(a)" })
        protected int compareInfinity(double a, RubyBignum b) {
            return a < 0 ? -1 : +1;
        }

        @Specialization(guards = { "!isNaN(a)", "!isInfinity(a)" })
        protected int compareBignum(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b);
        }

        @Specialization(guards = { "!isNaN(a)", "!isRubyNumber(b)" })
        protected Object compare(double a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare_bad_coerce_return_error", b);
        }

        public static int compareDoubles(double a, double b, ConditionProfile equalProfile) {
            if (equalProfile.profile(a == b)) {
                return 0;
            } else {
                return a > b ? 1 : -1;
            }
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

        @Specialization
        protected boolean greaterEqual(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) >= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterEqualCoerced(double a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().GEQ, b);
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

        @Specialization
        protected boolean greater(double a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) > 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterCoerced(double a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().GREATER_THAN, b);
        }
    }

    @CoreMethod(names = { "abs", "magnitude" })
    public abstract static class AbsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double abs(double n) {
            return Math.abs(n);
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
                return nil;
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

    @Primitive(name = "float_ceil")
    public abstract static class FloatCeilPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        protected Object ceil(double n) {
            return fixnumOrBignum.fixnumOrBignum(Math.ceil(n));
        }
    }

    @Primitive(name = "float_ceil_ndigits", lowerFixnum = 1)
    public abstract static class FloatCeilNDigitsPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected double ceilNDigits(double n, int ndigits) {
            final double scale = Math.pow(10.0, ndigits);
            return Math.ceil(n * scale) / scale;
        }

    }

    @Primitive(name = "float_floor")
    public abstract static class FloatFloorPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        protected Object floor(double n) {
            return fixnumOrBignum.fixnumOrBignum(Math.floor(n));
        }
    }

    @Primitive(name = "float_floor_ndigits", lowerFixnum = 1)
    public abstract static class FloatFloorNDigitsPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected double floorNDigits(double n, int ndigits) {
            final double scale = Math.pow(10.0, ndigits);
            return Math.floor(n * scale) / scale;
        }

    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_up")
    public abstract static class FloatRoundUpPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "fitsInInteger(n)")
        protected int roundFittingInt(double n) {
            int l = (int) n;
            int signum = (int) Math.signum(n);
            double d = Math.abs(n - l);
            if (d >= 0.5) {
                l += signum;
            }
            return l;
        }

        @Specialization(guards = "fitsInLong(n)", replaces = "roundFittingInt")
        protected long roundFittingLong(double n) {
            long l = (long) n;
            long signum = (long) Math.signum(n);
            double d = Math.abs(n - l);
            if (d >= 0.5) {
                l += signum;
            }
            return l;
        }

        @Specialization(replaces = "roundFittingLong")
        protected Object round(double n,
                @Cached FixnumOrBignumNode fixnumOrBignum) {
            double signum = Math.signum(n);
            double f = Math.floor(Math.abs(n));
            double d = Math.abs(n) - f;
            if (d >= 0.5) {
                f += 1;
            }
            return fixnumOrBignum.fixnumOrBignum(f * signum);
        }
    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_up_decimal", lowerFixnum = 1)
    public abstract static class FloatRoundUpDecimalPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected double roundNDecimal(double n, int ndigits,
                @Cached ConditionProfile boundaryCase) {
            long intPart = (long) n;
            double s = Math.pow(10.0, ndigits) * Math.signum(n);
            double f = (n % 1) * s;
            long fInt = (long) f;
            double d = f % 1;
            int limit = Math.getExponent(n) + Math.getExponent(s) - 51;
            if (boundaryCase.profile((Math.getExponent(d) <= limit) ||
                    (Math.getExponent(1.0 - d) <= limit))) {
                return findClosest(n, ndigits, s, d);
            } else if (d > 0.5 || Math.abs(n) - Math.abs((intPart + (fInt + 0.5) / s)) >= 0) {
                fInt += 1;
            }
            return intPart + fInt / s;
        }
    }

    /* If the rounding result is very near to an integer boundary then we need to find the number that is closest to the
     * correct result. If we don't do this then it's possible to get errors in the least significant bit of the result.
     * We'll test the adjacent double in the direction closest to the boundary and compare the fractional portions. If
     * we're already at the minimum error we'll return the original number as it is already rounded as well as it can
     * be. In the case of a tie we return the lower number, otherwise we check the go round again. */
    private static double findClosest(double n, int ndigits, double s, double d) {
        double n2;
        while (true) {
            if (d > 0.5) {
                n2 = Math.nextAfter(n, n + s);
            } else {
                n2 = Math.nextAfter(n, n - s);
            }
            long intPart = (long) n2;
            double f = (n2 % 1) * s;
            long fInt = (long) f;
            double d2 = f % 1;
            int limit = Math.getExponent(n2) + Math.getExponent(s) - 52;
            if (((d > 0.5) ? 1 - d : d) < ((d2 > 0.5) ? 1 - d2 : d2)) {
                return n;
            } else if (((d > 0.5) ? 1 - d : d) == ((d2 > 0.5) ? 1 - d2 : d2)) {
                return Math.abs(n) < Math.abs(n2) ? n : n2;
            } else {
                d = d2;
                n = n2;
            }
        }
    }

    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_even")
    public abstract static class FloatRoundEvenPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "fitsInInteger(n)" })
        protected int roundFittingInt(double n) {
            int l = (int) n;
            int signum = (int) Math.signum(n);
            double d = Math.abs(n - l);
            if (d > 0.5) {
                l += signum;
            } else if (d == 0.5) {
                l += l % 2;
            }
            return l;
        }

        @Specialization(guards = "fitsInLong(n)", replaces = "roundFittingInt")
        protected long roundFittingLong(double n) {
            long l = (long) n;
            long signum = (long) Math.signum(n);
            double d = Math.abs(n - l);
            if (d > 0.5) {
                l += signum;
            } else if (d == 0.5) {
                l += l % 2;
            }
            return l;
        }

        @Specialization(replaces = "roundFittingLong")
        protected Object round(double n,
                @Cached FixnumOrBignumNode fixnumOrBignum) {
            double signum = Math.signum(n);
            double f = Math.floor(Math.abs(n));
            double d = Math.abs(n) - f;
            if (d > 0.5) {
                f += signum;
            } else if (d == 0.5) {
                f += f % 2;
            }
            return fixnumOrBignum.fixnumOrBignum(f * signum);
        }
    }

    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_even_decimal", lowerFixnum = 1)
    public abstract static class FloatRoundEvenDecimalPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected double roundNDecimal(double n, int ndigits,
                @Cached ConditionProfile boundaryCase) {
            long intPart = (long) n;
            double s = Math.pow(10.0, ndigits) * Math.signum(n);
            double f = (n % 1) * s;
            long fInt = (long) f;
            double d = f % 1;
            int limit = Math.getExponent(n) + Math.getExponent(s) - 51;
            if (boundaryCase.profile((Math.getExponent(d) <= limit) ||
                    (Math.getExponent(1.0 - d) <= limit))) {
                return findClosest(n, ndigits, s, d);
            } else if (d > 0.5) {
                fInt += 1;
            } else if (d == 0.5 || Math.abs(n) - Math.abs((intPart + (fInt + 0.5) / s)) >= 0) {
                fInt += fInt % 2;
            }
            return intPart + fInt / s;
        }
    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_down")
    public abstract static class FloatRoundDownPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "fitsInInteger(n)")
        protected int roundFittingInt(double n) {
            int l = (int) n;
            int signum = (int) Math.signum(n);
            double d = Math.abs(n - l);
            if (d > 0.5) {
                l += signum;
            }
            return l;
        }

        @Specialization(guards = "fitsInLong(n)", replaces = "roundFittingInt")
        protected long roundFittingLong(double n) {
            long l = (long) n;
            long signum = (long) Math.signum(n);
            double d = Math.abs(n - l);
            if (d > 0.5) {
                l += signum;
            }
            return l;
        }

        @Specialization(replaces = "roundFittingLong")
        protected Object round(double n,
                @Cached FixnumOrBignumNode fixnumOrBignum) {
            double signum = Math.signum(n);
            double f = Math.floor(Math.abs(n));
            double d = Math.abs(n) - f;
            if (d > 0.5) {
                f += 1;
            }
            return fixnumOrBignum.fixnumOrBignum(f * signum);
        }
    }

    @ImportStatic(FloatRoundGuards.class)
    @Primitive(name = "float_round_down_decimal", lowerFixnum = 1)
    public abstract static class FloatRoundDownDecimalPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected double roundNDecimal(double n, int ndigits,
                @Cached ConditionProfile boundaryCase) {
            long intPart = (long) n;
            double s = Math.pow(10.0, ndigits) * Math.signum(n);
            double f = (n % 1) * s;
            long fInt = (long) f;
            double d = f % 1;
            int limit = Math.getExponent(n) + Math.getExponent(s) - 51;
            if (boundaryCase.profile((Math.getExponent(d) <= limit) ||
                    (Math.getExponent(1.0 - d) <= limit))) {
                return findClosest(n, ndigits, s, d);
            } else if (d > 0.5 && Math.abs(n) - Math.abs((intPart + (fInt + 0.5) / s)) > 0) {
                fInt += 1;
            }
            return intPart + fInt / s;
        }
    }

    @Primitive(name = "float_exp")
    public abstract static class FloatExpNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected int exp(double value) {
            return Math.getExponent(value);
        }
    }

    @CoreMethod(names = { "to_i", "to_int" })
    public abstract static class ToINode extends CoreMethodArrayArgumentsNode {

        public static ToINode create() {
            return FloatNodesFactory.ToINodeFactory.create(null);
        }

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
    @ImportStatic(Double.class)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        /* Ruby has complex custom formatting logic for floats. Our logic meets the specs but we suspect it's possibly
         * still not entirely correct. JRuby seems to be correct, but their logic is tied up in their printf
         * implementation. Also see our FormatFloatNode, which I suspect is also deficient or under-tested. */

        @Specialization(guards = "value == POSITIVE_INFINITY")
        protected RubyString toSPositiveInfinity(double value,
                @Cached("specialValueRope(POSITIVE_INFINITY)") Rope cachedRope) {
            return makeStringNode.executeMake(cachedRope, Encodings.US_ASCII, NotProvided.INSTANCE);
        }

        @Specialization(guards = "value == NEGATIVE_INFINITY")
        protected RubyString toSNegativeInfinity(double value,
                @Cached("specialValueRope(NEGATIVE_INFINITY)") Rope cachedRope) {
            return makeStringNode.executeMake(cachedRope, Encodings.US_ASCII, NotProvided.INSTANCE);
        }

        @Specialization(guards = "isNaN(value)")
        protected RubyString toSNaN(double value,
                @Cached("specialValueRope(value)") Rope cachedRope) {
            return makeStringNode.executeMake(cachedRope, Encodings.US_ASCII, NotProvided.INSTANCE);
        }

        @Specialization(guards = "hasNoExp(value)")
        protected RubyString toSNoExp(double value) {
            return makeStringNode.executeMake(makeRopeNoExp(value, getLanguage().getCurrentThread()),
                    Encodings.US_ASCII, CodeRange.CR_7BIT);
        }

        @Specialization(guards = "hasLargeExp(value)")
        protected RubyString toSLargeExp(double value) {
            return makeStringNode.executeMake(makeRopeLargeExp(value, getLanguage().getCurrentThread()),
                    Encodings.US_ASCII, CodeRange.CR_7BIT);
        }

        @Specialization(guards = "hasSmallExp(value)")
        protected RubyString toSSmallExp(double value) {
            return makeStringNode.executeMake(makeRopeSmallExp(value, getLanguage().getCurrentThread()),
                    Encodings.US_ASCII, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        private String makeRopeNoExp(double value, RubyThread thread) {
            return getNoExpFormat(thread).format(value);
        }

        @TruffleBoundary
        private String makeRopeSmallExp(double value, RubyThread thread) {
            return getSmallExpFormat(thread).format(value);
        }

        @TruffleBoundary
        private String makeRopeLargeExp(double value, RubyThread thread) {
            return getLargeExpFormat(thread).format(value);
        }

        protected static boolean hasNoExp(double value) {
            double abs = Math.abs(value);
            return abs == 0.0 || ((abs >= 0.0001) && (abs < 1_000_000_000_000_000.0));
        }

        protected static boolean hasLargeExp(double value) {
            double abs = Math.abs(value);
            return Double.isFinite(abs) && (abs >= 1_000_000_000_000_000.0);
        }

        protected static boolean hasSmallExp(double value) {
            double abs = Math.abs(value);
            return (abs < 0.0001) && (abs != 0.0);
        }

        protected static Rope specialValueRope(double value) {
            return RopeOperations.encodeAscii(Double.toString(value), Encodings.US_ASCII.jcoding);
        }

        private DecimalFormat getNoExpFormat(RubyThread thread) {
            if (thread.noExpFormat == null) {
                final DecimalFormatSymbols noExpSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
                thread.noExpFormat = new DecimalFormat("0.0################", noExpSymbols);
            }
            return thread.noExpFormat;
        }

        private DecimalFormat getSmallExpFormat(RubyThread thread) {
            if (thread.smallExpFormat == null) {
                final DecimalFormatSymbols smallExpSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
                smallExpSymbols.setExponentSeparator("e");
                thread.smallExpFormat = new DecimalFormat("0.0################E00", smallExpSymbols);
            }
            return thread.smallExpFormat;
        }

        private DecimalFormat getLargeExpFormat(RubyThread thread) {
            if (thread.largeExpFormat == null) {
                final DecimalFormatSymbols largeExpSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
                largeExpSymbols.setExponentSeparator("e+");
                thread.largeExpFormat = new DecimalFormat("0.0################E00", largeExpSymbols);
            }
            return thread.largeExpFormat;
        }

    }

    @NonStandard
    @CoreMethod(names = "dtoa", visibility = Visibility.PRIVATE)
    public abstract static class DToANode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyArray dToA(double value) {
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
                    makeStringNode.executeMake(string, Encodings.UTF_8, CodeRange.CR_7BIT),
                    decimal,
                    sign,
                    string.length()
            });
        }

    }

}
