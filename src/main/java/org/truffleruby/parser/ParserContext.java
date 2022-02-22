/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

public enum ParserContext {
    /** The main script */
    TOP_LEVEL_FIRST(true, false),
    /** required or loaded */
    TOP_LEVEL(true, false),
    /** class_eval */
    MODULE(false, true),
    /** instance_eval */
    INSTANCE_EVAL(false, true),
    /** Kernel#eval which is special because it sets new variables in the source in the Binding */
    EVAL(false, true),
    /** DebugHelpers.eval() or InlineParsingRequest, like EVAL but without the new variables handling */
    INLINE(false, true);

    private final boolean topLevel;
    private final boolean eval;

    private ParserContext(boolean topLevel, boolean eval) {
        this.topLevel = topLevel;
        this.eval = eval;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public boolean isEval() {
        return eval;
    }
}
