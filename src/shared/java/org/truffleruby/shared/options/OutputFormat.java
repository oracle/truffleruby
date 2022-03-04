/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import java.util.Locale;

public enum Verbosity {
    NIL,
    FALSE,
    TRUE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
