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
import org.truffleruby.language.control.NextException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.RedoException;
import org.truffleruby.language.control.RetryException;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.methods.TranslateExceptionNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

public class RubyProcRootNode extends RubyRootNode {

    public static RubyProcRootNode of(RootCallTarget callTarget) {
        return (RubyProcRootNode) callTarget.getRootNode();
    }

    @Child private TranslateExceptionNode translateExceptionNode;

    private final BranchProfile redoProfile = BranchProfile.create();
    private final BranchProfile nextProfile = BranchProfile.create();
    private final BranchProfile retryProfile = BranchProfile.create();

    public RubyProcRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FrameDescriptor frameDescriptor,
            SharedMethodInfo sharedMethodInfo,
            RubyNode body,
            Split split,
            ReturnID returnID) {
        super(language, sourceSection, frameDescriptor, sharedMethodInfo, body, split, returnID);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        SafepointManager.poll(language, this);

        try {
            while (true) {
                try {
                    return body.execute(frame);
                } catch (RedoException e) {
                    redoProfile.enter();
                    SafepointManager.poll(language, this);
                    continue;
                }
            }
        } catch (NextException e) {
            nextProfile.enter();
            return e.getResult();
        } catch (RetryException e) {
            retryProfile.enter();
            throw new RaiseException(getContext(), getContext().getCoreExceptions().syntaxErrorInvalidRetry(this));
        } catch (Throwable t) {
            if (translateExceptionNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                translateExceptionNode = insert(TranslateExceptionNode.create());
            }
            throw translateExceptionNode.executeTranslation(t, UnsupportedOperationBehavior.TYPE_ERROR);
        }
    }

}
