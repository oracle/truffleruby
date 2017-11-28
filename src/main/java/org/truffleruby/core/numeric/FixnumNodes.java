/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.algorithms.SipHash;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.numeric.FixnumNodesFactory.DivNodeFactory;
import org.truffleruby.core.numeric.FixnumNodesFactory.MulNodeFactory;
import org.truffleruby.core.numeric.IntegerNodes.IntegerMulNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.LazyIntRope;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.SnippetNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import java.math.BigInteger;
import java.nio.ByteBuffer;

@CoreClass("Fixnum")
public abstract class FixnumNodes {

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends CoreMethodArrayArgumentsNode {

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
            if (fixnumOrBignumNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fixnumOrBignumNode = insert(new FixnumOrBignumNode());
            }

            return fixnumOrBignumNode.fixnumOrBignum(BigInteger.valueOf(value).negate());
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddNode extends BignumNodes.BignumCoreMethodNode {

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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object addCoerced(int a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object addCoerced(long a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().PLUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubNode extends BignumNodes.BignumCoreMethodNode {

        public abstract Object executeSub(Object a, Object b);

        @Specialization(rewriteOn = ArithmeticException.class)
        public int sub(int a, int b) {
            return Math.subtractExact(a, b);
        }

        @Specialization
        public long subWithOverflow(int a, int b) {
            return (long) a - (long) b;
        }

        @Specialization(guards = "!isRubyBignum(b)")
        public Object subCoerced(int a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object subCoerced(long a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MINUS.getSymbol(), b);
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MulNode extends BignumNodes.BignumCoreMethodNode {

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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object mulCoerced(int a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object mulCoerced(long a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().MULTIPLY.getSymbol(), b);
        }
    }

    @CoreMethod(names = "/", required = 1)
    public abstract static class DivNode extends CoreMethodArrayArgumentsNode {

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
                throw new RaiseException(coreExceptions().zeroDivisionError(this));
            } else {
                return divInt(a, b);
            }
        }

        @Specialization(guards = "!isRubyBignum(b)")
        public Object divCoerced(int a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
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
                throw new RaiseException(coreExceptions().zeroDivisionError(this));
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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object divCoerced(long a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().DIVIDE.getSymbol(), b);
        }

        protected static boolean isLongMinValue(long a) {
            return a == Long.MIN_VALUE;
        }

    }

    // Defined in Java as we need to statically call #/
    @CoreMethod(names = "div", required = 1)
    public abstract static class IDivNode extends BignumNodes.BignumCoreMethodNode {

        @Child private DivNode divNode = DivNodeFactory.create(null);
        @Child private FloatNodes.FloorNode floorNode = FloatNodesFactory.FloorNodeFactory.create(null);

        @Specialization
        public Object idiv(Object a, Object b,
                @Cached("createBinaryProfile()") ConditionProfile zeroProfile) {
            Object quotient = divNode.executeDiv(a, b);
            if (quotient instanceof Double) {
                if (zeroProfile.profile((double) b == 0.0)) {
                    throw new RaiseException(coreExceptions().zeroDivisionError(this));
                }
                return floorNode.executeFloor((double) quotient);
            } else {
                return quotient;
            }
        }

    }

    @CoreMethod(names = "%", required = 1)
    public abstract static class ModNode extends BignumNodes.BignumCoreMethodNode {

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

    }

    @CoreMethod(names = "divmod", required = 1)
    public abstract static class DivModNode extends CoreMethodArrayArgumentsNode {

        @Child private GeneralDivModNode divModNode = new GeneralDivModNode();

        @Specialization
        public DynamicObject divMod(long a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public DynamicObject divMod(long a, DynamicObject b) {
            return divModNode.execute(a, Layouts.BIGNUM.getValue(b));
        }

        @Specialization(guards = "!isRubyBignum(b)")
        public DynamicObject divModOther(VirtualFrame frame, long a, DynamicObject b,
                                         @Cached("new()") SnippetNode snippetNode) {
            return (DynamicObject) snippetNode.execute(frame, "raise ZeroDivisionError if b === 0; [(a / b).floor, a-b*(a/b).floor]", "a", a, "b", b);
        }

        @Specialization
        public DynamicObject divMod(long a, double b) {
            return divModNode.execute(a, b);
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

        @Specialization(guards = {
                "!isRubyBignum(b)",
                "!isInteger(b)",
                "!isLong(b)",
                "!isDouble(b)" })
        public Object lessCoerced(long a, Object b,
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

        @Specialization(guards = {
                "!isRubyBignum(b)",
                "!isInteger(b)",
                "!isLong(b)",
                "!isDouble(b)" })
        public Object lessEqualCoerced(long a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().LESS_OR_EQUAL.getSymbol(), b);
        }
    }

    @CoreMethod(names = { "==", "===" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode reverseCallNode = CallDispatchHeadNode.create();

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

        @Specialization(guards = {
                "!isByte(b)",
                "!isShort(b)",
                "!isInteger(b)",
                "!isLong(b)",
                "!isRubyBignum(b)" })
        public Object equal(VirtualFrame frame, Object a, Object b) {
            return reverseCallNode.call(frame, b, "==", a);
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

        @Specialization(guards = {
                "!isInteger(b)",
                "!isLong(b)",
                "!isDouble(b)",
                "!isRubyBignum(b)" })
        public Object compare(
                VirtualFrame frame,
                Object a,
                Object b,
                @Cached("new()") SnippetNode snippetNode) {
            return snippetNode.execute(frame, "begin; b, a = math_coerce(other, :compare_error); a <=> b; rescue ArgumentError; nil; end", "other", b);
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

        @Specialization(guards = {
                "!isRubyBignum(b)",
                "!isInteger(b)",
                "!isLong(b)",
                "!isDouble(b)" })
        public Object greaterEqualCoerced(long a, Object b,
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

        @Specialization(guards = {
                "!isRubyBignum(b)",
                "!isInteger(b)",
                "!isLong(b)",
                "!isDouble(b)" })
        public Object greaterCoerced(long a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCompare) {
            return redoCompare.call(null, a, "redo_compare", coreStrings().GREATER_THAN.getSymbol(), b);
        }
    }

    @CoreMethod(names = "~")
    public abstract static class ComplementNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int complement(int n) {
            return ~n;
        }

        @Specialization
        public long complement(long n) {
            return ~n;
        }

    }

    @CoreMethod(names = "&", required = 1)
    public abstract static class BitAndNode extends BignumNodes.BignumCoreMethodNode {

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

        @Specialization(guards = { "!isInteger(b)", "!isLong(b)", "!isRubyBignum(b)" })
        public Object bitAndCoerce(int a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_bit_coerced", coreStrings().AMPERSAND.getSymbol(), b);
        }

        @Specialization(guards = { "!isInteger(b)", "!isLong(b)", "!isRubyBignum(b)" })
        public Object bitAndCoerce(long a, Object b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_bit_coerced", coreStrings().AMPERSAND.getSymbol(), b);
        }


    }

    @CoreMethod(names = "|", required = 1)
    public abstract static class BitOrNode extends BignumNodes.BignumCoreMethodNode {

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
    }

    @CoreMethod(names = "^", required = 1)
    public abstract static class BitXOrNode extends BignumNodes.BignumCoreMethodNode {

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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object bitXOr(Object a, DynamicObject b,
                @Cached("createOnSelf()") CallDispatchHeadNode redoCoerced) {
            return redoCoerced.call(null, a, "redo_coerced", coreStrings().CIRCUMFLEX.getSymbol(), b);
        }

    }

    @CoreMethod(names = "<<", required = 1, lowerFixnum = 1)
    public abstract static class LeftShiftNode extends BignumNodes.BignumCoreMethodNode {

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
                rightShiftNode = insert(FixnumNodesFactory.RightShiftNodeFactory.create(null));
            }
            return rightShiftNode.executeRightShift(a, -b);
        }

        @Specialization(guards = { "!isInteger(b)", "!isLong(b)" })
        public Object leftShiftFallback(Object a, Object b) {
            if (fallbackCallNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fallbackCallNode = insert(CallDispatchHeadNode.createOnSelf());
            }
            return fallbackCallNode.call(null, a, "left_shift_fallback", b);
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
    public abstract static class RightShiftNode extends CoreMethodArrayArgumentsNode {

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
                leftShiftNode = insert(FixnumNodesFactory.LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, -b);
        }

        @Specialization(guards = "b >= 0")
        public int rightShift(long a, long b) { // b is not in int range due to lowerFixnumParameters
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
                leftShiftNode = insert(FixnumNodesFactory.LeftShiftNodeFactory.create(null));
            }
            return leftShiftNode.executeLeftShift(a, Layouts.BIGNUM.getValue(b).negate());
        }

        @Specialization(guards = { "!isInteger(b)", "!isLong(b)" })
        public Object rightShiftFallback(Object a, Object b) {
            if (fallbackCallNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fallbackCallNode = insert(CallDispatchHeadNode.createOnSelf());
            }
            return fallbackCallNode.call(null, a, "right_shift_fallback", b);
        }

        protected static boolean isPositive(DynamicObject b) {
            return Layouts.BIGNUM.getValue(b).signum() >= 0;
        }

    }

    @CoreMethod(names = { "abs", "magnitude" })
    public abstract static class AbsNode extends CoreMethodArrayArgumentsNode {

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

    }

    @CoreMethod(names = "inspect")
    public abstract static class InspectNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject inspect(int n) {
            return makeStringNode.fromRope(new LazyIntRope(n));
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject inspect(long n) {
            if (CoreLibrary.fitsIntoInteger(n)) {
                return inspect((int) n);
            }

            return makeStringNode.executeMake(Long.toString(n), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @CoreMethod(names = "size", needsSelf = false)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int size() {
            return Long.SIZE / Byte.SIZE;
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

    }

    @CoreMethod(names = "to_s", optional = 1, lowerFixnum = 1)
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
        public DynamicObject toS(long n, int base) {
            if (base == 10) {
                return toS(n, NotProvided.INSTANCE);
            }

            if (base < 2 || base > 36) {
                throw new RaiseException(coreExceptions().argumentErrorInvalidRadix(base, this));
            }

            return makeStringNode.executeMake(Long.toString(n, base), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @CoreMethod(names = "zero?")
    public abstract static class ZeroNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean zero(int n) {
            return n == 0;
        }

        @Specialization
        public boolean zero(long n) {
            return n == 0;
        }

    }


    @Primitive(name = "fixnum_coerce", lowerFixnum = 1)
    public static abstract class FixnumCoercePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject coerce(int a, int b) {
            return Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), new int[]{b, a}, 2);
        }

        @Specialization
        public DynamicObject coerce(long a, int b) {
            return Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), new long[]{b, a}, 2);
        }

        @Specialization(guards = "!isInteger(b)")
        public Object coerce(int a, Object b) {
            return FAILURE;
        }

    }


    @Primitive(name = "fixnum_memhash")
    public static abstract class FixnumMemhashPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public long memhashLongLong(long a, long b) {
            final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
            buffer.putLong(a);
            buffer.putLong(b);
            return SipHash.hash24(Hashing.SEED_K0, Hashing.SEED_K1, buffer.array());
        }



    }

    @Primitive(name = "fixnum_fits_into_int")
    public static abstract class FixnumFitsIntoIntPrimitiveNode extends PrimitiveArrayArgumentsNode {

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

    @Primitive(name = "fixnum_fits_into_uint")
    public static abstract class FixnumFitsIntoUIntPrimitiveNode extends PrimitiveArrayArgumentsNode {

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

    @Primitive(name = "fixnum_fits_into_long")
    public static abstract class FixnumFitsIntoLongPrimitiveNode extends PrimitiveArrayArgumentsNode {

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

    @Primitive(name = "fixnum_fits_into_ulong")
    public static abstract class FixnumFitsIntoULongPrimitiveNode extends PrimitiveArrayArgumentsNode {

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

    @Primitive(name = "fixnum_ulong_from_bignum")
    public static abstract class FixnumULongFromBigNumPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyBignum(b)")
        public long uLongFromBignum(DynamicObject b) {
            return Layouts.BIGNUM.getValue(b).longValue();
        }

    }

    @Primitive(name = "fixnum_pow", lowerFixnum = { 0, 1 })
    public abstract static class PowNode extends PrimitiveArrayArgumentsNode {

        public abstract Object executePow(Object a, Object b);

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
                @Cached("create()") IntegerMulNode mulNode) {
            Object result = 1;
            int exp = cachedExponent;
            while (exp > 0) {
                if ((exp & 1) == 0) {
                    base = mulNode.executeMul(base, base);
                    exp >>= 1;
                } else {
                    result = mulNode.executeMul(base, result);
                    exp--;
                }
            }
            return result;
        }

        @Specialization(guards = { "isIntOrLong(base)", "exponent >= 0" })
        public Object powLoop(Object base, long exponent,
                @Cached("create()") IntegerMulNode mulNode) {
            Object result = 1;
            long exp = exponent;
            while (exp > 0) {
                if ((exp & 1) == 0) {
                    base = mulNode.executeMul(base, base);
                    exp >>= 1;
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

            warnNode.warn("warn('in a**b, b may be too big')");
            // b >= 2**63 && (a > 1 || a < -1) => larger than largest double
            // MRI behavior/bug: always positive Infinity even if a negative and b odd (likely due to libc pow(a, +inf)).
            return Double.POSITIVE_INFINITY;
        }

        @Specialization(guards = "!isRubyBignum(b)")
        public Object pow(long a, DynamicObject b) {
            return FAILURE;
        }

        protected static boolean isIntOrLong(Object value) {
            return value instanceof Integer || value instanceof Long;
        }

        @TruffleBoundary
        public BigInteger bigPow(long a, int b) {
            return BigInteger.valueOf(a).pow(b);
        }

        @TruffleBoundary
        private boolean testBit(BigInteger bigInteger, int n) {
            return bigInteger.testBit(n);
        }

        @TruffleBoundary
        private int compareTo(BigInteger a, BigInteger b) {
            return a.compareTo(b);
        }

        protected int getLimit() {
            return getContext().getOptions().POW_CACHE;
        }

    }


}
