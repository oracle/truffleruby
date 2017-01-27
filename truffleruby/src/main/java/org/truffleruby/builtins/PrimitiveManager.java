/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.builtins;

import com.oracle.truffle.api.dsl.NodeFactory;
import org.truffleruby.core.rubinius.UndefinedPrimitiveNodes;
import org.truffleruby.language.RubyNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the available Rubinius primitive calls.
 */
public class PrimitiveManager {

    private final Map<String, PrimitiveNodeConstructor> primitives = new ConcurrentHashMap<>();

    public PrimitiveNodeConstructor getPrimitive(String name) {
        final PrimitiveNodeConstructor constructor = primitives.get(name);

        if (constructor == null) {
            return primitives.get(UndefinedPrimitiveNodes.NAME);
        }

        return constructor;
    }

    public void addPrimitive(NodeFactory<? extends RubyNode> nodeFactory, Primitive annotation) {
        primitives.put(annotation.name(), new PrimitiveNodeConstructor(annotation, nodeFactory));
    }
}
