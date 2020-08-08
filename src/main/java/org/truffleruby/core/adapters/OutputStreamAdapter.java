/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.adapters;

import java.io.OutputStream;

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.support.RubyIO;

public class OutputStreamAdapter extends OutputStream {

    private final RubyContext context;
    private final RubyIO object;
    private final Encoding encoding;

    public OutputStreamAdapter(RubyContext context, RubyIO object, Encoding encoding) {
        this.context = context;
        this.object = object;
        this.encoding = encoding;
    }

    @Override
    public void write(int bite) {
        context.send(
                object,
                "write",
                StringOperations
                        .createString(context, RopeOperations.create((byte) bite, encoding, CodeRange.CR_UNKNOWN)));
    }

}
