/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.control.RaiseException;

public class FloatToIntegerNode extends RubyBaseNode {

    public static FloatToIntegerNode create() {
        return new FloatToIntegerNode();
    }

    public FloatToIntegerNode() {
    }

    @Child FixnumOrBignumNode fixnumOrBignumNode = FixnumOrBignumNode.create();

    private final ConditionProfile integerFromDoubleProfile = ConditionProfile.create();
    private final ConditionProfile longFromDoubleProfile = ConditionProfile.create();
    private final BranchProfile errorProfile = BranchProfile.create();

    public Object fixnumOrBignum(double value) {
        if (integerFromDoubleProfile.profile(value > Integer.MIN_VALUE && value < Integer.MAX_VALUE)) {
            return (int) value;
        } else if (longFromDoubleProfile.profile(value > Long.MIN_VALUE && value < Long.MAX_VALUE)) {
            return (long) value;
        } else {
            if (Double.isInfinite(value)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("Infinity", this));
            }

            if (Double.isNaN(value)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("NaN", this));
            }

            return fixnumOrBignumNode.fixnumOrBignum(BigIntegerOps.fromDouble(value));
        }
    }
}
