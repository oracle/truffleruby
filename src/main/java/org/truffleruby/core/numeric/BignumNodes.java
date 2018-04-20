/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.WarnNode;

import java.math.BigInteger;

@CoreClass("Bignum")
public abstract class BignumNodes {

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

    @Primitive(name = "bignum_compare")
    public abstract static class BignumCompareNode extends PrimitiveArrayArgumentsNode {

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

        @Specialization(guards = "!isRubyBignum(b)")
        public Object compareFallback(DynamicObject a, DynamicObject b) {
            return FAILURE;
        }

    }

    @Primitive(name = "bignum_pow")
    public static abstract class BignumPowPrimitiveNode extends PrimitiveArrayArgumentsNode {

        // Value taken from MRI for determining when to promote integer exponentiation into doubles.
        private static final int BIGLEN_LIMIT = 32 * 1024 * 1024;

        public static BignumPowPrimitiveNode create() {
            return BignumNodesFactory.BignumPowPrimitiveNodeFactory.create(null);
        }

        public abstract Object executePow(Object a, Object b);

        @Specialization
        public Object pow(DynamicObject a, int b) {
            return executePow(a, (long) b);
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
                    warnNode.warn("warn('in a**b, b may be too big')");
                    return executePow(a, (double) b);
                }

                // TODO CS 15-Feb-15 what about this cast?
                return createBignum(pow(base, (int) b));
            }
        }

        @TruffleBoundary
        @Specialization
        public double pow(DynamicObject a, double b) {
            return Math.pow(Layouts.BIGNUM.getValue(a).doubleValue(), b);
        }

        @Specialization(guards = "isRubyBignum(b)")
        public Void pow(DynamicObject a, DynamicObject b) {
            throw new UnsupportedOperationException();
        }

        @TruffleBoundary
        private static BigInteger pow(BigInteger bigInteger, int exponent) {
            return bigInteger.pow(exponent);
        }

    }

}
