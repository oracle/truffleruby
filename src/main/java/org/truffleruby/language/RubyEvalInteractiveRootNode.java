/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.backtrace.InternalRootNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.Source;

public class RubyEvalInteractiveRootNode extends RubyBaseRootNode implements InternalRootNode {

    private final Rope sourceRope;

    @CompilationFinal private TruffleLanguage.ContextReference<RubyContext> contextReference;

    public RubyEvalInteractiveRootNode(RubyLanguage language, Source source) {
        super(language, null, null);
        this.sourceRope = StringOperations.encodeRope(source.getCharacters().toString(), UTF8Encoding.INSTANCE);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = lookupContextReference(RubyLanguage.class);
        }
        final RubyContext context = contextReference.get();

        // Just do Truffle::Boot::INTERACTIVE_BINDING.eval(code) for interactive sources.
        // It's the semantics we want and takes care of caching correctly based on the Binding's FrameDescriptor.
        final Object interactiveBinding = context.getCoreLibrary().truffleBootModule.fields
                .getConstant("INTERACTIVE_BINDING")
                .getValue();
        return context
                .send(interactiveBinding, "eval", StringOperations.createString(context, sourceRope));

    }

}
