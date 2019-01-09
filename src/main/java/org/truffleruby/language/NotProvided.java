/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.NotProvidedForeign;

/**
 * Represents a value that was not provided by the user, such as optional arguments to a core library node.
 */
@MessageResolution(receiverType = NotProvided.class)
public final class NotProvided implements TruffleObject {

    public static final NotProvided INSTANCE = new NotProvided();

    private NotProvided() {
    }

    public static boolean isInstance(TruffleObject obj) {
        return obj instanceof NotProvided;
    }

    @Resolve(message = "IS_NULL")
    public static abstract class IsNullNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class HasSizeNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "IS_BOXED")
    public static abstract class IsBoxedNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "IS_EXECUTABLE")
    public static abstract class IsExecutableNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Override
    public ForeignAccess getForeignAccess() {
        return NotProvidedForeign.ACCESS;
    }

}
