/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import com.oracle.truffle.api.memory.ByteArraySupport;
import org.truffleruby.core.numeric.FixnumLowerNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

import java.nio.ByteOrder;

public abstract class GetRandomIntNode extends RubyBaseNode {

    public static GetRandomIntNode create() {
        return GetRandomIntNodeGen.create();
    }

    public abstract int execute(Object randomizer);

    private static final ByteArraySupport byteArraySupport = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
            ? ByteArraySupport.bigEndian()
            : ByteArraySupport.littleEndian();

    @Specialization
    protected int genRandInt(RubyPRNGRandomizer randomizer) {
        return randomizer.genrandInt32();
    }

    @Specialization
    protected int genRandInt(RubySecureRandomizer randomizer) {
        final byte[] bytes = getContext().getRandomSeedBytes(4);
        return byteArraySupport.getInt(bytes, 0);
    }

    @Specialization
    protected int genRandFallback(RubyCustomRandomizer randomizer,
            @Cached DispatchNode randomIntNode,
            @Cached FixnumLowerNode fixnumLowerNode) {
        return (int) fixnumLowerNode.executeLower(
                randomIntNode.call(
                        getContext().getCoreLibrary().truffleRandomOperationsModule,
                        "obj_random_int",
                        randomizer));
    }

}
