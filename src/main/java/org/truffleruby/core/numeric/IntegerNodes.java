/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import java.math.BigInteger;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.numeric.IntegerNodesFactory.AbsNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.DivNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.LeftShiftNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.MulNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.PowNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.RightShiftNodeFactory;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.LazyIntRope;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

@CoreClass("Integer")
public abstract class IntegerNodes {

    public static abstract class BignumCoreMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum;

        public Object fixnumOrBignum(BigInteger value) {
            if (fixnumOrBignum == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fixnumOrBignum = insert(new FixnumOrBignumNode());
            }
            return fixnumOrBignum.fixnumOrBignum(value);
        }

    }

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends BignumCoreMethodNode {

        @Child private FixnumOrBignumNode fixnumOrBignumNode;

        @Specialization(rewriteOn = ArithmeticException.class)
        public int neg(int value) {
            // TODO CS 13-Oct-16, use negateExact, but this isn't intrinsified by Graal yet
            return Math.subtractExact(0, value);
        }

        @Specialization(replaces = "neg")
        public Object negWithOverflow(int value) {
            if (value == Integer.MIN_VALUE) {
                return -((long) value);
            }
            return -value;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long neg(long value) {
            return Math.subtractExact(0, value);
        }

        @Specialization
        public Object negWithOverflow(long value) {
            return fixnumOrBignum(BigInteger.valueOf(value).negate());
        }

        @Specialization
        public Object neg(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).negate());
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddNode extends BignumCoreMethodNode {

        public abstract Object executeAdd(Object a, Object b);

        @Specialization(guards = "b == 1", rewriteOn = ArithmeticException.class)
        public int incInt(int a, int b) {
            return Math.incrementExact(a);
        }

        @Specialization(rewriteOn = ArithmeticException.class, replaces = "incInt")
        public int add(int a, int b) {
            return Math.addExact(a, b);
        }

        @Specialization
        public long addWithOverflow(int a, int b) {
            return (long) a + (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long add(long a, long b) {
            return Math.addExact(a, b);
        }

        @Specialization
        public Object addWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).add(BigInteger.valueOf(b)));
        }

        @Specialization
        public double add(long a, double b) {
            return a + b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object add(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).add(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        public Object add(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).add(BigInteger.valueOf(b)));
        }

        @Specialization
        public double add(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() + b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object add(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).add(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object addCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubNode extends BignumCoreMethodNode {

        public abstract Object executeSub(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        public int sub(int a, int b) {
            return Math.subtractExact(a, b);
        }

        @Specialization
        public long subWithOverflow(int a, int b) {
            return (long) a - (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long sub(long a, long b) {
            return Math.subtractExact(a, b);
        }

        @Specialization
        public Object subWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)));
        }

        @Specialization
        public double sub(long a, double b) {
            return a - b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object sub(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).subtract(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        public Object sub(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).subtract(BigInteger.valueOf(b)));
        }

        @Specialization
        public double sub(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() - b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object sub(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).subtract(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object subCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MulNode extends BignumCoreMethodNode {

        public static MulNode create() {
            return MulNodeFactory.create(null);
        }

        public abstract Object executeMul(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        public int mul(int a, int b) {
            return Math.multiplyExact(a, b);
        }

        @Specialization
        public long mulWithOverflow(int a, int b) {
            return (long) a * (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long mul(long a, long b) {
            return Math.multiplyExact(a, b);
        }

        @TruffleBoundary
        @Specialization
        public Object mulWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
        }

        @Specialization
        public double mul(long a, double b) {
            return a * b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        public Object mul(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).multiply(Layouts.BIGNUM.getValue(b)));
        }

        @TruffleBoundary
        @Specialization
        public Object mul(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).multiply(BigInteger.valueOf(b)));
        }

        @Specialization
        public double mul(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() * b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        public Object mul(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).multiply(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object mul(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
        }

    }

    @CoreMethod(names = "/", required = 1)
    public abstract static class DivNode extends BignumCoreMethodNode {

        private final BranchProfile bGreaterZero = BranchProfile.create();
        private final BranchProfile bGreaterZeroAGreaterEqualZero = BranchProfile.create();
        private final BranchProfile bGreaterZeroALessZero = BranchProfile.create();
        private final BranchProfile aGreaterZero = BranchProfile.create();
        private final BranchProfile bMinusOne = BranchProfile.create();
        private final BranchProfile bMinusOneAMinimum = BranchProfile.create();
        private final BranchProfile bMinusOneANotMinimum = BranchProfile.create();
        private final BranchProfile finalCase = BranchProfile.create();

        public abstract Object executeDiv(Object a, Object b);

        // int

        @Specialization(rewriteOn = ArithmeticException.class)
        public Object divInt(int a, int b) {
            if (b > 0) {
                bGreaterZero.enter();
                if (a >= 0) {
                    bGreaterZeroAGreaterEqualZero.enter();
                    return a / b;
                } else {
                    bGreaterZeroALessZero.enter();
                    return (a + 1) / b - 1;
                }
            } else if (a > 0) {
                aGreaterZero.enter();
                return (a - 1) / b - 1;
            } else if (b == -1) {
                bMinusOne.enter();
                if (a == Integer.MIN_VALUE) {
                    bMinusOneAMinimum.enter();
                    return BigInteger.valueOf(a).negate();
                } else {
                    bMinusOneANotMinimum.enter();
                    return -a;
                }
            } else {
                finalCase.enter();
                return a / b;
            }
        }

        @Specialization
        public Object divIntFallback(int a, int b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            if (zeroProfile.profile(b == 0)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                return divInt(a, b);
            }
        }

        // long

        @Specialization(rewriteOn = ArithmeticException.class)
        public Object divLong(long a, long b) {
            if (b > 0) {
                bGreaterZero.enter();
                if (a >= 0) {
                    bGreaterZeroAGreaterEqualZero.enter();
                    return a / b;
                } else {
                    bGreaterZeroALessZero.enter();
                    return (a + 1) / b - 1;
                }
            } else if (a > 0) {
                aGreaterZero.enter();
                return (a - 1) / b - 1;
            } else if (b == -1) {
                bMinusOne.enter();
                if (a == Long.MIN_VALUE) {
                    bMinusOneAMinimum.enter();
                    return BigInteger.valueOf(a).negate();
                } else {
                    bMinusOneANotMinimum.enter();
                    return -a;
                }
            } else {
                finalCase.enter();
                return a / b;
            }
        }

        @Specialization
        public Object divLongFallback(long a, long b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            if (zeroProfile.profile(b == 0)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                return divLong(a, b);
            }
        }

        @Specialization
        public double div(long a, double b) {
            return a / b;
        }

        @Specialization(guards = { "isRubyBignum(b)", "!isLongMinValue(a)" })
        public int divBignum(long a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "isLongMinValue(a)" })
        public int divBignumEdgeCase(long a, DynamicObject b) {
            return -Layouts.BIGNUM.getValue(b).signum();
        }

        // Bignum

        @TruffleBoundary
        @Specialization
        public Object div(DynamicObject a, long b) {
            if (b == 0) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }
            final BigInteger bBigInt = BigInteger.valueOf(b);
            final BigInteger aBigInt = Layouts.BIGNUM.getValue(a);
            final BigInteger result = aBigInt.divide(bBigInt);
            if (result.signum() == -1 && !aBigInt.mod(bBigInt.abs()).equals(BigInteger.ZERO)) {
                return fixnumOrBignum(result.subtract(BigInteger.ONE));
            } else {
                return fixnumOrBignum(result);
            }
        }

        @TruffleBoundary
        @Specialization
        public double div(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() / b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        public Object div(DynamicObject a, DynamicObject b) {
            final BigInteger aBigInt = Layouts.BIGNUM.getValue(a);
            final BigInteger bBigInt = Layouts.BIGNUM.getValue(b);
            final BigInteger result = aBigInt.divide(bBigInt);
            if (result.signum() == -1 && !aBigInt.mod(bBigInt.abs()).equals(BigInteger.ZERO)) {
                return fixnumOrBignum(result.subtract(BigInteger.ONE));
            } else {
                return fixnumOrBignum(result);
            }
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object divCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
        }

        protected static boolean isLongMinValue(long a) {
            return a == Long.MIN_VALUE;
        }

    }

    // Defined in Java as we need to statically call #/
    @CoreMethod(names = "div", required = 1)
    public abstract static class IDivNode extends BignumCoreMethodNode {

        @Child private DivNode divNode = DivNodeFactory.create(null);
        @Child private FloatNodes.FloorNode floorNode = FloatNodesFactory.FloorNodeFactory.create(null);

        @Specialization
        public Object idiv(Object a, Object b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            Object quotient = divNode.executeDiv(a, b);
            if (quotient instanceof Double) {
                if (zeroProfile.profile((double) b == 0.0)) {
                    throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
                }
                return floorNode.executeFloor((double) quotient);
            } else {
                return quotient;
            }
        }

    }

    @CoreMethod(names = { "%", "modulo" }, required = 1)
    public abstract static class ModNode extends BignumCoreMethodNode {

        private final BranchProfile adjustProfile = BranchProfile.create();

        public abstract Object executeMod(Object a, Object b);

        @Specialization(guards = { "a >= 0", "b == cachedB", "isPowerOfTwo(cachedB)" })
        public int modPowerOfTwo(int a, int b,
                @Cached("b") int cachedB) {
            return a & (cachedB - 1);
        }

        protected static boolean isPowerOfTwo(int n) {
            return n > 0 && (n & (n - 1)) == 0;
        }

        @Specialization(replaces = "modPowerOfTwo")
        public int mod(int a, int b) {
            int mod = a % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                adjustProfile.enter();
                mod += b;
            }

            return mod;
        }

        @Specialization
        public double mod(long a, double b) {
            if (b == 0) {
                throw new ArithmeticException("divide by zero");
            }

            double mod = a % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                adjustProfile.enter();
                mod += b;
            }

            return mod;
        }

        @Specialization
        public long mod(long a, long b) {
            long mod = a % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                adjustProfile.enter();
                mod += b;
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        public Object mod(long a, DynamicObject b) {
            // TODO(CS): why are we getting this case?

            long mod = BigInteger.valueOf(a).mod(Layouts.BIGNUM.getValue(b)).longValue();

            if (mod < 0 && Layouts.BIGNUM.getValue(b).compareTo(BigInteger.ZERO) > 0 || mod > 0 && Layouts.BIGNUM.getValue(b).compareTo(BigInteger.ZERO) < 0) {
                return createBignum(BigInteger.valueOf(mod).add(Layouts.BIGNUM.getValue(b)));
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization
        public Object mod(DynamicObject a, long b) {
            if (b == 0) {
                throw new ArithmeticException("divide by zero");
            } else if (b < 0) {
                final BigInteger bigint = BigInteger.valueOf(b);
                final BigInteger mod = Layouts.BIGNUM.getValue(a).mod(bigint.negate());
                return fixnumOrBignum(mod.add(bigint));
            }
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).mod(BigInteger.valueOf(b)));
        }

        @TruffleBoundary
        @Specialization
        public double mod(DynamicObject a, double b) {
            if (b == 0) {
                throw new ArithmeticException("divide by zero");
            }

            double mod = Layouts.BIGNUM.getValue(a).doubleValue() % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                mod += b;
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        public Object mod(DynamicObject a, DynamicObject b) {
            final BigInteger bigint = Layouts.BIGNUM.getValue(b);
            final int compare = bigint.compareTo(BigInteger.ZERO);
            if (compare == 0) {
                throw new ArithmeticException("divide by zero");
            } else if (compare < 0) {
                final BigInteger mod = Layouts.BIGNUM.getValue(a).mod(bigint.negate());
                return fixnumOrBignum(mod.add(bigint));
            }
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).mod(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object modCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MODULO.getSymbol(), b);
        }

    }

    @Primitive(name = "integer_divmod")
    public abstract static class DivModNode extends PrimitiveArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        public DynamicObject divMod(long a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public DynamicObject divMod(long a, DynamicObject b) {
            return divModNode.execute(a, Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        public DynamicObject divMod(long a, double b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        public DynamicObject divMod(DynamicObject a, long b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), b);
        }

        @Specialization
        public DynamicObject divMod(DynamicObject a, double b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public DynamicObject divMod(DynamicObject a, DynamicObject b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object divModOther(Object a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "<", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class LessNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean less(int a, int b) {
            return a < b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean less(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization
        public boolean less(long a, long b) {
            return a < b;
        }

        @Specialization
        public boolean less(long a, double b) {
            return a < b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean less(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization
        public boolean less(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) < 0;
        }

        @Specialization
        public boolean less(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) < 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean less(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object lessCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().LESS_THAN.getSymbol(), b);
        }
    }

    @CoreMethod(names = "<=", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class LessEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean lessEqual(int a, int b) {
            return a <= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean lessEqual(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(long a, long b) {
            return a <= b;
        }

        @Specialization
        public boolean lessEqual(long a, double b) {
            return a <= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean lessEqual(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf((long) b)) <= 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean lessEqual(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object lessEqualCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().LESS_OR_EQUAL.getSymbol(), b);
        }

    }

    @CoreMethod(names = { "==", "===" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean equal(int a, DynamicObject b) {
            return false;
        }

        @Specialization
        public boolean equal(long a, long b) {
            return a == b;
        }

        @Specialization
        public boolean equal(long a, double b) {
            return a == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean equal(long a, DynamicObject b) {
            return false;
        }

        @Specialization
        public boolean equal(DynamicObject a, int b) {
            return false;
        }

        @Specialization
        public boolean equal(DynamicObject a, long b) {
            return false;
        }

        @Specialization
        public boolean equal(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean equal(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).equals(Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object equal(VirtualFrame frame, Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode reverseCallNode,
                @Cached("create()") BooleanCastNode booleanCastNode) {
            final Object reversedResult = reverseCallNode.call(frame, b, "==", a);
            return booleanCastNode.executeToBoolean(reversedResult);
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int compare(int a, int b,
                @Cached("createBinaryProfile()") ConditionProfile smallerProfile,
                @Cached("createBinaryProfile()") ConditionProfile equalProfile) {
            if (smallerProfile.profile(a < b)) {
                return -1;
            } else if (equalProfile.profile(a == b)) {
                return 0;
            } else {
                return +1;
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        public int compare(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        public int compare(long a, long b,
                @Cached("createBinaryProfile()") ConditionProfile smallerProfile,
                @Cached("createBinaryProfile()") ConditionProfile equalProfile) {
            if (smallerProfile.profile(a < b)) {
                return -1;
            } else if (equalProfile.profile(a == b)) {
                return 0;
            } else {
                return +1;
            }
        }

        @Specialization
        public int compare(long a, double b) {
            return Double.compare(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public int compare(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        public int compare(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b));
        }

        @Specialization(guards = "!isInfinity(b)")
        public int compare(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b);
        }

        @Specialization(guards = "isInfinity(b)")
        public int compareInfinity(DynamicObject a, double b) {
            if (b < 0) {
                return +1;
            } else {
                return -1;
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        public int compare(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object compare(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare_no_error", b);
        }

    }

    @CoreMethod(names = ">=", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class GreaterEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean greaterEqual(int a, int b) {
            return a >= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greaterEqual(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(long a, long b) {
            return a >= b;
        }

        @Specialization
        public boolean greaterEqual(long a, double b) {
            return a >= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greaterEqual(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) >= 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greaterEqual(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object greaterEqualCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().GREATER_OR_EQUAL.getSymbol(), b);
        }

    }

    @CoreMethod(names = ">", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class GreaterNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean greater(int a, int b) {
            return a > b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greater(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization
        public boolean greater(long a, long b) {
            return a > b;
        }

        @Specialization
        public boolean greater(long a, double b) {
            return a > b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greater(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization
        public boolean greater(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) > 0;
        }

        @Specialization
        public boolean greater(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) > 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean greater(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        public Object greaterCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().GREATER_THAN.getSymbol(), b);
        }

    }

    @CoreMethod(names = "~")
    public abstract static class ComplementNode extends BignumCoreMethodNode {

        @Specialization
        public int complement(int n) {
            return ~n;
        }

        @Specialization
        public long complement(long n) {
            return ~n;
        }

        @Specialization
        public Object complement(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).not());
        }

    }

    @CoreMethod(names = "&", required = 1)
    public abstract static class BitAndNode extends BignumCoreMethodNode {

        public abstract Object executeBitAnd(Object a, Object b);

        @Specialization
        public int bitAndIntInt(int a, int b) {
            return a & b;
        }

        @Specialization(guards = "a >= 0")
        public int bitAndIntLong(int a, long b) {
            return a & ((int) b);
        }

        @Specialization(guards = "b >= 0")
        public int bitAndLongInt(long a, int b) {
            return ((int) a) & b;
        }

        @Specialization
        public long bitAndLongLong(long a, long b) {
            return a & b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitAndBignum(int a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitAndBignum(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        public Object bitAnd(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).and(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitAnd(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        public Object bitAndCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_bit_coerced", coreStrings().AMPERSAND.getSymbol(), b);
        }

    }

    @CoreMethod(names = "|", required = 1)
    public abstract static class BitOrNode extends BignumCoreMethodNode {

        public abstract Object executeBitOr(Object a, Object b);

        @Specialization
        public int bitOr(int a, int b) {
            return a | b;
        }

        @Specialization
        public long bitOr(long a, long b) {
            return a | b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitOr(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).or(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        public Object bitOr(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).or(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitOr(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).or(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        public Object bitOrCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_bit_coerced", coreStrings().PIPE.getSymbol(), b);
        }

    }

    @CoreMethod(names = "^", required = 1)
    public abstract static class BitXOrNode extends BignumCoreMethodNode {

        @Specialization
        public int bitXOr(int a, int b) {
            return a ^ b;
        }

        @Specialization
        public long bitXOr(long a, long b) {
            return a ^ b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitXOr(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).xor(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        public Object bitXOr(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).xor(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object bitXOr(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).xor(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        public Object bitXOrCoerced(Object a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_bit_coerced", coreStrings().CIRCUMFLEX.getSymbol(), b);
        }

    }

    @CoreMethod(names = "<<", required = 1, lowerFixnum = 1)
    public abstract static class LeftShiftNode extends BignumCoreMethodNode {

        @Child private AbsNode absNode;
        @Child private RightShiftNode rightShiftNode;
        @Child private CallDispatchHeadNode fallbackCallNode;

        public abstract Object executeLeftShift(Object a, Object b);

        @Specialization(guards = { "b >= 0", "canShiftIntoInt(a, b)" })
        public int leftShift(int a, int b) {
            return a << b;
        }

        @Specialization(guards = { "b >= 0", "canShiftLongIntoInt(a, b)" })
        public int leftShift(long a, int b) {
            return (int) (a << b);
        }

        @Specialization(guards = { "b >= 0", "canShiftIntoLong(a, b)" })
        public long leftShiftToLong(long a, int b) {
            return a << b;
        }

        @Specialization(guards = "b >= 0")
        public Object leftShiftWithOverflow(long a, int b) {
            if (canShiftIntoLong(a, b)) {
                return leftShiftToLong(a, b);
            } else {
                return fixnumOrBignum(BigInteger.valueOf(a).shiftLeft(b));
            }
        }

        @Specialization(guards = "b < 0")
        public Object leftShiftNeg(long a, int b) {
            if (rightShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rightShiftNode = insert(RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, absoluteValue(b));
        }

        @Specialization(guards = "b < 0")
        public Object leftShiftNeg(int a, long b) {
            if (rightShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rightShiftNode = insert(RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, absoluteValue(b));
        }

        @TruffleBoundary
        @Specialization
        public Object leftShift(DynamicObject a, int b,
                @Cached("createBinaryProfile()") ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftLeft(b));
            } else {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftRight(-b));
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object leftShift(DynamicObject a, DynamicObject b,
                @Cached("create()") ToIntNode toIntNode) {
            final BigInteger bBigInt = Layouts.BIGNUM.getValue(b);
            if (bBigInt.signum() == -1) {
                return 0;
            } else {
                // MRI would raise a NoMemoryError; JRuby would raise a coercion error.
                return executeLeftShift(a, toIntNode.doInt(b));
            }
        }

        @Specialization(guards = "!isRubyInteger(b)")
        public Object leftShiftCoerced(Object a, Object b,
                @Cached("create()") ToIntNode toIntNode) {
            return executeLeftShift(a, toIntNode.doInt(b));
        }

        private Object absoluteValue(Object value) {
            if (absNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                absNode = insert(AbsNodeFactory.create(null));
            }
            return absNode.executeAbs(value);
        }

        static boolean canShiftIntoInt(int a, int b) {
            return Integer.numberOfLeadingZeros(a) - b > 0;
        }

        static boolean canShiftLongIntoInt(long a, int b) {
            return Long.numberOfLeadingZeros(a) - 32 - b > 0;
        }

        static boolean canShiftIntoLong(long a, int b) {
            return Long.numberOfLeadingZeros(a) - b > 0;
        }

    }

    @CoreMethod(names = ">>", required = 1, lowerFixnum = 1)
    public abstract static class RightShiftNode extends BignumCoreMethodNode {

        @Child private CallDispatchHeadNode fallbackCallNode;
        @Child private LeftShiftNode leftShiftNode;

        public abstract Object executeRightShift(Object a, Object b);

        @Specialization(guards = "b >= 0")
        public int rightShift(int a, int b,
                @Cached("createBinaryProfile()") ConditionProfile profile) {
            if (profile.profile(b >= Integer.SIZE - 1)) {
                return a < 0 ? -1 : 0;
            } else {
                return a >> b;
            }
        }

        @Specialization(guards = "b >= 0")
        public Object rightShift(long a, int b,
                @Cached("createBinaryProfile()") ConditionProfile profile) {
            if (profile.profile(b >= Long.SIZE - 1)) {
                return a < 0 ? -1 : 0; // int
            } else {
                return a >> b; // long
            }
        }

        @Specialization(guards = "b < 0")
        public Object rightShiftNeg(long a, int b) {
            if (leftShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                leftShiftNode = insert(LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, -b);
        }

        @Specialization(guards = "b >= 0")
        public int rightShift(long a, long b) { // b is not in int range due to
                                                // lowerFixnumParameters
            assert !CoreLibrary.fitsIntoInteger(b);
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "isPositive(b)" })
        public int rightShift(long a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "!isPositive(b)" })
        public Object rightShiftNeg(long a, DynamicObject b) {
            if (leftShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                leftShiftNode = insert(LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, Layouts.BIGNUM.getValue(b).negate());
        }

        @Specialization
        public Object rightShift(DynamicObject a, int b,
                @Cached("createBinaryProfile()") ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftRight(b));
            } else {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftLeft(-b));
            }
        }

        @Specialization
        public Object rightShift(DynamicObject a, long b) {
            assert !CoreLibrary.fitsIntoInteger(b);
            return 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public int rightShift(DynamicObject a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = "!isRubyInteger(b)")
        public Object rightShiftCoerced(Object a, Object b,
                @Cached("create()") ToIntNode toIntNode) {
            return executeRightShift(a, toIntNode.doInt(b));
        }

        protected static boolean isPositive(DynamicObject b) {
            return Layouts.BIGNUM.getValue(b).signum() >= 0;
        }

    }

    @CoreMethod(names = { "abs", "magnitude" })
    public abstract static class AbsNode extends BignumCoreMethodNode {

        public abstract Object executeAbs(Object a);

        @Specialization(rewriteOn = ArithmeticException.class)
        public int absIntInBounds(int n) {
            // TODO CS 13-Oct-16, use negateExact, but this isn't intrinsified by Graal yet
            return (n < 0) ? Math.subtractExact(0, n) : n;
        }

        @Specialization(replaces = "absIntInBounds")
        public Object abs(int n) {
            if (n == Integer.MIN_VALUE) {
                return -((long) n);
            }
            return (n < 0) ? -n : n;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long absInBounds(long n) {
            return (n < 0) ? Math.subtractExact(0, n) : n;
        }

        @Specialization(replaces = "absInBounds")
        public Object abs(long n) {
            if (n == Long.MIN_VALUE) {
                return createBignum(BigInteger.valueOf(n).abs());
            }
            return (n < 0) ? -n : n;
        }

        @Specialization
        public Object abs(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).abs());
        }

    }

    @CoreMethod(names = "bit_length")
    public abstract static class BitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int bitLength(int n) {
            if (n < 0) {
                n = ~n;
            }

            return Integer.SIZE - Integer.numberOfLeadingZeros(n);
        }

        @Specialization
        public int bitLength(long n) {
            if (n < 0) {
                n = ~n;
            }

            return Long.SIZE - Long.numberOfLeadingZeros(n);
        }

        @Specialization
        public int bitLength(DynamicObject value) {
            return Layouts.BIGNUM.getValue(value).bitLength();
        }

    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int size(long value) {
            return Long.BYTES;
        }

        @Specialization
        public int size(DynamicObject value) {
            return (Layouts.BIGNUM.getValue(value).bitLength() + 7) / 8;
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public double toF(int n) {
            return n;
        }

        @Specialization
        public double toF(long n) {
            return n;
        }

        @Specialization
        public double toF(DynamicObject value) {
            return Layouts.BIGNUM.getValue(value).doubleValue();
        }

    }

    @CoreMethod(names = { "to_s", "inspect" }, optional = 1, lowerFixnum = 1)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject toS(int n, NotProvided base) {
            return makeStringNode.fromRope(new LazyIntRope(n));
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject toS(long n, NotProvided base) {
            if (CoreLibrary.fitsIntoInteger(n)) {
                return toS((int) n, base);
            }

            return makeStringNode.executeMake(Long.toString(n), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject toS(DynamicObject value, NotProvided base) {
            return makeStringNode.executeMake(Layouts.BIGNUM.getValue(value).toString(), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject toS(long n, int base) {
            if (base == 10) {
                return toS(n, NotProvided.INSTANCE);
            }

            if (base < 2 || base > 36) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(Long.toString(n, base), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject toS(DynamicObject value, int base) {
            if (base < 2 || base > 36) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(Layouts.BIGNUM.getValue(value).toString(base), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @Primitive(name = "integer_memhash")
    public static abstract class IntegerMemhashNode extends PrimitiveArrayArgumentsNode {

        private static final int CLASS_SALT = 94974697; // random number, stops hashes for similar
                                                        // values but different classes being the
                                                        // same, static because we want
                                                        // deterministic hashes

        @Specialization
        public long memhashLongLong(long a, long b) {
            long h = getContext().getHashing(this).start(CLASS_SALT);
            h = Hashing.update(h, a);
            h = Hashing.update(h, b);
            return Hashing.end(h);
        }

    }

    @Primitive(name = "integer_fits_into_int")
    public static abstract class FixnumFitsIntoIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean fitsIntoIntInt(int a) {
            return true;
        }

        @Specialization
        public boolean fitsIntoIntLong(long a) {
            return CoreLibrary.fitsIntoInteger(a);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean fitsIntoIntBignum(DynamicObject b) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_uint")
    public static abstract class IntegerFitsIntoUIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean fitsIntoUIntInt(int a) {
            return true;
        }

        @Specialization
        public boolean fitsIntoUIntLong(long a) {
            return CoreLibrary.fitsIntoUnsignedInteger(a);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean fitsIntoUIntBignum(DynamicObject b) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_long")
    public static abstract class IntegerFitsIntoLongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean fitsIntoLongInt(int a) {
            return true;
        }

        @Specialization
        public boolean fitsIntoLongLong(long a) {
            return true;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean fitsIntoLongBignum(DynamicObject b) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_ulong")
    public static abstract class IntegerFitsIntoULongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean fitsIntoULongInt(int a) {
            return true;
        }

        @Specialization
        public boolean fitsIntoULongLong(long a) {
            return true;
        }

        @Specialization(guards = "isRubyBignum(b)")
        public boolean fitsIntoULongBignum(DynamicObject b) {
            BigInteger bi = Layouts.BIGNUM.getValue(b);
            if (bi.signum() >= 0) {
                return bi.bitLength() <= 64;
            } else {
                return false;
            }
        }

    }

    @Primitive(name = "integer_lower")
    public abstract static class IntegerLowerNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public int lower(int value) {
            return value;
        }

        @Specialization(guards = "canLower(value)")
        public int lower(long value) {
            return (int) value;
        }

        @Specialization(guards = "!canLower(value)")
        public long lowerFails(long value) {
            return value;
        }

        protected static boolean canLower(long value) {
            return CoreLibrary.fitsIntoInteger(value);
        }

    }

    @Primitive(name = "integer_ulong_from_bignum")
    public static abstract class IntegerULongFromBigNumNode extends PrimitiveArrayArgumentsNode {

        private static final BigInteger TWO_POW_64 = BigInteger.valueOf(1).shiftLeft(64);
        private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

        @Specialization(guards = "isRubyBignum(b)")
        public long uLongFromBignum(DynamicObject b,
                @Cached("createBinaryProfile()") ConditionProfile doesNotNeedsConversion) {
            final BigInteger value = Layouts.BIGNUM.getValue(b);
            assert value.signum() >= 0;
            if (doesNotNeedsConversion.profile(value.compareTo(LONG_MAX) < 1)) {
                return value.longValue();
            } else {
                return value.subtract(TWO_POW_64).longValue();
            }
        }
    }

    @Primitive(name = "integer_pow", lowerFixnum = { 0, 1 })
    public abstract static class PowNode extends PrimitiveArrayArgumentsNode {

        @Child private PowNode recursivePowNode;

        // Value taken from MRI for determining when to promote integer exponentiation into doubles.
        private static final int BIGLEN_LIMIT = 32 * 1024 * 1024;

        public abstract Object executePow(Object a, Object b);

        protected Object recursivePow(Object a, Object b) {
            if (recursivePowNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                recursivePowNode = insert(PowNodeFactory.create(null));
            }

            return recursivePowNode.executePow(a, b);
        }

        // Highest bit we can set is the 30th due to sign
        @Specialization(guards = { "a == 2", "b >= 0", "b <= 30" })
        public int powTwoInt(int a, int b) {
            return 1 << b;
        }

        // Highest bit we can set is the 62nd due to sign
        @Specialization(guards = { "a == 2", "b >= 0", "b <= 62" }, replaces = "powTwoInt")
        public long powTwoLong(int a, int b) {
            return 1L << b;
        }

        @ExplodeLoop
        @Specialization(guards = {
                "isIntOrLong(base)", "exponent == cachedExponent",
                "cachedExponent >= 0", "cachedExponent <= 10"
        }, limit = "getLimit()")
        public Object powConstantExponent(Object base, int exponent,
                @Cached("exponent") int cachedExponent,
                @Cached("create()") BranchProfile overflowProfile,
                @Cached("create()") MulNode mulNode) {
            Object result = 1;
            int exp = cachedExponent;
            while (exp > 0) {
                if ((exp & 1) == 0) {
                    base = mulNode.executeMul(base, base);
                    exp >>= 1;

                    if (base instanceof DynamicObject) {
                        overflowProfile.enter();
                        final Object bignumResult = recursivePow(base, exp);
                        return mulNode.executeMul(result, bignumResult);
                    }
                } else {
                    result = mulNode.executeMul(base, result);
                    exp--;
                }
            }
            return result;
        }

        @Specialization(guards = { "isIntOrLong(base)", "exponent >= 0" })
        public Object powLoop(Object base, long exponent,
                @Cached("create()") BranchProfile overflowProfile,
                @Cached("create()") MulNode mulNode) {
            Object result = 1;
            long exp = exponent;
            while (exp > 0) {
                if ((exp & 1) == 0) {
                    base = mulNode.executeMul(base, base);
                    exp >>= 1;

                    if (base instanceof DynamicObject) {
                        overflowProfile.enter();
                        final Object bignumResult = recursivePow(base, exp);
                        return mulNode.executeMul(result, bignumResult);
                    }
                } else {
                    result = mulNode.executeMul(base, result);
                    exp--;
                }
            }
            return result;
        }

        @Specialization(guards = "exponent < 0")
        public Object pow(long a, long exponent) {
            return FAILURE;
        }

        @Specialization
        public Object powDouble(long a, double b,
                @Cached("createBinaryProfile()") ConditionProfile complexProfile) {
            if (complexProfile.profile(a < 0)) {
                return FAILURE;
            } else {
                return Math.pow(a, b);
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Object powBignum(long a, DynamicObject b,
                @Cached("new()") WarnNode warnNode) {
            if (a == 0) {
                return 0;
            }

            if (a == 1) {
                return 1;
            }

            if (a == -1) {
                if (testBit(Layouts.BIGNUM.getValue(b), 0)) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (compareTo(Layouts.BIGNUM.getValue(b), BigInteger.ZERO) < 0) {
                return FAILURE;
            }

            warnNode.warningMessage(getContext().getCallStack().getTopMostUserSourceSection(), "in a**b, b may be too big");
            // b >= 2**63 && (a > 1 || a < -1) => larger than largest double
            // MRI behavior/bug: always positive Infinity even if a negative and b odd (likely due
            // to libc pow(a, +inf)).
            return Double.POSITIVE_INFINITY;
        }

        @Specialization
        public Object pow(DynamicObject a, long b,
                @Cached("createBinaryProfile()") ConditionProfile negativeProfile,
                @Cached("createBinaryProfile()") ConditionProfile maybeTooBigProfile,
                @Cached("new()") WarnNode warnNode) {
            if (negativeProfile.profile(b < 0)) {
                return FAILURE;
            } else {
                final BigInteger base = Layouts.BIGNUM.getValue(a);
                final int baseBitLength = base.bitLength();

                // Logic for promoting integer exponentiation into doubles taken from MRI.
                // We replicate the logic exactly so we match MRI's ranges.
                if (maybeTooBigProfile.profile(baseBitLength > BIGLEN_LIMIT || (baseBitLength * b > BIGLEN_LIMIT))) {
                    warnNode.warningMessage(getContext().getCallStack().getTopMostUserSourceSection(), "in a**b, b may be too big");
                    return powBigIntegerDouble(base, b);
                }

                // TODO CS 15-Feb-15 what about this cast?
                return createBignum(pow(base, (int) b));
            }
        }

        @Specialization
        public double pow(DynamicObject a, double b) {
            return powBigIntegerDouble(Layouts.BIGNUM.getValue(a), b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Void pow(DynamicObject a, DynamicObject b) {
            throw new UnsupportedOperationException();
        }

        @Specialization(guards = { "!isInteger(b)", "!isLong(b)", "!isDouble(b)", "!isRubyBignum(b)" })
        public Object pow(long a, Object b) {
            return FAILURE;
        }

        protected static boolean isIntOrLong(Object value) {
            return value instanceof Integer || value instanceof Long;
        }

        @TruffleBoundary
        private boolean testBit(BigInteger bigInteger, int n) {
            return bigInteger.testBit(n);
        }

        @TruffleBoundary
        private int compareTo(BigInteger a, BigInteger b) {
            return a.compareTo(b);
        }

        @TruffleBoundary
        private static BigInteger pow(BigInteger bigInteger, int exponent) {
            return bigInteger.pow(exponent);
        }

        @TruffleBoundary
        private static double powBigIntegerDouble(BigInteger bigInteger, double exponent) {
            return Math.pow(bigInteger.doubleValue(), exponent);
        }

        protected int getLimit() {
            return getContext().getOptions().POW_CACHE;
        }

    }

    @CoreMethod(names = "downto", needsBlock = true, required = 1, returnsEnumeratorIfNoBlock = true, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class DownToNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode downtoInternalCall;

        @Specialization
        public Object downto(int from, int to, DynamicObject block) {
            int count = 0;

            try {
                for (int i = from; i >= to; i--) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, i);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return nil();
        }

        @Specialization
        public Object downto(int from, double to, DynamicObject block) {
            return downto(from, (int) Math.ceil(to), block);
        }

        @Specialization
        public Object downto(long from, long to, DynamicObject block) {
            // TODO BJF 22-Apr-2015 how to handle reportLoopCount(long)
            int count = 0;

            try {
                for (long i = from; i >= to; i--) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, i);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return nil();
        }

        @Specialization
        public Object downto(long from, double to, DynamicObject block) {
            return downto(from, (long) Math.ceil(to), block);
        }

        @Specialization(guards = "isDynamicObject(from) || isDynamicObject(to)")
        public Object downto(VirtualFrame frame, Object from, Object to, DynamicObject block) {
            if (downtoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                downtoInternalCall = insert(CallDispatchHeadNode.createOnSelf());
            }

            return downtoInternalCall.callWithBlock(frame, from, "downto_internal", block, to);
        }

    }

    @CoreMethod(names = { "to_i", "to_int" })
    public abstract static class ToINode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int toI(int n) {
            return n;
        }

        @Specialization
        public long toI(long n) {
            return n;
        }

        @Specialization(guards = "isRubyBignum(n)")
        public DynamicObject toI(DynamicObject n) {
            return n;
        }

    }

    @CoreMethod(names = "upto", needsBlock = true, required = 1, returnsEnumeratorIfNoBlock = true, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class UpToNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode uptoInternalCall;

        @Specialization
        public Object upto(int from, int to, DynamicObject block) {
            int count = 0;

            try {
                for (int i = from; i <= to; i++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, i);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return nil();
        }

        @Specialization
        public Object upto(int from, double to, DynamicObject block) {
            return upto(from, (int) Math.floor(to), block);
        }

        @Specialization
        public Object upto(long from, long to, DynamicObject block) {
            int count = 0;

            try {
                for (long i = from; i <= to; i++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, i);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return nil();
        }

        @Specialization
        public Object upto(long from, double to, DynamicObject block) {
            return upto(from, (long) Math.ceil(to), block);
        }

        @Specialization(guards = "isDynamicObject(from) || isDynamicObject(to)")
        public Object upto(VirtualFrame frame, Object from, Object to, DynamicObject block) {
            if (uptoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                uptoInternalCall = insert(CallDispatchHeadNode.createOnSelf());
            }

            return uptoInternalCall.callWithBlock(frame, from, "upto_internal", block, to);
        }

    }

}
