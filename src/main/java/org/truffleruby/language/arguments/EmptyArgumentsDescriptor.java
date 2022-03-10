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

/** An arguments descriptor for where we're providing no information about the value of arguments being passed. */
public class EmptyArgumentsDescriptor extends ArgumentsDescriptor {

    public static final EmptyArgumentsDescriptor INSTANCE = new EmptyArgumentsDescriptor();

    private EmptyArgumentsDescriptor() {
    }

}
