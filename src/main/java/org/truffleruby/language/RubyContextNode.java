/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

/** Has context but nothing else. */
public abstract class RubyContextNode extends RubyBaseNode implements RubyNode.WithContext {

    @CompilationFinal private ContextReference<RubyContext> contextReference;

    @Override
    public RubyContext getContext() {
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = lookupContextReference(RubyLanguage.class);
        }

        return contextReference.get();
    }
}
