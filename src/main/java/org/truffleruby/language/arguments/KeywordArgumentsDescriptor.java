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

/** An arguments descriptor that says that keyword arguments are present. */
public class KeywordArgumentsDescriptor extends ArgumentsDescriptor {

    public static final KeywordArgumentsDescriptor INSTANCE = new KeywordArgumentsDescriptor();

    private KeywordArgumentsDescriptor() {
    }

}
