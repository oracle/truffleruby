package org.truffleruby.language;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;

/** Has both context but nothing else. */
public abstract class ContextRubyNode extends BaseRubyNode implements RubyNode.WithContext {

    @CompilerDirectives.CompilationFinal private RubyContext context;

    // Accessors

    @Override
    public RubyContext getContext() {
        if (context == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            context = RubyLanguage.getCurrentContext();
        }

        return context;
    }

}
