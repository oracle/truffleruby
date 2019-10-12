/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.literal.IntegerFixnumLiteralNode;

public abstract class PrimitiveArrayNodeFactory {

    /**
     * Create a node to read from an array with a constant denormalized index.
     */
    public static RubyNode read(RubyNode array, int index) {
        final RubyNode literalIndex = new IntegerFixnumLiteralNode(index);

        if (index >= 0) {
            return ArrayReadNormalizedNodeGen.create(array, literalIndex);
        } else {
            return ArrayReadDenormalizedNodeGen.create(array, literalIndex);
        }
    }

}
