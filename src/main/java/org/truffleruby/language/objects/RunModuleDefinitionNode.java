/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.ModuleBodyDefinitionNode;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;

public class RunModuleDefinitionNode extends RubyContextSourceNode {

    @Child private RubyNode definingModule;
    @Child private ModuleBodyDefinitionNode definitionMethod;
    @Child private IndirectCallNode callModuleDefinitionNode = Truffle.getRuntime().createIndirectCallNode();

    public RunModuleDefinitionNode(ModuleBodyDefinitionNode definition, RubyNode definingModule) {
        this.definingModule = definingModule;
        this.definitionMethod = definition;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyModule module = (RubyModule) definingModule.execute(frame);
        final InternalMethod definition = definitionMethod.createMethod(frame, module);

        return callModuleDefinitionNode.call(definition.getCallTarget(), RubyArguments.pack(
                null,
                null,
                definition,
                null,
                module,
                nil,
                EmptyArgumentsDescriptor.INSTANCE,
                EMPTY_ARGUMENTS));
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new RunModuleDefinitionNode(
                definitionMethod,
                definingModule.cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
