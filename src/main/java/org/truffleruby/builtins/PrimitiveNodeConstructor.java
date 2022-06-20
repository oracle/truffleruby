/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.numeric.FixnumLowerNodeGen;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.Translator;

import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.source.Source;

public class PrimitiveNodeConstructor {

    private final Primitive annotation;
    private final NodeFactory<? extends RubyBaseNode> factory;

    public PrimitiveNodeConstructor(Primitive annotation, NodeFactory<? extends RubyBaseNode> factory) {
        this.annotation = annotation;
        this.factory = factory;
    }

    public int getPrimitiveArity() {
        return factory.getExecutionSignature().size();
    }

    public RubyNode createInvokePrimitiveNode(Source source, SourceIndexLength sourceSection, RubyNode[] arguments) {
        if (arguments.length != getPrimitiveArity()) {
            throw new Error(
                    "Incorrect number of arguments (expected " + getPrimitiveArity() + ") at " +
                            RubyLanguage.getCurrentContext().fileLine(sourceSection.toSourceSection(source)));
        }

        for (int n = 0; n < arguments.length; n++) {
            if (ArrayUtils.contains(annotation.lowerFixnum(), n)) {
                arguments[n] = FixnumLowerNodeGen.create(arguments[n]);
            }
            if (ArrayUtils.contains(annotation.raiseIfFrozen(), n)) {
                arguments[n] = TypeNodes.CheckFrozenNode.create(arguments[n]);
            }
            if (ArrayUtils.contains(annotation.raiseIfNotMutable(), n)) {
                arguments[n] = TypeNodes.CheckMutableStringNode.create(arguments[n]);
            }
        }

        final RubyNode primitiveNode = (RubyNode) CoreMethodNodeManager.createNodeFromFactory(factory, arguments);
        return Translator.withSourceSection(sourceSection, primitiveNode);
    }

}
