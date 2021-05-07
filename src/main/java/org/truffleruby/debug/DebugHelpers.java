/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import com.oracle.truffle.api.RootCallTarget;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.source.Source;

public abstract class DebugHelpers {

    @Deprecated
    public static Object eval(String code, Object... arguments) {
        return eval(RubyLanguage.getCurrentContext(), code, arguments);
    }

    @Deprecated
    @TruffleBoundary
    public static Object eval(RubyContext context, String code, Object... arguments) {
        final Frame currentFrame = context.getCallStack().getCurrentFrame(FrameAccess.MATERIALIZE);

        final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(currentFrame);

        final Object[] packedArguments = RubyArguments.pack(
                null,
                null,
                RubyArguments.getMethod(currentFrame),
                declarationContext,
                null,
                RubyArguments.getSelf(currentFrame),
                Nil.INSTANCE,
                RubyNode.EMPTY_ARGUMENTS);

        final FrameDescriptor frameDescriptor = new FrameDescriptor(
                currentFrame.getFrameDescriptor().getDefaultValue());

        final MaterializedFrame evalFrame = Truffle.getRuntime().createMaterializedFrame(
                packedArguments,
                frameDescriptor);

        if (arguments.length % 2 == 1) {
            throw new UnsupportedOperationException("odd number of name-value pairs for arguments");
        }

        for (int n = 0; n < arguments.length; n += 2) {
            final Object identifier = arguments[n];
            assert !(identifier == null || (identifier instanceof String && ((String) identifier).isEmpty()));
            evalFrame.setObject(evalFrame.getFrameDescriptor().findOrAddFrameSlot(identifier), arguments[n + 1]);
        }

        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, code, "debug-eval").build();

        final RootCallTarget callTarget = context
                .getCodeLoader()
                .parse(new RubySource(source, "debug-eval"), ParserContext.INLINE, evalFrame, null, true, null);

        final CodeLoader.DeferredCall deferredCall = context.getCodeLoader().prepareExecute(
                callTarget,
                ParserContext.INLINE,
                declarationContext,
                evalFrame,
                RubyArguments.getSelf(evalFrame));

        return deferredCall.callWithoutCallNode();
    }

}
