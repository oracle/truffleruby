/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.classvariables;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "lexicalScopeNode", type = RubyNode.class)
public abstract class ResolveTargetModuleForClassVariablesNode extends RubyContextSourceNode {

    public static ResolveTargetModuleForClassVariablesNode create() {
        return ResolveTargetModuleForClassVariablesNodeGen.create(null);
    }

    public abstract RubyModule execute(LexicalScope lexicalScope);

    @TruffleBoundary
    @Specialization
    protected RubyModule resolveTargetModuleForClassVariables(LexicalScope lexicalScope) {
        LexicalScope scope = lexicalScope;

        // MRI logic: ignore lexical scopes (cref) referring to singleton classes
        while (RubyGuards.isSingletonClass(scope.getLiveModule())) {
            scope = scope.getParent();
        }

        return scope.getLiveModule();
    }

}
