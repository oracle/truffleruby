/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;

public class PropagateTaintNode extends RubyBaseWithoutContextNode {

    @Child private IsTaintedNode isTaintedNode = IsTaintedNode.create();
    @Child private TaintNode taintNode;

    public static PropagateTaintNode create() {
        return new PropagateTaintNode();
    }

    public void propagate(DynamicObject source, Object target) {
        if (isTaintedNode.executeIsTainted(source)) {
            taint(target);
        }
    }

    private void taint(Object target) {
        if (taintNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            taintNode = insert(TaintNode.create());
        }
        taintNode.executeTaint(target);
    }

}
