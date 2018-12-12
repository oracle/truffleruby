/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.options;

public class UnknownOptionException extends UnsupportedOperationException {

    private static final long serialVersionUID = 94889894853948L;

    private final String name;

    public UnknownOptionException(String name) {
        super(String.format("Unknown option %s", name));
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
