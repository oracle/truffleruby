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
    TOP_LEVEL_FIRST(true),
    /** required or loaded */
    TOP_LEVEL(true),
    /** class_eval */
    MODULE(false),
    /** eval */
    EVAL(false),
    /** SnippetNode and DebugHelpers.eval() */
    INLINE(false);

    private boolean topLevel;

    private ParserContext(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public boolean isTopLevel() {
        return topLevel;
    }
}
