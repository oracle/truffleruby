/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

public enum Split {
    ALWAYS,
    HEURISTIC,
    /** Disallow splitting for this CallTarget, which avoids making a eager uninitialized copy of the AST. Useful
     * notably for methods not specializing on their arguments and just calling a TruffleBoundary. */
    NEVER
}
