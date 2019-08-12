/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.adapters;

import java.io.IOException;
import java.io.OutputStream;

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;

import com.oracle.truffle.api.object.DynamicObject;

public class OutputStreamAdapter extends OutputStream {

    private final RubyContext context;
    private final DynamicObject object;
    private final Encoding encoding;

    public OutputStreamAdapter(RubyContext context, DynamicObject object, Encoding encoding) {
        this.context = context;
        this.object = object;
        this.encoding = encoding;
    }

    @Override
    public void write(int bite) throws IOException {
        context.send(object, "write", StringOperations.createString(context, RopeOperations.create((byte) bite, encoding, CodeRange.CR_UNKNOWN)));
    }

}
