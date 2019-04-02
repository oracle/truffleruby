/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.RubyNode;

import java.math.BigDecimal;

public class BigDecimalCoerceNode extends BigDecimalCoreMethodNode {

    @Child private RubyNode child;
    @Child private BigDecimalCastNode bigDecimalCastNode = BigDecimalCastNodeGen.create();
    private final ConditionProfile castProfile = ConditionProfile.createBinaryProfile();

    public BigDecimalCoerceNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object value = child.execute(frame);
        final Object castedValue = bigDecimalCastNode.execute(value, getRoundMode());
        if (castProfile.profile(castedValue instanceof BigDecimal)) {
            return createBigDecimal(castedValue);
        } else {
            return value;
        }
    }

}
