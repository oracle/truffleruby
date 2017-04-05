/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.NotProvided;

@MessageResolution(receiverType = NotProvided.class)
public class NotProvidedMessageResolution {

    @Resolve(message = "IS_NULL")
    public static abstract class NotProvidedIsNullNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class NotProvidedHasSizeNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "IS_BOXED")
    public static abstract class NotProvidedIsBoxedNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

    @Resolve(message = "IS_EXECUTABLE")
    public static abstract class NotProvidedIsExecutableNode extends Node {

        protected boolean access(NotProvided object) {
            return false;
        }

    }

}
