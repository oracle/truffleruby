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

import com.oracle.truffle.api.RootCallTarget;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.LocalReturnException;
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
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

public class RubyLambdaRootNode extends RubyRootNode {

    public static RubyLambdaRootNode of(RootCallTarget callTarget) {
        return (RubyLambdaRootNode) callTarget.getRootNode();
    }

    public final BreakID breakID;
    @CompilationFinal private TruffleLanguage.ContextReference<RubyContext> contextReference;
    @Child private TranslateExceptionNode translateExceptionNode;

    private final BranchProfile localReturnProfile = BranchProfile.create();
    private final ConditionProfile matchingReturnProfile = ConditionProfile.create();
    private final ConditionProfile matchingBreakProfile = ConditionProfile.create();
    private final BranchProfile retryProfile = BranchProfile.create();
    private final BranchProfile redoProfile = BranchProfile.create();
    private final BranchProfile nextProfile = BranchProfile.create();

    public RubyLambdaRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FrameDescriptor frameDescriptor,
            SharedMethodInfo sharedMethodInfo,
            RubyNode body,
            Split split,
            ReturnID returnID,
            BreakID breakID) {
        super(language, sourceSection, frameDescriptor, sharedMethodInfo, body, split, returnID);
        this.breakID = breakID;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        SafepointManager.poll(language, this);

        while (true) {
            try {
                return body.execute(frame);
            } catch (LocalReturnException e) {
                localReturnProfile.enter();
                return e.getValue();
            } catch (DynamicReturnException e) {
                if (matchingReturnProfile.profile(e.getReturnID() == returnID)) {
                    return e.getValue();
                } else {
                    throw e;
                }
            } catch (RetryException e) {
                retryProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().syntaxErrorInvalidRetry(this));
            } catch (RedoException e) {
                redoProfile.enter();
                SafepointManager.poll(language, this);
                continue;
            } catch (NextException e) {
                nextProfile.enter();
                return e.getResult();
            } catch (BreakException e) {
                if (matchingBreakProfile.profile(e.getBreakID() == breakID)) {
                    return e.getResult();
                } else {
                    throw e;
                }
            } catch (Throwable t) {
                if (translateExceptionNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    translateExceptionNode = insert(TranslateExceptionNode.create());
                }
                throw translateExceptionNode.executeTranslation(t, UnsupportedOperationBehavior.TYPE_ERROR);
            }
        }
    }

    public TruffleLanguage.ContextReference<RubyContext> getContextReference() {
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = lookupContextReference(RubyLanguage.class);
        }

        return contextReference;
    }

    public RubyContext getContext() {
        return getContextReference().get();
    }

}
