/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class FlipFlopStateNode extends RubyBaseNode {

    public abstract boolean getState(VirtualFrame frame);

    public abstract void setState(VirtualFrame frame, boolean state);

}
