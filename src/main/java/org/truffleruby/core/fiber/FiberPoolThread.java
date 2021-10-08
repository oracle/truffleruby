/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import org.truffleruby.RubyLanguage.ThreadLocalState;

public final class FiberPoolThread extends Thread {

    public final ThreadLocalState threadLocalState;

    public FiberPoolThread(Runnable target) {
        super(target);
        threadLocalState = new ThreadLocalState();
    }

}
