/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

public class BignumOperations {

    private static final BigInteger LONG_MIN_BIGINT = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX_BIGINT = BigInteger.valueOf(Long.MAX_VALUE);

    public static RubyBignum createBignum(BigInteger value) {
        assert value.compareTo(LONG_MIN_BIGINT) < 0 ||
                value.compareTo(LONG_MAX_BIGINT) > 0 : "Bignum in long range : " + value;
        final RubyBignum instance = new RubyBignum(value);
        // TODO BJF Jul-30-2020 Add allocation tracing
        return instance;
    }

}
