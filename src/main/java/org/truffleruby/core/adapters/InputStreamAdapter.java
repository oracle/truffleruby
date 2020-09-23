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

import org.graalvm.shadowed.org.jline.utils.NonBlockingInputStream;
import org.truffleruby.RubyContext;
import org.truffleruby.core.support.RubyIO;
import org.truffleruby.language.Nil;

/** extends NonBlockingInputStream so JLine does not create an extra thread and calls read() on the same thread as
 * calling readLine(). */
public class InputStreamAdapter extends NonBlockingInputStream {

    private final RubyContext context;
    private final RubyIO object;

    private int peeked = EOF;

    public InputStreamAdapter(RubyContext context, RubyIO object) {
        this.context = context;
        this.object = object;
    }

    @Override
    public int read() {
        final Object result = context.send(object, "getbyte");

        if (result == Nil.INSTANCE) {
            return EOF;
        }

        return (int) result;
    }

    @Override
    public int read(long timeout, boolean isPeek) {
        if (peeked != EOF) {
            int c = peeked;
            this.peeked = EOF;
            return c;
        }

        int character = read();
        if (isPeek) {
            peeked = character;
        }
        return character;
    }
}
