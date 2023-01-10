/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.classvariables;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

public abstract class ResolveTargetModuleForClassVariablesNode extends RubyBaseNode {

    public static ResolveTargetModuleForClassVariablesNode create() {
        return ResolveTargetModuleForClassVariablesNodeGen.create();
    }

    public abstract RubyModule execute(LexicalScope lexicalScope);

    @Specialization(guards = { "isSingleContext()", "lexicalScope == cachedLexicalScope" })
    protected RubyModule cached(LexicalScope lexicalScope,
            @Cached("lexicalScope") LexicalScope cachedLexicalScope,
            @Cached("uncached(lexicalScope)") RubyModule cachedModule) {
        return cachedModule;
    }

    @Specialization(replaces = "cached")
    protected RubyModule uncached(LexicalScope lexicalScope) {
        LexicalScope scope = lexicalScope;

        // MRI logic: ignore lexical scopes (cref) referring to singleton classes
        while (RubyGuards.isSingletonClass(scope.getLiveModule())) {
            scope = scope.getParent();
        }

        return scope.getLiveModule();
    }

}
