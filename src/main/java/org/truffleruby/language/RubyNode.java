/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;
import com.oracle.truffle.api.source.SourceSection;

@Instrumentable(factory = RubyNodeWrapper.class)
public abstract class RubyNode extends RubyBaseNode {

    // Fundamental execute methods

    public abstract Object execute(VirtualFrame frame);

    public void executeVoid(VirtualFrame frame) {
        execute(frame);
    }

    public Object isDefined(VirtualFrame frame) {
        return coreStrings().EXPRESSION.createInstance();
    }

    // Boundaries

    @TruffleBoundary
    @Override
    public SourceSection getEncapsulatingSourceSection() {
        return super.getEncapsulatingSourceSection();
    }

}
