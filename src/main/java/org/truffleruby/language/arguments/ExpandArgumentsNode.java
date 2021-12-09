/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;

import java.util.Arrays;

@NodeChild("args")
@NodeChild("descriptor")
public abstract class ExpandArgumentsNode extends RubyContextSourceNode {

    public static ExpandArgumentsNode create() {
        return ExpandArgumentsNodeGen.create(null, null);
    }

    public abstract Object[] execute(Object[] args, KeywordDescriptor descriptor);

    @Specialization
    protected Object[] expand(Object[] args, EmptyKeywordDescriptor descriptor) {
        return args;
    }

    @Specialization
    protected Object[] expand(Object[] args, NonEmptyKeywordDescriptor descriptor,
            @Cached ExpandKeywordArgumentsNode expandKeywordArgumentsNode) {
        final Object[] expanded = Arrays.copyOf(args, args.length - descriptor.getLength());
        expanded[descriptor.getHashIndex()] = expandKeywordArgumentsNode.execute(args, descriptor, 0);
        return expanded;
    }

}
