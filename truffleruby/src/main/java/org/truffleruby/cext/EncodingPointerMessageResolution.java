/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(
        receiverType = EncodingPointerAdapter.class,
        language = RubyLanguage.class
)
public class EncodingPointerMessageResolution {

    @CanResolve
    public abstract static class EncodingPointerCheckNode extends Node {

        protected static boolean test(TruffleObject receiver) {
            return receiver instanceof EncodingPointerAdapter;
        }

    }

    @Resolve(message = "READ")
    public static abstract class EncodingPointerReadNode extends Node {

        protected Object access(EncodingPointerAdapter adapter, long index) {
            assert index == 0L;
            return Layouts.ENCODING.getName(adapter.getEncoding());
        }

    }

}
