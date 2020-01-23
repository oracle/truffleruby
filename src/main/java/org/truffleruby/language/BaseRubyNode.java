package org.truffleruby.language;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.Node;

/** Base of all Ruby nodes */
@TypeSystemReference(RubyTypes.class)
@ImportStatic(RubyGuards.class)
public abstract class BaseRubyNode extends Node {
    public static final ContextSourceRubyNode[] EMPTY_CONTEXT_SOURCE_RUBY_NODES = new ContextSourceRubyNode[]{};
    public static final RubyNode[] EMPTY_RUBY_NODES = new RubyNode[]{};
    public static final Object[] EMPTY_ARGUMENTS = new Object[]{};
}
