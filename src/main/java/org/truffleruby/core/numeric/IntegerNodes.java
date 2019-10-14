/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
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

@CoreModule(value = "Integer", isClass = true)
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

        public abstract Object executeNeg(Object a);

        @Specialization(rewriteOn = ArithmeticException.class)
        protected int doInt(int value) {
            // TODO CS 13-Oct-16, use negateExact, but this isn't intrinsified by Graal yet
            return Math.subtractExact(0, value);
        }

        @Specialization(replaces = "doInt")
        protected Object doIntWithOverflow(int value) {
            if (value == Integer.MIN_VALUE) {
                return -((long) value);
            }
            return -value;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long doLong(long value) {
            return Math.subtractExact(0, value);
        }

        @Specialization(replaces = "doLong")
        protected Object doLongWihtOverflow(long value) {
            return fixnumOrBignum(BigInteger.valueOf(value).negate());
        }

        @Specialization
        protected Object doObject(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).negate());
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddNode extends BignumCoreMethodNode {

        public abstract Object executeAdd(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        protected int add(int a, int b) {
            return Math.addExact(a, b);
        }

        @Specialization
        protected long addWithOverflow(int a, int b) {
            return (long) a + (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long add(long a, long b) {
            return Math.addExact(a, b);
        }

        @Specialization
        protected Object addWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).add(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double add(long a, double b) {
            return a + b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object add(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).add(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        protected Object add(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).add(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double add(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() + b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object add(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).add(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object addCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubNode extends BignumCoreMethodNode {

        public abstract Object executeSub(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        protected int sub(int a, int b) {
            return Math.subtractExact(a, b);
        }

        @Specialization
        protected long subWithOverflow(int a, int b) {
            return (long) a - (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long sub(long a, long b) {
            return Math.subtractExact(a, b);
        }

        @Specialization
        protected Object subWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double sub(long a, double b) {
            return a - b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object sub(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).subtract(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        protected Object sub(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).subtract(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double sub(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() - b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object sub(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).subtract(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object subCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MulNode extends BignumCoreMethodNode {

        public static MulNode create() {
            return MulNodeFactory.create(null);
        }

        public abstract Object executeMul(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        protected int mul(int a, int b) {
            return Math.multiplyExact(a, b);
        }

        @Specialization
        protected long mulWithOverflow(int a, int b) {
            return (long) a * (long) b;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long mul(long a, long b) {
            return Math.multiplyExact(a, b);
        }

        @TruffleBoundary
        @Specialization
        protected Object mulWithOverflow(long a, long b) {
            return fixnumOrBignum(BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double mul(long a, double b) {
            return a * b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        protected Object mul(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).multiply(Layouts.BIGNUM.getValue(b)));
        }

        @TruffleBoundary
        @Specialization
        protected Object mul(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).multiply(BigInteger.valueOf(b)));
        }

        @Specialization
        protected double mul(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() * b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        protected Object mul(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).multiply(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object mul(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
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
        protected Object divInt(int a, int b) {
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
                    return -((long) Integer.MIN_VALUE);
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
        protected Object divIntFallback(int a, int b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            if (zeroProfile.profile(b == 0)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                return divInt(a, b);
            }
        }

        // long

        @Specialization(rewriteOn = ArithmeticException.class)
        protected Object divLong(long a, long b) {
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
                    return createBignum(BigInteger.valueOf(a).negate());
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
        protected Object divLongFallback(long a, long b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            if (zeroProfile.profile(b == 0)) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            } else {
                return divLong(a, b);
            }
        }

        @Specialization
        protected double div(long a, double b) {
            return a / b;
        }

        @Specialization(guards = { "isRubyBignum(b)", "!isLongMinValue(a)" })
        protected int divBignum(long a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "isLongMinValue(a)" })
        protected int divBignumEdgeCase(long a, DynamicObject b) {
            return -Layouts.BIGNUM.getValue(b).signum();
        }

        // Bignum

        @TruffleBoundary
        @Specialization
        protected Object div(DynamicObject a, long b) {
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
        protected double div(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() / b;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        protected Object div(DynamicObject a, DynamicObject b) {
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
        protected Object divCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
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
        protected Object idiv(Object a, Object b,
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
        protected int modPowerOfTwo(int a, int b,
                @Cached("b") int cachedB) {
            return a & (cachedB - 1);
        }

        protected static boolean isPowerOfTwo(int n) {
            return n > 0 && (n & (n - 1)) == 0;
        }

        @Specialization(replaces = "modPowerOfTwo")
        protected int mod(int a, int b) {
            int mod = a % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                adjustProfile.enter();
                mod += b;
            }

            return mod;
        }

        @Specialization
        protected double mod(long a, double b) {
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
        protected long mod(long a, long b) {
            long mod = a % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                adjustProfile.enter();
                mod += b;
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(b)")
        protected Object mod(long a, DynamicObject b) {
            // TODO(CS): why are we getting this case?

            long mod = BigInteger.valueOf(a).mod(Layouts.BIGNUM.getValue(b)).longValue();

            if (mod < 0 && Layouts.BIGNUM.getValue(b).compareTo(BigInteger.ZERO) > 0 ||
                    mod > 0 && Layouts.BIGNUM.getValue(b).compareTo(BigInteger.ZERO) < 0) {
                return createBignum(BigInteger.valueOf(mod).add(Layouts.BIGNUM.getValue(b)));
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization
        protected Object mod(DynamicObject a, long b) {
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
        protected double mod(DynamicObject a, double b) {
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
        protected Object mod(DynamicObject a, DynamicObject b) {
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
        protected Object modCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreStrings().MODULO.getSymbol(), b);
        }

    }

    @Primitive(name = "integer_divmod")
    public abstract static class DivModNode extends PrimitiveArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        protected DynamicObject divMod(long a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected DynamicObject divMod(long a, DynamicObject b) {
            return divModNode.execute(a, Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        protected DynamicObject divMod(long a, double b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected DynamicObject divMod(DynamicObject a, long b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), b);
        }

        @Specialization
        protected DynamicObject divMod(DynamicObject a, double b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected DynamicObject divMod(DynamicObject a, DynamicObject b) {
            return divModNode.execute(Layouts.BIGNUM.getValue(a), Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object divModOther(Object a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "<", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class LessNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean less(int a, int b) {
            return a < b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean less(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization
        protected boolean less(long a, long b) {
            return a < b;
        }

        @Specialization
        protected boolean less(long a, double b) {
            return a < b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean less(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization
        protected boolean less(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) < 0;
        }

        @Specialization
        protected boolean less(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) < 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean less(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) < 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().LESS_THAN.getSymbol(), b);
        }
    }

    @CoreMethod(names = "<=", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class LessEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean lessEqual(int a, int b) {
            return a <= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean lessEqual(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization
        protected boolean lessEqual(long a, long b) {
            return a <= b;
        }

        @Specialization
        protected boolean lessEqual(long a, double b) {
            return a <= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean lessEqual(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization
        protected boolean lessEqual(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) <= 0;
        }

        @Specialization
        protected boolean lessEqual(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf((long) b)) <= 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean lessEqual(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) <= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessEqualCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().LESS_OR_EQUAL.getSymbol(), b);
        }

    }

    @CoreMethod(names = { "==", "===" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean equal(int a, DynamicObject b) {
            return false;
        }

        @Specialization
        protected boolean equal(long a, long b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(long a, double b) {
            return a == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean equal(long a, DynamicObject b) {
            return false;
        }

        @Specialization
        protected boolean equal(DynamicObject a, int b) {
            return false;
        }

        @Specialization
        protected boolean equal(DynamicObject a, long b) {
            return false;
        }

        @Specialization
        protected boolean equal(DynamicObject a, double b) {
            return Layouts.BIGNUM.getValue(a).doubleValue() == b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean equal(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).equals(Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object equal(VirtualFrame frame, Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode reverseCallNode,
                @Cached BooleanCastNode booleanCastNode) {
            final Object reversedResult = reverseCallNode.call(b, "==", a);
            return booleanCastNode.executeToBoolean(reversedResult);
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int compare(int a, int b,
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
        protected int compare(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        protected int compare(long a, long b,
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
        protected int compare(long a, double b) {
            return Double.compare(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected int compare(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization
        protected int compare(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b));
        }

        @Specialization(guards = "!isInfinity(b)")
        protected int compare(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b);
        }

        @Specialization(guards = "isInfinity(b)")
        protected int compareInfinity(DynamicObject a, double b) {
            if (b < 0) {
                return +1;
            } else {
                return -1;
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected int compare(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object compare(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare_no_error", b);
        }

    }

    @CoreMethod(names = ">=", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class GreaterEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greaterEqual(int a, int b) {
            return a >= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greaterEqual(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization
        protected boolean greaterEqual(long a, long b) {
            return a >= b;
        }

        @Specialization
        protected boolean greaterEqual(long a, double b) {
            return a >= b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greaterEqual(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization
        protected boolean greaterEqual(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) >= 0;
        }

        @Specialization
        protected boolean greaterEqual(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) >= 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greaterEqual(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) >= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterEqualCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().GREATER_OR_EQUAL.getSymbol(), b);
        }

    }

    @CoreMethod(names = ">", required = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class GreaterNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greater(int a, int b) {
            return a > b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greater(int a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization
        protected boolean greater(long a, long b) {
            return a > b;
        }

        @Specialization
        protected boolean greater(long a, double b) {
            return a > b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greater(long a, DynamicObject b) {
            return BigInteger.valueOf(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization
        protected boolean greater(DynamicObject a, long b) {
            return Layouts.BIGNUM.getValue(a).compareTo(BigInteger.valueOf(b)) > 0;
        }

        @Specialization
        protected boolean greater(DynamicObject a, double b) {
            return Double.compare(Layouts.BIGNUM.getValue(a).doubleValue(), b) > 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected boolean greater(DynamicObject a, DynamicObject b) {
            return Layouts.BIGNUM.getValue(a).compareTo(Layouts.BIGNUM.getValue(b)) > 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreStrings().GREATER_THAN.getSymbol(), b);
        }

    }

    @CoreMethod(names = "~")
    public abstract static class ComplementNode extends BignumCoreMethodNode {

        @Specialization
        protected int complement(int n) {
            return ~n;
        }

        @Specialization
        protected long complement(long n) {
            return ~n;
        }

        @Specialization
        protected Object complement(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).not());
        }

    }

    @CoreMethod(names = "&", required = 1)
    public abstract static class BitAndNode extends BignumCoreMethodNode {

        public abstract Object executeBitAnd(Object a, Object b);

        @Specialization
        protected int bitAndIntInt(int a, int b) {
            return a & b;
        }

        @Specialization(guards = "a >= 0")
        protected int bitAndIntLong(int a, long b) {
            return a & ((int) b);
        }

        @Specialization(guards = "b >= 0")
        protected int bitAndLongInt(long a, int b) {
            return ((int) a) & b;
        }

        @Specialization
        protected long bitAndLongLong(long a, long b) {
            return a & b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitAndBignum(int a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitAndBignum(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        protected Object bitAnd(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).and(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitAnd(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).and(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitAndCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreStrings().AMPERSAND.getSymbol(), b);
        }

    }

    @CoreMethod(names = "|", required = 1)
    public abstract static class BitOrNode extends BignumCoreMethodNode {

        public abstract Object executeBitOr(Object a, Object b);

        @Specialization
        protected int bitOr(int a, int b) {
            return a | b;
        }

        @Specialization
        protected long bitOr(long a, long b) {
            return a | b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitOr(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).or(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        protected Object bitOr(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).or(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitOr(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).or(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitOrCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreStrings().PIPE.getSymbol(), b);
        }

    }

    @CoreMethod(names = "^", required = 1)
    public abstract static class BitXOrNode extends BignumCoreMethodNode {

        @Specialization
        protected int bitXOr(int a, int b) {
            return a ^ b;
        }

        @Specialization
        protected long bitXOr(long a, long b) {
            return a ^ b;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitXOr(long a, DynamicObject b) {
            return fixnumOrBignum(BigInteger.valueOf(a).xor(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization
        protected Object bitXOr(DynamicObject a, long b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).xor(BigInteger.valueOf(b)));
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object bitXOr(DynamicObject a, DynamicObject b) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(a).xor(Layouts.BIGNUM.getValue(b)));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitXOrCoerced(Object a, Object b,
                @Cached("createPrivate()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreStrings().CIRCUMFLEX.getSymbol(), b);
        }

    }

    @CoreMethod(names = "<<", required = 1, lowerFixnum = 1)
    public abstract static class LeftShiftNode extends BignumCoreMethodNode {

        @Child private AbsNode absNode;
        @Child private RightShiftNode rightShiftNode;
        @Child private CallDispatchHeadNode fallbackCallNode;

        public abstract Object executeLeftShift(Object a, Object b);

        @Specialization(guards = { "b >= 0", "canShiftIntoInt(a, b)" })
        protected int leftShift(int a, int b) {
            return a << b;
        }

        @Specialization(guards = { "b >= 0", "canShiftLongIntoInt(a, b)" })
        protected int leftShift(long a, int b) {
            return (int) (a << b);
        }

        @Specialization(guards = { "b >= 0", "canShiftIntoLong(a, b)" })
        protected long leftShiftToLong(long a, int b) {
            return a << b;
        }

        @Specialization(guards = "b >= 0")
        protected Object leftShiftWithOverflow(long a, int b) {
            if (canShiftIntoLong(a, b)) {
                return leftShiftToLong(a, b);
            } else {
                return fixnumOrBignum(BigInteger.valueOf(a).shiftLeft(b));
            }
        }

        @Specialization(guards = "b < 0")
        protected Object leftShiftNeg(long a, int b) {
            if (rightShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rightShiftNode = insert(RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, absoluteValue(b));
        }

        @Specialization(guards = "b < 0")
        protected Object leftShiftNeg(int a, long b) {
            if (rightShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rightShiftNode = insert(RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, absoluteValue(b));
        }

        @TruffleBoundary
        @Specialization
        protected Object leftShift(DynamicObject a, int b,
                @Cached("createBinaryProfile()") ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftLeft(b));
            } else {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftRight(-b));
            }
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected Object leftShift(DynamicObject a, DynamicObject b,
                @Cached ToIntNode toIntNode) {
            final BigInteger bBigInt = Layouts.BIGNUM.getValue(b);
            if (bBigInt.signum() == -1) {
                return 0;
            } else {
                // MRI would raise a NoMemoryError; JRuby would raise a coercion error.
                return executeLeftShift(a, toIntNode.doInt(b));
            }
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object leftShiftCoerced(Object a, Object b,
                @Cached ToIntNode toIntNode) {
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
        protected int rightShift(int a, int b,
                @Cached("createBinaryProfile()") ConditionProfile profile) {
            if (profile.profile(b >= Integer.SIZE - 1)) {
                return a < 0 ? -1 : 0;
            } else {
                return a >> b;
            }
        }

        @Specialization(guards = "b >= 0")
        protected Object rightShift(long a, int b,
                @Cached("createBinaryProfile()") ConditionProfile profile) {
            if (profile.profile(b >= Long.SIZE - 1)) {
                return a < 0 ? -1 : 0; // int
            } else {
                return a >> b; // long
            }
        }

        @Specialization(guards = "b < 0")
        protected Object rightShiftNeg(long a, int b) {
            if (leftShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                leftShiftNode = insert(LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, -b);
        }

        @Specialization(guards = "b >= 0")
        protected int rightShift(long a, long b) {
            // b is not in int range due to lowerFixnumParameters
            assert !CoreLibrary.fitsIntoInteger(b);
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "isPositive(b)" })
        protected int rightShift(long a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = { "isRubyBignum(b)", "!isPositive(b)" })
        protected Object rightShiftNeg(long a, DynamicObject b) {
            if (leftShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                leftShiftNode = insert(LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, Layouts.BIGNUM.getValue(b).negate());
        }

        @Specialization
        protected Object rightShift(DynamicObject a, int b,
                @Cached("createBinaryProfile()") ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftRight(b));
            } else {
                return fixnumOrBignum(Layouts.BIGNUM.getValue(a).shiftLeft(-b));
            }
        }

        @Specialization
        protected Object rightShift(DynamicObject a, long b) {
            assert !CoreLibrary.fitsIntoInteger(b);
            return 0;
        }

        @Specialization(guards = "isRubyBignum(b)")
        protected int rightShift(DynamicObject a, DynamicObject b) {
            return 0;
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object rightShiftCoerced(Object a, Object b,
                @Cached ToIntNode toIntNode) {
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
        protected int absIntInBounds(int n) {
            // TODO CS 13-Oct-16, use negateExact, but this isn't intrinsified by Graal yet
            return (n < 0) ? Math.subtractExact(0, n) : n;
        }

        @Specialization(replaces = "absIntInBounds")
        protected Object abs(int n) {
            if (n == Integer.MIN_VALUE) {
                return -((long) n);
            }
            return (n < 0) ? -n : n;
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long absInBounds(long n) {
            return (n < 0) ? Math.subtractExact(0, n) : n;
        }

        @Specialization(replaces = "absInBounds")
        protected Object abs(long n) {
            if (n == Long.MIN_VALUE) {
                return createBignum(BigInteger.valueOf(n).abs());
            }
            return (n < 0) ? -n : n;
        }

        @Specialization
        protected Object abs(DynamicObject value) {
            return fixnumOrBignum(Layouts.BIGNUM.getValue(value).abs());
        }

    }

    @CoreMethod(names = "bit_length")
    public abstract static class BitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int bitLength(int n) {
            if (n < 0) {
                n = ~n;
            }

            return Integer.SIZE - Integer.numberOfLeadingZeros(n);
        }

        @Specialization
        protected int bitLength(long n) {
            if (n < 0) {
                n = ~n;
            }

            return Long.SIZE - Long.numberOfLeadingZeros(n);
        }

        @Specialization
        protected int bitLength(DynamicObject value) {
            return Layouts.BIGNUM.getValue(value).bitLength();
        }

    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(long value) {
            return Long.BYTES;
        }

        @Specialization
        protected int size(DynamicObject value) {
            return (Layouts.BIGNUM.getValue(value).bitLength() + 7) / 8;
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double toF(int n) {
            return n;
        }

        @Specialization
        protected double toF(long n) {
            return n;
        }

        @Specialization
        protected double toF(DynamicObject value) {
            return Layouts.BIGNUM.getValue(value).doubleValue();
        }

    }

    @CoreMethod(names = { "to_s", "inspect" }, optional = 1, lowerFixnum = 1)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected DynamicObject toS(int n, NotProvided base) {
            return makeStringNode.fromRope(new LazyIntRope(n));
        }

        @TruffleBoundary
        @Specialization
        protected DynamicObject toS(long n, NotProvided base) {
            if (CoreLibrary.fitsIntoInteger(n)) {
                return toS((int) n, base);
            }

            return makeStringNode.executeMake(Long.toString(n), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        protected DynamicObject toS(DynamicObject value, NotProvided base) {
            return makeStringNode.executeMake(
                    Layouts.BIGNUM.getValue(value).toString(),
                    USASCIIEncoding.INSTANCE,
                    CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        protected DynamicObject toS(long n, int base) {
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
        protected DynamicObject toS(DynamicObject value, int base) {
            if (base < 2 || base > 36) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(
                    Layouts.BIGNUM.getValue(value).toString(base),
                    USASCIIEncoding.INSTANCE,
                    CodeRange.CR_7BIT);
        }

    }

    @Primitive(name = "integer_fits_into_int")
    public static abstract class FixnumFitsIntoIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoIntInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoIntLong(long a) {
            return CoreLibrary.fitsIntoInteger(a);
        }

        @Specialization(guards = "isRubyBignum(a)")
        protected boolean fitsIntoIntBignum(DynamicObject a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_uint")
    public static abstract class IntegerFitsIntoUIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoUIntInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoUIntLong(long a) {
            return CoreLibrary.fitsIntoUnsignedInteger(a);
        }

        @Specialization(guards = "isRubyBignum(a)")
        protected boolean fitsIntoUIntBignum(DynamicObject a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_long")
    public static abstract class IntegerFitsIntoLongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoLongInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoLongLong(long a) {
            return true;
        }

        @Specialization(guards = "isRubyBignum(a)")
        protected boolean fitsIntoLongBignum(DynamicObject a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_ulong")
    public static abstract class IntegerFitsIntoULongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoULongInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoULongLong(long a) {
            return true;
        }

        @Specialization(guards = "isRubyBignum(a)")
        protected boolean fitsIntoULongBignum(DynamicObject a) {
            BigInteger bi = Layouts.BIGNUM.getValue(a);
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
        protected int lower(int value) {
            return value;
        }

        @Specialization(guards = "canLower(value)")
        protected int lower(long value) {
            return (int) value;
        }

        @Specialization(guards = "!canLower(value)")
        protected long lowerFails(long value) {
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
        protected long uLongFromBignum(DynamicObject b,
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
        @Specialization(guards = { "base == 2", "exponent >= 0", "exponent <= 30" })
        protected int powTwoInt(int base, int exponent) {
            return 1 << exponent;
        }

        // Highest bit we can set is the 62nd due to sign
        @Specialization(guards = { "base == 2", "exponent >= 0", "exponent <= 62" }, replaces = "powTwoInt")
        protected long powTwoLong(int base, int exponent) {
            return 1L << exponent;
        }

        @ExplodeLoop
        @Specialization(
                guards = {
                        "isIntOrLong(base)",
                        "exponent == cachedExponent",
                        "cachedExponent >= 0",
                        "cachedExponent <= 10" },
                limit = "getLimit()")
        protected Object powConstantExponent(Object base, int exponent,
                @Cached("exponent") int cachedExponent,
                @Cached BranchProfile overflowProfile,
                @Cached MulNode mulNode) {
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
        protected Object powLoop(Object base, long exponent,
                @Cached BranchProfile overflowProfile,
                @Cached MulNode mulNode) {
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
        protected Object pow(long base, long exponent) {
            return FAILURE;
        }

        @Specialization
        protected Object powDouble(long base, double exponent,
                @Cached("createBinaryProfile()") ConditionProfile complexProfile) {
            if (complexProfile.profile(base < 0)) {
                return FAILURE;
            } else {
                return Math.pow(base, exponent);
            }
        }

        @Specialization(guards = "isRubyBignum(exponent)")
        protected Object powBignum(long base, DynamicObject exponent,
                @Cached("new()") WarnNode warnNode) {
            if (base == 0) {
                return 0;
            }

            if (base == 1) {
                return 1;
            }

            if (base == -1) {
                if (testBit(Layouts.BIGNUM.getValue(exponent), 0)) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (compareTo(Layouts.BIGNUM.getValue(exponent), BigInteger.ZERO) < 0) {
                return FAILURE;
            }

            warnNode.warningMessage(
                    getContext().getCallStack().getTopMostUserSourceSection(),
                    "in a**b, b may be too big");
            // b >= 2**63 && (a > 1 || a < -1) => larger than largest double
            // MRI behavior/bug: always positive Infinity even if a negative and b odd (likely due
            // to libc pow(a, +inf)).
            return Double.POSITIVE_INFINITY;
        }

        @Specialization
        protected Object pow(DynamicObject base, long exponent,
                @Cached("createBinaryProfile()") ConditionProfile negativeProfile,
                @Cached("createBinaryProfile()") ConditionProfile maybeTooBigProfile,
                @Cached("new()") WarnNode warnNode) {
            if (negativeProfile.profile(exponent < 0)) {
                return FAILURE;
            } else {
                final BigInteger bigIntegerBase = Layouts.BIGNUM.getValue(base);
                final int baseBitLength = bigIntegerBase.bitLength();

                // Logic for promoting integer exponentiation into doubles taken from MRI.
                // We replicate the logic exactly so we match MRI's ranges.
                if (maybeTooBigProfile
                        .profile(baseBitLength > BIGLEN_LIMIT || (baseBitLength * exponent > BIGLEN_LIMIT))) {
                    warnNode.warningMessage(
                            getContext().getCallStack().getTopMostUserSourceSection(),
                            "in a**b, b may be too big");
                    return powBigIntegerDouble(bigIntegerBase, exponent);
                }

                // TODO CS 15-Feb-15 what about this cast?
                return createBignum(pow(bigIntegerBase, (int) exponent));
            }
        }

        @Specialization
        protected Object pow(DynamicObject base, double exponent) {
            double doublePow = powBigIntegerDouble(Layouts.BIGNUM.getValue(base), exponent);
            if (Double.isNaN(doublePow)) {
                // Instead of returning NaN, run the fallback code which can create a complex result
                return FAILURE;
            } else {
                return doublePow;
            }
        }

        @Specialization(guards = "isRubyBignum(exponent)")
        protected Object pow(DynamicObject base, DynamicObject exponent) {
            return FAILURE;
        }

        @Specialization(
                guards = {
                        "!isInteger(exponent)",
                        "!isLong(exponent)",
                        "!isDouble(exponent)",
                        "!isRubyBignum(exponent)" })
        protected Object pow(Object base, Object exponent) {
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

    @CoreMethod(
            names = "downto",
            needsBlock = true,
            required = 1,
            returnsEnumeratorIfNoBlock = true,
            unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class DownToNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode downtoInternalCall;

        @Specialization
        protected Object downto(int from, int to, DynamicObject block) {
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
        protected Object downto(int from, double to, DynamicObject block) {
            return downto(from, (int) Math.ceil(to), block);
        }

        @Specialization
        protected Object downto(long from, long to, DynamicObject block) {
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
        protected Object downto(long from, double to, DynamicObject block) {
            return downto(from, (long) Math.ceil(to), block);
        }

        @Specialization(guards = "isDynamicObject(from) || isDynamicObject(to)")
        protected Object downto(VirtualFrame frame, Object from, Object to, DynamicObject block) {
            if (downtoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                downtoInternalCall = insert(CallDispatchHeadNode.createPrivate());
            }

            return downtoInternalCall.callWithBlock(from, "downto_internal", block, to);
        }

    }

    @CoreMethod(names = { "to_i", "to_int" })
    public abstract static class ToINode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int toI(int n) {
            return n;
        }

        @Specialization
        protected long toI(long n) {
            return n;
        }

        @Specialization(guards = "isRubyBignum(n)")
        protected DynamicObject toI(DynamicObject n) {
            return n;
        }

    }

    @CoreMethod(
            names = "upto",
            needsBlock = true,
            required = 1,
            returnsEnumeratorIfNoBlock = true,
            unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class UpToNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode uptoInternalCall;

        @Specialization
        protected Object upto(int from, int to, DynamicObject block) {
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
        protected Object upto(int from, double to, DynamicObject block) {
            return upto(from, (int) Math.floor(to), block);
        }

        @Specialization
        protected Object upto(long from, long to, DynamicObject block) {
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
        protected Object upto(long from, double to, DynamicObject block) {
            return upto(from, (long) Math.ceil(to), block);
        }

        @Specialization(guards = "isDynamicObject(from) || isDynamicObject(to)")
        protected Object upto(VirtualFrame frame, Object from, Object to, DynamicObject block) {
            if (uptoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                uptoInternalCall = insert(CallDispatchHeadNode.createPrivate());
            }

            return uptoInternalCall.callWithBlock(from, "upto_internal", block, to);
        }

    }

}
