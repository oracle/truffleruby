/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.StringGuards;
import org.truffleruby.language.RubyBaseNode;

public abstract class TStringNodes {

    public abstract static class SingleByteOptimizableNode extends RubyBaseNode {
        public static SingleByteOptimizableNode create() {
            return TStringNodesFactory.SingleByteOptimizableNodeGen.create();
        }

        public abstract boolean execute(AbstractTruffleString string, RubyEncoding encoding);

        @Specialization
        protected boolean isSingleByteOptimizable(AbstractTruffleString string, RubyEncoding encoding,
                @Cached ConditionProfile asciiOnlyProfile,
                @Cached TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            if (asciiOnlyProfile.profile(StringGuards.is7Bit(string, encoding, getByteCodeRangeNode))) {
                return true;
            } else {
                return encoding.jcoding.isSingleByte();
            }
        }
    }

}
