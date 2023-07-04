/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.FloatToIntegerNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class GeneralDivModNode extends RubyBaseNode {

    public abstract RubyArray execute(Object a, Object b);

    @Specialization
    protected RubyArray doLongs(long a, long b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Exclusive InlinedBranchProfile bMinusOneProfile,
            @Cached @Exclusive InlinedBranchProfile useFixnumPairProfile,
            @Cached @Exclusive InlinedBranchProfile useObjectPairProfile,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumQuotient) {
        return divMod(a, b, bZeroProfile, bMinusOneProfile, useFixnumPairProfile, useObjectPairProfile,
                fixnumOrBignumQuotient);
    }

    @Specialization
    protected RubyArray doLongAndBigInt(long a, BigInteger b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile bigIntegerFixnumProfile,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumQuotient,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumRemainder) {
        return divMod(BigIntegerOps.valueOf(a), b, bZeroProfile, bigIntegerFixnumProfile, fixnumOrBignumQuotient,
                fixnumOrBignumRemainder);
    }

    @Specialization
    protected RubyArray doLongAndDouble(long a, double b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile nanProfile,
            @Cached @Shared FloatToIntegerNode floatToIntegerNode) {
        return divMod(a, b, bZeroProfile, nanProfile, floatToIntegerNode);
    }

    @Specialization
    protected RubyArray doBigIntAndLong(BigInteger a, long b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile bigIntegerFixnumProfile,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumQuotient,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumRemainder) {
        return divMod(a, BigIntegerOps.valueOf(b), bZeroProfile, bigIntegerFixnumProfile, fixnumOrBignumQuotient,
                fixnumOrBignumRemainder);
    }

    @Specialization
    protected RubyArray doBigInts(BigInteger a, BigInteger b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile bigIntegerFixnumProfile,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumQuotient,
            @Cached @Shared FixnumOrBignumNode fixnumOrBignumRemainder) {
        return divMod(a, b, bZeroProfile, bigIntegerFixnumProfile, fixnumOrBignumQuotient, fixnumOrBignumRemainder);
    }

    @Specialization
    protected RubyArray doBigIntAndDouble(BigInteger a, double b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile nanProfile,
            @Cached @Shared FloatToIntegerNode floatToIntegerNode) {
        return divMod(BigIntegerOps.doubleValue(a), b, bZeroProfile, nanProfile, floatToIntegerNode);
    }

    @Specialization
    protected RubyArray doDoubleAndLong(double a, long b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile nanProfile,
            @Cached @Shared FloatToIntegerNode floatToIntegerNode) {
        return divMod(a, b, bZeroProfile, nanProfile, floatToIntegerNode);
    }

    @Specialization
    protected RubyArray doDoubleAndBigInt(double a, BigInteger b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile nanProfile,
            @Cached @Shared FloatToIntegerNode floatToIntegerNode) {
        return divMod(a, BigIntegerOps.doubleValue(b), bZeroProfile, nanProfile, floatToIntegerNode);
    }

    @Specialization
    protected RubyArray doDoubles(double a, double b,
            @Cached @Shared InlinedBranchProfile bZeroProfile,
            @Cached @Shared InlinedBranchProfile nanProfile,
            @Cached @Shared FloatToIntegerNode floatToIntegerNode) {
        return divMod(a, b, bZeroProfile, nanProfile, floatToIntegerNode);
    }

    /* div-mod algorithms copied from org.jruby.RubyFixnum, org.jruby.RubyBignum and org.jrubyRubyFloat. See license and
     * contributors there. */

    private RubyArray divMod(long a, long b, InlinedBranchProfile bZeroProfile,
            InlinedBranchProfile bMinusOneProfile, InlinedBranchProfile useFixnumPairProfile,
            InlinedBranchProfile useObjectPairProfile, FixnumOrBignumNode fixnumOrBignumQuotient) {
        if (b == 0) {
            bZeroProfile.enter(this);
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        long mod;
        Object integerDiv;

        if (b == -1) {
            bMinusOneProfile.enter(this);

            if (a == Long.MIN_VALUE) {
                integerDiv = BigIntegerOps.negate(a);
            } else {
                integerDiv = -a;
            }
            mod = 0;
        } else {
            long div = a / b;
            mod = a - b * div;
            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                div -= 1;
                mod += b;
            }
            integerDiv = div;
        }

        if (integerDiv instanceof Long && CoreLibrary.fitsIntoInteger((long) integerDiv) &&
                CoreLibrary.fitsIntoInteger(mod)) {
            useFixnumPairProfile.enter(this);
            return createArray(new int[]{ (int) (long) integerDiv, (int) mod });
        } else if (integerDiv instanceof Long) {
            useObjectPairProfile.enter(this);
            return createArray(new long[]{ (long) integerDiv, mod });
        } else {
            useObjectPairProfile.enter(this);
            return createArray(new Object[]{
                    fixnumOrBignumQuotient.fixnumOrBignum((BigInteger) integerDiv),
                    mod
            });
        }
    }

    @TruffleBoundary
    private RubyArray divMod(double a, double b, InlinedBranchProfile bZeroProfile, InlinedBranchProfile nanProfile,
            FloatToIntegerNode floatToIntegerNode) {
        if (b == 0) {
            bZeroProfile.enter(this);
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        double mod = Math.IEEEremainder(a, b);

        if (Double.isNaN(mod)) {
            nanProfile.enter(this);
            throw new RaiseException(getContext(), coreExceptions().floatDomainError("NaN", this));
        }

        final double div = Math.floor(a / b);

        if (b * mod < 0) {
            mod += b;
        }

        return createArray(new Object[]{ floatToIntegerNode.fixnumOrBignum(div), mod });
    }

    @TruffleBoundary
    private RubyArray divMod(BigInteger a, BigInteger b, InlinedBranchProfile bZeroProfile,
            InlinedBranchProfile bigIntegerFixnumProfile, FixnumOrBignumNode fixnumOrBignumQuotient,
            FixnumOrBignumNode fixnumOrBignumRemainder) {
        if (b.signum() == 0) {
            bZeroProfile.enter(this);
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        final BigInteger[] bigIntegerResults = a.divideAndRemainder(b);

        if ((a.signum() * b.signum()) == -1 && bigIntegerResults[1].signum() != 0) {
            bigIntegerFixnumProfile.enter(this);
            bigIntegerResults[0] = bigIntegerResults[0].subtract(BigInteger.ONE);
            bigIntegerResults[1] = b.add(bigIntegerResults[1]);
        }

        return createArray(new Object[]{
                fixnumOrBignumQuotient.fixnumOrBignum(bigIntegerResults[0]),
                fixnumOrBignumRemainder.fixnumOrBignum(bigIntegerResults[1]) });
    }

}
