/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
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
    TOP_LEVEL_FIRST("<main>", false),
    /** required or loaded */
    TOP_LEVEL("<top (required)>", false),
    /** class_eval */
    MODULE(null, true),
    /** instance_eval */
    INSTANCE_EVAL(null, true),
    /** Kernel#eval which is special because it sets new variables in the source in the Binding */
    EVAL(null, true),
    /** DebugHelpers.eval() or InlineParsingRequest, like EVAL but without the new variables handling */
    INLINE(null, true);

    private final String topLevelName;
    private final boolean eval;

    private ParserContext(String topLevelName, boolean eval) {
        this.topLevelName = topLevelName;
        this.eval = eval;
    }

    public String getTopLevelName() {
        return topLevelName;
    }

    public boolean isTopLevel() {
        return topLevelName != null;
    }

    public boolean isEval() {
        return eval;
    }
}
