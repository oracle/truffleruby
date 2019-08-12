/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.RubyContext;
import org.truffleruby.core.RaiseIfFrozenNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.numeric.FixnumLowerNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.parser.Translator;

import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.source.Source;

public class PrimitiveNodeConstructor {

    private final Primitive annotation;
    private final NodeFactory<? extends RubyNode> factory;

    public PrimitiveNodeConstructor(Primitive annotation, NodeFactory<? extends RubyNode> factory) {
        this.annotation = annotation;
        this.factory = factory;
        if (CoreMethodNodeManager.CHECK_DSL_USAGE) {
            LowerFixnumChecker.checkLowerFixnumArguments(factory, annotation.needsSelf() ? 1 : 0, annotation.lowerFixnum());
        }
    }

    public int getPrimitiveArity() {
        return factory.getExecutionSignature().size();
    }

    public RubyNode createCallPrimitiveNode(SourceIndexLength sourceSection, RubyNode fallback) {
        final RubyNode[] arguments = new RubyNode[getPrimitiveArity()];
        int argumentsCount = getPrimitiveArity();
        int start = 0;

        if (annotation.needsSelf()) {
            arguments[0] = transformArgument(ProfileArgumentNodeGen.create(new ReadSelfNode()), 0);
            start++;
            argumentsCount--;
        }

        for (int n = 0; n < argumentsCount; n++) {
            RubyNode readArgumentNode = ProfileArgumentNodeGen.create(new ReadPreArgumentNode(n, MissingArgumentBehavior.NOT_PROVIDED));
            arguments[start + n] = transformArgument(readArgumentNode, n + 1);
        }

        final RubyNode primitiveNode = CoreMethodNodeManager.createNodeFromFactory(factory, arguments);

        return Translator.withSourceSection(sourceSection, new CallPrimitiveNode(primitiveNode, fallback));
    }

    public RubyNode createInvokePrimitiveNode(RubyContext context, Source source, SourceIndexLength sourceSection, RubyNode[] arguments) {
        if (arguments.length != getPrimitiveArity()) {
            throw new Error("Incorrect number of arguments (expected " + getPrimitiveArity() + ") at " + context.fileLine(sourceSection.toSourceSection(source)));
        }

        for (int n = 0; n < arguments.length; n++) {
            int nthArg = annotation.needsSelf() ? n : n + 1;
            arguments[n] = transformArgument(arguments[n], nthArg);
        }

        final RubyNode primitiveNode = CoreMethodNodeManager.createNodeFromFactory(factory, arguments);

        return Translator.withSourceSection(sourceSection, new InvokePrimitiveNode(primitiveNode));
    }

    private RubyNode transformArgument(RubyNode argument, int n) {
        if (ArrayUtils.contains(annotation.lowerFixnum(), n)) {
            return FixnumLowerNodeGen.create(argument);
        } else if (n == 0 && annotation.raiseIfFrozenSelf()) {
            return new RaiseIfFrozenNode(argument);
        } else {
            return argument;
        }
    }

}
