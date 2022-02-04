/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@GenerateUncached
public abstract class RubyToForeignArgumentsNode extends RubyBaseNode {

    public static RubyToForeignArgumentsNode create() {
        return RubyToForeignArgumentsNodeGen.create();
    }

    public abstract Object[] executeConvert(Object[] args);

    @ExplodeLoop
    @Specialization(
            guards = { "args.length == cachedArgsLength", "cachedArgsLength <= MAX_EXPLODE_SIZE" },
            limit = "getLimit()")
    protected Object[] convertCached(Object[] args,
            @Cached("args.length") int cachedArgsLength,
            @Cached RubyToForeignNode rubyToForeignNode) {
        final Object[] convertedArgs = new Object[cachedArgsLength];

        for (int n = 0; n < cachedArgsLength; n++) {
            convertedArgs[n] = rubyToForeignNode.executeConvert(args[n]);
        }

        return convertedArgs;
    }

    @Specialization(replaces = "convertCached")
    protected Object[] convertUncached(Object[] args,
            @Cached RubyToForeignNode rubyToForeignNode) {
        final Object[] convertedArgs = new Object[args.length];

        for (int n = 0; n < args.length; n++) {
            convertedArgs[n] = rubyToForeignNode.executeConvert(args[n]);
        }

        return convertedArgs;
    }

    protected int getLimit() {
        return getLanguage().options.INTEROP_CONVERT_CACHE;
    }

}
