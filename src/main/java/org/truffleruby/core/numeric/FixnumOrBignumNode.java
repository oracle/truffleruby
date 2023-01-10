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

import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class FixnumOrBignumNode extends RubyBaseNode {

    private static final BigInteger LONG_MIN_BIGINT = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX_BIGINT = BigInteger.valueOf(Long.MAX_VALUE);

    public static FixnumOrBignumNode create() {
        return new FixnumOrBignumNode();
    }

    public FixnumOrBignumNode() {
    }

    private final ConditionProfile lowerProfile = ConditionProfile.create();
    private final ConditionProfile intProfile = ConditionProfile.create();

    public Object fixnumOrBignum(BigInteger value) {
        if (lowerProfile.profile(fitsIntoLong(value))) {
            final long longValue = BigIntegerOps.longValue(value);

            if (intProfile.profile(CoreLibrary.fitsIntoInteger(longValue))) {
                return (int) longValue;
            } else {
                return longValue;
            }
        } else {
            return createBignum(value);
        }
    }

    @TruffleBoundary
    private static boolean fitsIntoLong(BigInteger value) {
        return value.compareTo(LONG_MIN_BIGINT) >= 0 && value.compareTo(LONG_MAX_BIGINT) <= 0;
    }

}
