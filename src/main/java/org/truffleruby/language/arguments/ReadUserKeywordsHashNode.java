/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public final class ReadUserKeywordsHashNode extends RubyBaseNode {

    private final ConditionProfile keywordArgumentsProfile = ConditionProfile.create();
    private final ConditionProfile emptyKeywordArgumentsProfile = ConditionProfile.create();

    public ReadUserKeywordsHashNode() {
    }

    public RubyHash execute(VirtualFrame frame) {
        final ArgumentsDescriptor descriptor = RubyArguments.getDescriptor(frame);
        if (keywordArgumentsProfile.profile(descriptor instanceof KeywordArgumentsDescriptor)) {
            final RubyHash keywordArguments = (RubyHash) RubyArguments.getLastArgument(frame);
            if (emptyKeywordArgumentsProfile.profile(keywordArguments.size == 0)) {
                return null; // empty kwargs -> treated the same as if it was not passed by the caller
            } else {
                return keywordArguments;
            }
        } else {
            return null;
        }
    }
}
