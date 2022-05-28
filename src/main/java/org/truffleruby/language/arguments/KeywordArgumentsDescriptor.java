/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

/** An arguments descriptor that says that keyword arguments were passed (either foo(a: 1) or foo(**kw)). Note that
 * currently, if kw is empty, then this descriptor is used even though it is semantically the same as if no keyword
 * arguments were passed. The callee must handle that for now. */
public class KeywordArgumentsDescriptor extends ArgumentsDescriptor {

    public enum SplatType {
        NO_SPLAT,
        ONLY_SPLAT,
        PRE_SPLAT,
        POST_SPLAT
    }

    @CompilationFinal(dimensions = 1) private final String[] keywords;
    private final SplatType splatType;

    /** Use {@link KeywordArgumentsDescriptorManager} to get an instance. */
    KeywordArgumentsDescriptor(String[] keywords, SplatType splatType) {
        this.keywords = keywords;
        this.splatType = splatType;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public SplatType getSplatType() {
        return splatType;
    }
}
