/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.HashNodes.HashLookupOrExecuteDefaultNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadKeywordArgumentNode extends RubyContextSourceNode implements PEBiFunction {

    private final RubySymbol name;
    private final ConditionProfile defaultProfile = ConditionProfile.create();

    @Child private RubyNode defaultValue;
    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private HashLookupOrExecuteDefaultNode hashLookupNode;

    public ReadKeywordArgumentNode(int minimum, RubySymbol name, RubyNode defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyHash hash = readUserKeywordsHashNode.execute(frame);

        if (defaultProfile.profile(hash == null)) {
            return defaultValue.execute(frame);
        }

        return lookupKeywordInHash(frame, hash);
    }

    private Object lookupKeywordInHash(VirtualFrame frame, RubyHash hash) {
        if (hashLookupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashLookupNode = insert(HashLookupOrExecuteDefaultNode.create());
        }

        return hashLookupNode.executeGet(frame, hash, name, this);
    }

    @Override
    public Object accept(VirtualFrame frame, Object hash, Object key) {
        return defaultValue.execute(frame);
    }

}
