/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.ModuleBodyDefinitionNode;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;

public class RunModuleDefinitionNode extends RubyContextSourceNode {

    final protected LexicalScope lexicalScope;

    @Child private RubyNode definingModule;
    @Child private ModuleBodyDefinitionNode definitionMethod;
    @Child private IndirectCallNode callModuleDefinitionNode = Truffle.getRuntime().createIndirectCallNode();

    public RunModuleDefinitionNode(
            LexicalScope lexicalScope,
            ModuleBodyDefinitionNode definition,
            RubyNode definingModule) {
        this.definingModule = definingModule;
        this.definitionMethod = definition;
        this.lexicalScope = lexicalScope;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyModule module = (RubyModule) definingModule.execute(frame);
        final InternalMethod definition = definitionMethod.createMethod(frame, lexicalScope, module);

        return callModuleDefinitionNode.call(definition.getCallTarget(), RubyArguments.pack(
                null,
                null,
                null,
                definition,
                null,
                module,
                null,
                EMPTY_ARGUMENTS));
    }

}
