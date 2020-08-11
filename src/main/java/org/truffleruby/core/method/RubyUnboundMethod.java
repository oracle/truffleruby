/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

@ExportLibrary(InteropLibrary.class)
public class RubyUnboundMethod extends RubyDynamicObject implements ObjectGraphNode {

    final RubyModule origin;
    public final InternalMethod method;

    public RubyUnboundMethod(Shape shape, RubyModule origin, InternalMethod method) {
        super(shape);
        this.origin = origin;
        this.method = method;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        reachable.add(origin);
        method.getAdjacentObjects(reachable);
    }

    // region SourceLocation
    @ExportMessage
    public boolean hasSourceLocation() {
        return true;
    }

    @ExportMessage
    public SourceSection getSourceLocation() {
        return method.getSharedMethodInfo().getSourceSection();
    }
    // endregion

}
