/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.Split;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.SourceSection;

public abstract class RubyCheckArityRootNode extends RubyRootNode {

    public final Arity arityForCheck;

    public RubyCheckArityRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FrameDescriptor frameDescriptor,
            SharedMethodInfo sharedMethodInfo,
            RubyNode body,
            Split split,
            ReturnID returnID,
            Arity arityForCheck) {
        super(language, sourceSection, frameDescriptor, sharedMethodInfo, body, split, returnID);

        this.arityForCheck = arityForCheck;
    }

}
