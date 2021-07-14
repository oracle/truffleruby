/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BigIntegerCastNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.numeric.IntegerNodesFactory.AbsNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.DivNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.LeftShiftNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.MulNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.PowNodeFactory;
import org.truffleruby.core.numeric.IntegerNodesFactory.RightShiftNodeFactory;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.LazyIntRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.NoImplicitCastsToLong;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Integer", isClass = true)
public abstract class IntegerNodes {

    public abstract static class BignumCoreMethodNode extends CoreMethodArrayArgumentsNode {

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
            return Math.negateExact(value);
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
        protected Object doLongWithOverflow(long value) {
            return fixnumOrBignum(BigIntegerOps.negate(value));
        }

        @Specialization
        protected Object doObject(RubyBignum value) {
            return fixnumOrBignum(BigIntegerOps.negate(value.value));
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
            return fixnumOrBignum(BigIntegerOps.add(a, b));
        }

        @Specialization
        protected double add(long a, double b) {
            return a + b;
        }

        @Specialization
        protected Object add(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.add(b.value, a));
        }

        @Specialization
        protected Object add(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.add(a.value, b));
        }

        @Specialization
        protected double add(RubyBignum a, double b) {
            return BigIntegerOps.doubleValue(a.value) + b;
        }

        @Specialization
        protected Object add(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.add(a.value, b.value));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object addCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().PLUS, b);
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
            return fixnumOrBignum(BigIntegerOps.subtract(a, b));
        }

        @Specialization
        protected double sub(long a, double b) {
            return a - b;
        }

        @Specialization
        protected Object sub(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.subtract(a, b.value));
        }

        @Specialization
        protected Object sub(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.subtract(a.value, b));
        }

        @Specialization
        protected double sub(RubyBignum a, double b) {
            return BigIntegerOps.doubleValue(a.value) - b;
        }

        @Specialization
        protected Object sub(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.subtract(a.value, b.value));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object subCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MINUS, b);
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

        @Specialization
        protected Object mulWithOverflow(long a, long b) {
            return fixnumOrBignum(BigIntegerOps.multiply(a, b));
        }

        @Specialization
        protected double mul(long a, double b) {
            return a * b;
        }

        @Specialization
        protected Object mul(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.multiply(b.value, a));
        }

        @Specialization
        protected Object mul(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.multiply(a.value, b));
        }

        @Specialization
        protected double mul(RubyBignum a, double b) {
            return BigIntegerOps.doubleValue(a.value) * b;
        }

        @Specialization
        protected Object mul(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.multiply(a.value, b.value));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object mul(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MULTIPLY, b);
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
                @Cached ConditionProfile zeroProfile) {
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
                    return createBignum(BigIntegerOps.negate(a));
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
                @Cached ConditionProfile zeroProfile) {
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

        @Specialization(guards = { "!isLongMinValue(a)" })
        protected int divBignum(long a, RubyBignum b) {
            return 0;
        }

        @Specialization(guards = { "isLongMinValue(a)" })
        protected int divBignumEdgeCase(long a, RubyBignum b) {
            return -b.value.signum();
        }

        // Bignum

        @TruffleBoundary
        @Specialization
        protected Object div(RubyBignum a, long b) {
            if (b == 0) {
                throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
            }
            final BigInteger bBigInt = BigInteger.valueOf(b);
            final BigInteger aBigInt = a.value;
            final BigInteger result = aBigInt.divide(bBigInt);
            if (result.signum() == -1 && !aBigInt.mod(bBigInt.abs()).equals(BigInteger.ZERO)) {
                return fixnumOrBignum(result.subtract(BigInteger.ONE));
            } else {
                return fixnumOrBignum(result);
            }
        }

        @Specialization
        protected double div(RubyBignum a, double b) {
            return BigIntegerOps.doubleValue(a) / b;
        }

        @TruffleBoundary
        @Specialization
        protected Object div(RubyBignum a, RubyBignum b) {
            final BigInteger aBigInt = a.value;
            final BigInteger bBigInt = b.value;
            final BigInteger result = aBigInt.divide(bBigInt);
            if (result.signum() == -1 && !aBigInt.mod(bBigInt.abs()).equals(BigInteger.ZERO)) {
                return fixnumOrBignum(result.subtract(BigInteger.ONE));
            } else {
                return fixnumOrBignum(result);
            }
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object divCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().DIVIDE, b);
        }

        protected static boolean isLongMinValue(long a) {
            return a == Long.MIN_VALUE;
        }

    }

    // Defined in Java as we need to statically call #/
    @CoreMethod(names = "div", required = 1)
    public abstract static class IDivNode extends BignumCoreMethodNode {

        @Child private DivNode divNode = DivNodeFactory.create(null);
        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        protected Object idiv(Object a, Object b,
                @Cached ConditionProfile zeroProfile) {
            Object quotient = divNode.executeDiv(a, b);
            if (quotient instanceof Double) {
                if (zeroProfile.profile((double) b == 0.0)) {
                    throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
                }
                return fixnumOrBignum.fixnumOrBignum(Math.floor((double) quotient));
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
        @Specialization
        protected Object mod(long a, RubyBignum b) {
            // TODO(CS): why are we getting this case?

            long mod = BigInteger.valueOf(a).mod(b.value).longValue();

            if (mod < 0 && b.value.compareTo(BigInteger.ZERO) > 0 ||
                    mod > 0 && b.value.compareTo(BigInteger.ZERO) < 0) {
                return createBignum(BigInteger.valueOf(mod).add(b.value));
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization
        protected Object mod(RubyBignum a, long b) {
            if (b == 0) {
                throw new ArithmeticException("divide by zero");
            } else if (b < 0) {
                final BigInteger bigint = BigInteger.valueOf(b);
                final BigInteger mod = a.value.mod(bigint.negate());
                return fixnumOrBignum(mod.add(bigint));
            }
            return fixnumOrBignum(a.value.mod(BigInteger.valueOf(b)));
        }

        @TruffleBoundary // exception throw + BigInteger
        @Specialization
        protected double mod(RubyBignum a, double b) {
            if (b == 0) {
                throw new ArithmeticException("divide by zero");
            }

            double mod = a.value.doubleValue() % b;

            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                mod += b;
            }

            return mod;
        }

        @TruffleBoundary
        @Specialization
        protected Object mod(RubyBignum a, RubyBignum b) {
            final BigInteger bigint = b.value;
            final int compare = bigint.compareTo(BigInteger.ZERO);
            if (compare == 0) {
                throw new ArithmeticException("divide by zero");
            } else if (compare < 0) {
                final BigInteger mod = a.value.mod(bigint.negate());
                return fixnumOrBignum(mod.add(bigint));
            }
            return fixnumOrBignum(a.value.mod(b.value));
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object modCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_coerced", coreSymbols().MODULO, b);
        }

    }

    @Primitive(name = "integer_divmod")
    public abstract static class DivModNode extends PrimitiveArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        protected RubyArray divMod(long a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected RubyArray divMod(long a, RubyBignum b) {
            return divModNode.execute(a, b.value);
        }

        @Specialization
        protected RubyArray divMod(long a, double b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        protected RubyArray divMod(RubyBignum a, long b) {
            return divModNode.execute(a.value, b);
        }

        @Specialization
        protected RubyArray divMod(RubyBignum a, double b) {
            return divModNode.execute(a.value, b);
        }

        @Specialization
        protected RubyArray divMod(RubyBignum a, RubyBignum b) {
            return divModNode.execute(a.value, b.value);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object divModOther(Object a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class LessNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean less(int a, int b) {
            return a < b;
        }

        @Specialization
        protected boolean less(long a, long b) {
            return a < b;
        }

        @Specialization
        protected boolean less(long a, double b) {
            return a < b;
        }

        @Specialization
        protected boolean less(long a, RubyBignum b) {
            return BigIntegerOps.isPositive(b); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean less(RubyBignum a, long b) {
            return BigIntegerOps.isNegative(a); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean less(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b) < 0;
        }

        @Specialization
        protected boolean less(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) < 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessCoerced(Object a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().LESS_THAN, b);
        }
    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class LessEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean lessEqual(int a, int b) {
            return a <= b;
        }

        @Specialization
        protected boolean lessEqual(long a, long b) {
            return a <= b;
        }

        @Specialization
        protected boolean lessEqual(long a, double b) {
            return a <= b;
        }

        @Specialization
        protected boolean lessEqual(long a, RubyBignum b) {
            return BigIntegerOps.isPositive(b); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean lessEqual(RubyBignum a, long b) {
            return BigIntegerOps.isNegative(a); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean lessEqual(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b) <= 0;
        }

        @Specialization
        protected boolean lessEqual(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) <= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object lessEqualCoerced(Object a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().LEQ, b);
        }

    }

    @CoreMethod(names = { "==", "===" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(int a, RubyBignum b) {
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

        @Specialization
        protected boolean equal(long a, RubyBignum b) {
            return false;
        }

        @Specialization
        protected boolean equal(RubyBignum a, long b) {
            return false;
        }

        @Specialization
        protected boolean equal(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b) == 0;
        }

        @Specialization
        protected boolean equal(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.equals(a.value, b.value);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object equal(VirtualFrame frame, Object a, Object b,
                @Cached DispatchNode reverseCallNode,
                @Cached BooleanCastNode booleanCastNode) {
            final Object reversedResult = reverseCallNode.call(b, "==", a);
            return booleanCastNode.executeToBoolean(reversedResult);
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int compare(int a, int b,
                @Cached ConditionProfile smallerProfile,
                @Cached ConditionProfile equalProfile) {
            if (smallerProfile.profile(a < b)) {
                return -1;
            } else if (equalProfile.profile(a == b)) {
                return 0;
            } else {
                return +1;
            }
        }

        @Specialization
        protected int compare(long a, long b,
                @Cached ConditionProfile smallerProfile,
                @Cached ConditionProfile equalProfile) {
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

        @Specialization
        protected int compare(long a, RubyBignum b) {
            return BigIntegerOps.compare(a, b);
        }

        @Specialization
        protected int compare(RubyBignum a, long b) {
            return BigIntegerOps.compare(a, b);
        }

        @Specialization(guards = "!isInfinity(b)")
        protected int compare(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b);
        }

        @Specialization(guards = "isInfinity(b)")
        protected int compareInfinity(RubyBignum a, double b) {
            if (b < 0) {
                return +1;
            } else {
                return -1;
            }
        }

        @Specialization
        protected int compare(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.compare(a, b);
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object compare(Object a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare_no_error", b);
        }

    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class GreaterEqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greaterEqual(int a, int b) {
            return a >= b;
        }

        @Specialization
        protected boolean greaterEqual(long a, long b) {
            return a >= b;
        }

        @Specialization
        protected boolean greaterEqual(long a, double b) {
            return a >= b;
        }

        @Specialization
        protected boolean greaterEqual(long a, RubyBignum b) {
            return BigIntegerOps.isNegative(b); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean greaterEqual(RubyBignum a, long b) {
            return BigIntegerOps.isPositive(a); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean greaterEqual(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b) >= 0;
        }

        @Specialization
        protected boolean greaterEqual(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) >= 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterEqualCoerced(Object a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().GEQ, b);
        }

    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class GreaterNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean greater(int a, int b) {
            return a > b;
        }

        @Specialization
        protected boolean greater(long a, long b) {
            return a > b;
        }

        @Specialization
        protected boolean greater(long a, double b) {
            return a > b;
        }

        @Specialization
        protected boolean greater(long a, RubyBignum b) {
            return BigIntegerOps.isNegative(b); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean greater(RubyBignum a, long b) {
            return BigIntegerOps.isPositive(a); // Bignums are never long-valued.
        }

        @Specialization
        protected boolean greater(RubyBignum a, double b) {
            return BigIntegerOps.compare(a, b) > 0;
        }

        @Specialization
        protected boolean greater(RubyBignum a, RubyBignum b) {
            return BigIntegerOps.compare(a, b) > 0;
        }

        @Specialization(guards = "!isRubyNumber(b)")
        protected Object greaterCoerced(Object a, Object b,
                @Cached DispatchNode redoCompare) {
            return redoCompare.call(a, "redo_compare", coreSymbols().GREATER_THAN, b);
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
        protected Object complement(RubyBignum value) {
            return fixnumOrBignum(BigIntegerOps.not(value.value));
        }

    }

    @TypeSystemReference(NoImplicitCastsToLong.class)
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

        @Specialization(guards = "a < 0")
        protected long bitAndIntLongNegative(int a, long b) {
            return a & b;
        }

        @Specialization(guards = "b >= 0")
        protected int bitAndLongInt(long a, int b) {
            return ((int) a) & b;
        }

        @Specialization(guards = "b < 0")
        protected long bitAndLongIntNegative(long a, int b) {
            return a & b;
        }

        @Specialization
        protected long bitAndLongLong(long a, long b) {
            return a & b;
        }

        @Specialization
        protected Object bitAndBignum(int a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.and(b.value, a));
        }

        @Specialization
        protected Object bitAndBignum(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.and(b.value, a));
        }

        @Specialization
        protected Object bitAnd(RubyBignum a, int b) {
            return fixnumOrBignum(BigIntegerOps.and(a.value, b));
        }

        @Specialization
        protected Object bitAnd(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.and(a.value, b));
        }

        @Specialization
        protected Object bitAnd(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.and(a.value, b.value));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitAndCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreSymbols().AMPERSAND, b);
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

        @Specialization
        protected Object bitOr(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.or(b.value, a));
        }

        @Specialization
        protected Object bitOr(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.or(a.value, b));
        }

        @Specialization
        protected Object bitOr(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.or(a.value, b.value));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitOrCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreSymbols().PIPE, b);
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

        @Specialization
        protected Object bitXOr(long a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.xor(b.value, a));
        }

        @Specialization
        protected Object bitXOr(RubyBignum a, long b) {
            return fixnumOrBignum(BigIntegerOps.xor(a.value, b));
        }

        @Specialization
        protected Object bitXOr(RubyBignum a, RubyBignum b) {
            return fixnumOrBignum(BigIntegerOps.xor(a.value, b.value));
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object bitXOrCoerced(Object a, Object b,
                @Cached DispatchNode redoCoerced) {
            return redoCoerced.call(a, "redo_bit_coerced", coreSymbols().CIRCUMFLEX, b);
        }

    }

    @CoreMethod(names = "<<", required = 1, lowerFixnum = 1)
    public abstract static class LeftShiftNode extends BignumCoreMethodNode {

        @Child private AbsNode absNode;
        @Child private RightShiftNode rightShiftNode;
        @Child private DispatchNode fallbackCallNode;

        public abstract Object executeLeftShift(Object a, Object b);


        public static LeftShiftNode create() {
            return LeftShiftNodeFactory.create(null);
        }

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
                return fixnumOrBignum(BigIntegerOps.shiftLeft(a, b));
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

        @Specialization(guards = "b < 0")
        protected Object leftShiftNeg(long a, long b) {
            if (rightShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rightShiftNode = insert(RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, absoluteValue(b));
        }

        @Specialization
        protected Object leftShift(RubyBignum a, int b,
                @Cached ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(BigIntegerOps.shiftLeft(a.value, b));
            } else {
                return fixnumOrBignum(BigIntegerOps.shiftRight(a.value, -b));
            }
        }

        @Specialization
        protected Object leftShift(RubyBignum a, RubyBignum b,
                @Cached ToIntNode toIntNode) {
            final BigInteger bBigInt = b.value;
            if (bBigInt.signum() == -1) {
                return 0;
            } else {
                // We raise a RangeError.
                // MRI would raise a NoMemoryError; JRuby would raise a coercion error.
                toIntNode.execute(b);
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object leftShiftCoerced(Object a, Object b,
                @Cached ToRubyIntegerNode toRubyIntNode,
                @Cached LeftShiftNode leftShiftNode) {
            return leftShiftNode.executeLeftShift(a, toRubyIntNode.execute(b));
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

        @Child private DispatchNode fallbackCallNode;
        @Child private LeftShiftNode leftShiftNode;

        public abstract Object executeRightShift(Object a, Object b);

        public static RightShiftNode create() {
            return RightShiftNodeFactory.create(null);
        }

        @Specialization(guards = "b >= 0")
        protected int rightShift(int a, int b,
                @Cached ConditionProfile profile) {
            if (profile.profile(b >= Integer.SIZE - 1)) {
                return a < 0 ? -1 : 0;
            } else {
                return a >> b;
            }
        }

        @Specialization(guards = "b >= 0")
        protected Object rightShift(long a, int b,
                @Cached ConditionProfile profile) {
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

        @Specialization(guards = "b < 0")
        protected Object rightShiftNeg(long a, long b) {
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

        @Specialization(guards = { "isPositive(b)" })
        protected int rightShift(long a, RubyBignum b) {
            return 0;
        }

        @Specialization(guards = { "!isPositive(b)" })
        protected Object rightShiftNeg(long a, RubyBignum b) {
            if (leftShiftNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                leftShiftNode = insert(LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, BigIntegerOps.negate(b.value));
        }

        @Specialization
        protected Object rightShift(RubyBignum a, int b,
                @Cached ConditionProfile bPositive) {
            if (bPositive.profile(b >= 0)) {
                return fixnumOrBignum(BigIntegerOps.shiftRight(a.value, b));
            } else {
                return fixnumOrBignum(BigIntegerOps.shiftLeft(a.value, -b));
            }
        }

        @Specialization
        protected Object rightShift(RubyBignum a, long b) {
            assert !CoreLibrary.fitsIntoInteger(b);
            return 0;
        }

        @Specialization
        protected int rightShift(RubyBignum a, RubyBignum b) {
            return 0;
        }

        @Specialization(guards = "!isRubyInteger(b)")
        protected Object rightShiftCoerced(Object a, Object b,
                @Cached ToRubyIntegerNode toRubyIntNode,
                @Cached RightShiftNode rightShiftNode) {
            return rightShiftNode.executeRightShift(a, toRubyIntNode.execute(b));
        }

        protected static boolean isPositive(RubyBignum b) {
            return b.value.signum() >= 0;
        }

    }

    @CoreMethod(names = { "abs", "magnitude" })
    public abstract static class AbsNode extends BignumCoreMethodNode {

        public abstract Object executeAbs(Object a);

        @Specialization(rewriteOn = ArithmeticException.class)
        protected int absIntInBounds(int n) {
            return (n < 0) ? Math.negateExact(n) : n;
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
                return createBignum(BigIntegerOps.abs(n));
            }
            return (n < 0) ? -n : n;
        }

        @Specialization
        protected Object abs(RubyBignum value) {
            return fixnumOrBignum(BigIntegerOps.abs(value.value));
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
        protected int bitLength(RubyBignum value) {
            return BigIntegerOps.bitLength(value.value);
        }

    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(long value) {
            return Long.BYTES;
        }

        @Specialization
        protected int size(RubyBignum value) {
            return (BigIntegerOps.bitLength(value.value) + 7) / 8;
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
        protected double toF(RubyBignum value) {
            return BigIntegerOps.doubleValue(value.value);
        }

    }

    @CoreMethod(names = { "to_s", "inspect" }, optional = 1, lowerFixnum = 1)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString toS(int n, NotProvided base) {
            final Rope rope = new LazyIntRope(n);
            return makeStringNode.fromRope(rope, Encodings.US_ASCII);
        }

        @TruffleBoundary
        @Specialization
        protected RubyString toS(long n, NotProvided base) {
            if (CoreLibrary.fitsIntoInteger(n)) {
                return toS((int) n, base);
            }

            return makeStringNode.executeMake(Long.toString(n), Encodings.US_ASCII, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        protected RubyString toS(RubyBignum value, NotProvided base) {
            return makeStringNode.executeMake(
                    BigIntegerOps.toString(value.value),
                    Encodings.US_ASCII,
                    CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        protected RubyString toS(long n, int base) {
            if (base == 10) {
                return toS(n, NotProvided.INSTANCE);
            }

            if (base < 2 || base > 36) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(Long.toString(n, base), Encodings.US_ASCII, CodeRange.CR_7BIT);
        }

        @TruffleBoundary
        @Specialization
        protected RubyString toS(RubyBignum value, int base) {
            if (base < 2 || base > 36) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(
                    BigIntegerOps.toString(value.value, base),
                    Encodings.US_ASCII,
                    CodeRange.CR_7BIT);
        }

    }

    @Primitive(name = "integer_fits_into_int")
    public abstract static class FixnumFitsIntoIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoIntInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoIntLong(long a) {
            return CoreLibrary.fitsIntoInteger(a);
        }

        @Specialization
        protected boolean fitsIntoIntBignum(RubyBignum a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_uint")
    public abstract static class IntegerFitsIntoUIntNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoUIntInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoUIntLong(long a) {
            return CoreLibrary.fitsIntoUnsignedInteger(a);
        }

        @Specialization
        protected boolean fitsIntoUIntBignum(RubyBignum a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_long")
    public abstract static class IntegerFitsIntoLongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoLongInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoLongLong(long a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoLongBignum(RubyBignum a) {
            return false;
        }

    }

    @Primitive(name = "integer_fits_into_ulong")
    public abstract static class IntegerFitsIntoULongNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fitsIntoULongInt(int a) {
            return true;
        }

        @Specialization
        protected boolean fitsIntoULongLong(long a) {
            return true;
        }

        @TruffleBoundary
        @Specialization
        protected boolean fitsIntoULongBignum(RubyBignum a) {
            BigInteger bigInt = a.value;
            if (bigInt.signum() >= 0) {
                return bigInt.bitLength() <= 64;
            } else {
                return false;
            }
        }

    }

    @Primitive(name = "integer_lower")
    public abstract static class IntegerLowerNode extends PrimitiveArrayArgumentsNode {

        public static IntegerLowerNode create() {
            return IntegerNodesFactory.IntegerLowerNodeFactory.create(null);
        }

        public abstract Object executeLower(Object value);

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
    public abstract static class IntegerULongFromBigNumNode extends PrimitiveArrayArgumentsNode {

        private static final BigInteger TWO_POW_64 = BigInteger.valueOf(1).shiftLeft(64);
        private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

        @TruffleBoundary
        @Specialization
        protected long uLongFromBignum(RubyBignum b,
                @Cached ConditionProfile doesNotNeedsConversion) {
            final BigInteger value = b.value;
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

        @ExplodeLoop(kind = LoopExplosionKind.FULL_UNROLL)
        @Specialization(
                guards = {
                        "isImplicitLong(base)",
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

                    if (base instanceof RubyBignum) { // Bignum
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

        @Specialization(guards = { "isImplicitLong(base)", "exponent >= 0" })
        protected Object powLoop(Object base, long exponent,
                @Cached BranchProfile overflowProfile,
                @Cached MulNode mulNode) {
            Object result = 1;
            long exp = exponent;
            while (exp > 0) {
                if ((exp & 1) == 0) {
                    base = mulNode.executeMul(base, base);
                    exp >>= 1;

                    if (base instanceof RubyBignum) { // Bignum
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
                @Cached ConditionProfile complexProfile) {
            if (complexProfile.profile(base < 0)) {
                return FAILURE;
            } else {
                return Math.pow(base, exponent);
            }
        }

        @Specialization
        protected Object powBignum(long base, RubyBignum exponent,
                @Cached("new()") WarnNode warnNode) {
            if (base == 0) {
                return 0;
            }

            if (base == 1) {
                return 1;
            }

            if (base == -1) {
                if (BigIntegerOps.testBit(exponent.value, 0)) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (BigIntegerOps.compare(exponent.value, BigInteger.ZERO) < 0) {
                return FAILURE;
            }

            if (warnNode.shouldWarn()) {
                warnNode.warningMessage(
                        getContext().getCallStack().getTopMostUserSourceSection(),
                        "in a**b, b may be too big");
            }
            // b >= 2**63 && (a > 1 || a < -1) => larger than largest double
            // MRI behavior/bug: always positive Infinity even if a negative and b odd (likely due
            // to libc pow(a, +inf)).
            return Double.POSITIVE_INFINITY;
        }

        @Specialization
        protected Object pow(RubyBignum base, long exponent,
                @Cached ConditionProfile negativeProfile,
                @Cached ConditionProfile maybeTooBigProfile,
                @Cached("new()") WarnNode warnNode) {
            if (negativeProfile.profile(exponent < 0)) {
                return FAILURE;
            } else {
                final BigInteger bigIntegerBase = base.value;
                final int baseBitLength = BigIntegerOps.bitLength(bigIntegerBase);

                // Logic for promoting integer exponentiation into doubles taken from MRI.
                // We replicate the logic exactly so we match MRI's ranges.
                if (maybeTooBigProfile
                        .profile(baseBitLength > BIGLEN_LIMIT || (baseBitLength * exponent > BIGLEN_LIMIT))) {
                    if (warnNode.shouldWarn()) {
                        warnNode.warningMessage(
                                getContext().getCallStack().getTopMostUserSourceSection(),
                                "in a**b, b may be too big");
                    }
                    return BigIntegerOps.pow(bigIntegerBase, /* as double */ exponent);
                }

                // The cast is safe because of the check above.
                return createBignum(BigIntegerOps.pow(bigIntegerBase, (int) exponent));
            }
        }

        @Specialization
        protected Object pow(RubyBignum base, double exponent) {
            double doublePow = BigIntegerOps.pow(base.value, exponent);
            if (Double.isNaN(doublePow)) {
                // Instead of returning NaN, run the fallback code which can create a complex result
                return FAILURE;
            } else {
                return doublePow;
            }
        }

        @Specialization
        protected Object pow(RubyBignum base, RubyBignum exponent) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyNumber(exponent)")
        protected Object pow(Object base, Object exponent) {
            return FAILURE;
        }

        protected int getLimit() {
            return getLanguage().options.POW_CACHE;
        }

    }

    @Primitive(name = "mod_pow")
    @NodeChild(value = "base", type = RubyNode.class)
    @NodeChild(value = "exponent", type = RubyNode.class)
    @NodeChild(value = "modulo", type = RubyNode.class)
    public abstract static class ModPowNode extends PrimitiveNode {
        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @CreateCast("base")
        protected RubyNode baseToBigInteger(RubyNode base) {
            return BigIntegerCastNode.create(base);
        }

        @CreateCast("exponent")
        protected RubyNode exponentToBigInteger(RubyNode exponent) {
            return BigIntegerCastNode.create(exponent);
        }

        @CreateCast("modulo")
        protected RubyNode moduloToBigInteger(RubyNode modulo) {
            return BigIntegerCastNode.create(modulo);
        }

        @Specialization(guards = "modulo.signum() < 0")
        protected Object mod_pow_neg(BigInteger base, BigInteger exponent, BigInteger modulo) {
            BigInteger result = BigIntegerOps.modPow(base, exponent, BigIntegerOps.negate(modulo));
            return fixnumOrBignum.fixnumOrBignum(result.signum() == 1 ? BigIntegerOps.add(result, modulo) : result);
        }

        @Specialization(guards = "modulo.signum() > 0")
        protected Object mod_pow_pos(BigInteger base, BigInteger exponent, BigInteger modulo) {
            BigInteger result = BigIntegerOps.modPow(base, exponent, modulo);
            return fixnumOrBignum.fixnumOrBignum(result);
        }

        @Specialization(guards = "modulo.signum() == 0")
        protected Object mod_pow_zero(BigInteger base, BigInteger exponent, BigInteger modulo) {
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }
    }

    @CoreMethod(names = "downto", needsBlock = true, required = 1, returnsEnumeratorIfNoBlock = true)
    public abstract static class DownToNode extends YieldingCoreMethodNode {

        @Child private DispatchNode downtoInternalCall;
        private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

        @Specialization
        protected Object downto(int from, int to, RubyProc block) {
            int i = from;
            try {
                loopProfile.profileCounted(from - to + 1);
                for (; loopProfile.inject(i >= to); i--) {
                    callBlock(block, i);
                }
            } finally {
                LoopNode.reportLoopCount(this, from - i + 1);
            }

            return nil;
        }

        @Specialization
        protected Object downto(int from, double to, RubyProc block) {
            return downto(from, (int) Math.ceil(to), block);
        }

        @Specialization
        protected Object downto(long from, long to, RubyProc block) {
            long i = from;
            try {
                loopProfile.profileCounted(from - to + 1);
                for (; i >= to; i--) {
                    callBlock(block, i);
                }
            } finally {
                reportLongLoopCount(from - i + 1);
            }

            return nil;
        }

        @Specialization
        protected Object downto(long from, double to, RubyProc block) {
            return downto(from, (long) Math.ceil(to), block);
        }

        @Specialization(guards = "isRubyBignum(from) || !isImplicitLongOrDouble(to)")
        protected Object downto(Object from, Object to, RubyProc block) {
            if (downtoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                downtoInternalCall = insert(DispatchNode.create());
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

        @Specialization
        protected RubyBignum toI(RubyBignum n) {
            return n;
        }

    }

    @CoreMethod(names = "upto", needsBlock = true, required = 1, returnsEnumeratorIfNoBlock = true)
    public abstract static class UpToNode extends YieldingCoreMethodNode {

        @Child private DispatchNode uptoInternalCall;
        private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

        @Specialization
        protected Object upto(int from, int to, RubyProc block) {
            int i = from;
            try {
                loopProfile.profileCounted(to - from + 1);
                for (; loopProfile.inject(i <= to); i++) {
                    callBlock(block, i);
                }
            } finally {
                LoopNode.reportLoopCount(this, i - from + 1);
            }

            return nil;
        }

        @Specialization
        protected Object upto(int from, double to, RubyProc block) {
            return upto(from, (int) Math.floor(to), block);
        }

        @Specialization
        protected Object upto(long from, long to, RubyProc block) {
            long i = from;
            try {
                loopProfile.profileCounted(to - from + 1);
                for (; i <= to; i++) {
                    callBlock(block, i);
                }
            } finally {
                reportLongLoopCount(i - from + 1);
            }

            return nil;
        }

        @Specialization
        protected Object upto(long from, double to, RubyProc block) {
            return upto(from, (long) Math.floor(to), block);
        }

        @Specialization(guards = "isRubyBignum(from) || !isImplicitLongOrDouble(to)")
        protected Object upto(Object from, Object to, RubyProc block) {
            if (uptoInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                uptoInternalCall = insert(DispatchNode.create());
            }

            return uptoInternalCall.callWithBlock(from, "upto_internal", block, to);
        }

    }

}
