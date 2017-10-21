/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "arguments", type = RubyNode.class)
public abstract class ForeignToRubyArgumentsNode extends RubyNode {

    public static ForeignToRubyArgumentsNode create() {
        return ForeignToRubyArgumentsNodeGen.create(null);
    }

    public abstract Object[] executeConvert(VirtualFrame frame, Object[] args);

    @Specialization(
            guards = "args.length == cachedArgsLength",
            limit = "getLimit()")
    @ExplodeLoop
    public Object[] convertCached(VirtualFrame frame, Object[] args,
                                  @Cached("args.length") int cachedArgsLength,
                                  @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
        final Object[] convertedArgs = new Object[cachedArgsLength];

        for (int n = 0; n < cachedArgsLength; n++) {
            convertedArgs[n] = foreignToRubyNode.executeConvert(frame, args[n]);
        }

        return convertedArgs;
    }

    @Specialization(replaces = "convertCached")
    public Object[] convertUncached(VirtualFrame frame, Object[] args,
                                    @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
        final Object[] convertedArgs = new Object[args.length];

        for (int n = 0; n < args.length; n++) {
            convertedArgs[n] = foreignToRubyNode.executeConvert(frame, args[n]);
        }

        return convertedArgs;
    }

    protected int getLimit() {
        return getContext().getOptions().INTEROP_CONVERT_CACHE;
    }

}
