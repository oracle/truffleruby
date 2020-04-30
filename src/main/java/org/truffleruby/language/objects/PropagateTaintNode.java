/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class PropagateTaintNode extends RubyBaseNode {

    public static PropagateTaintNode create() {
        return PropagateTaintNodeGen.create();
    }

    public abstract void executePropagate(Object source, Object target);

    @Specialization
    protected void propagate(Object source, Object target,
            @Cached IsTaintedNode isTaintedNode,
            @Cached TaintNode taintNode) {
        if (isTaintedNode.executeIsTainted(source)) {
            taintNode.executeTaint(target);
        }
    }

}
