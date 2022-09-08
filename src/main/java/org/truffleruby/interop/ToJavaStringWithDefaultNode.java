/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** Convert a Ruby String or Symbol to a Java string, or return a default string if a value was not provided. */
@NodeChild(value = "valueNode", type = RubyNode.class)
public abstract class ToJavaStringWithDefaultNode extends RubyContextSourceNode {

    private final String defaultValue;

    public ToJavaStringWithDefaultNode(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    abstract RubyNode getValueNode();

    @Specialization
    protected String doDefault(NotProvided value) {
        return defaultValue;
    }

    @Specialization(guards = "wasProvided(value)")
    protected String doProvided(Object value,
            @Cached ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = ToJavaStringWithDefaultNodeGen.create(
                defaultValue,
                getValueNode().cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
