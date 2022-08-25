/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

/** For def m(**nil) */
public class CheckNoKeywordArgumentsNode extends RubyContextSourceNode {

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    private final BranchProfile errorProfile = BranchProfile.create();

    public CheckNoKeywordArgumentsNode() {
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame);
        if (keywordArguments != null) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError("no keywords accepted", this));
        }

        return nil;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new CheckNoKeywordArgumentsNode();
        copy.copyFlags(this);
        return copy;
    }

}
