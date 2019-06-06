/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.symbol.SymbolTable;

@ReportPolymorphism
@GenerateUncached
public abstract class ObjectIVarGetNode extends Node {

    public static ObjectIVarGetNode create() {
        return ObjectIVarGetNodeGen.create();
    }

    public abstract Object executeIVarGet(DynamicObject object, Object name, boolean checkName);

    public final Object executeIVarGet(DynamicObject object, Object name) {
        return executeIVarGet(object, name, false);
    }

    @Specialization(guards = "name == cachedName", limit = "getCacheLimit()")
    public Object ivarGetCached(DynamicObject object, Object name, boolean checkName,
            @Cached("name") Object cachedName,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("createReadFieldNode(context, object, cachedName, checkName)") ReadObjectFieldNode readObjectFieldNode) {
        CompilerAsserts.partialEvaluationConstant(checkName);
        return readObjectFieldNode.execute(object);
    }

    @TruffleBoundary
    @Specialization(replaces = "ivarGetCached")
    public Object ivarGetUncached(DynamicObject object, Object name, boolean checkName,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
        return ReadObjectFieldNode.read(
                object,
                checkName(this, rubyContext, object, name, checkName),
                rubyContext.getCoreLibrary().getNil());
    }

    protected ReadObjectFieldNode createReadFieldNode(RubyContext context, DynamicObject object, Object name, boolean checkName) {
        return ReadObjectFieldNodeGen.create(
                checkName(this, context, object, name, checkName),
                context.getCoreLibrary().getNil());
    }

    public static Object checkName(
            Node currentNode,
            RubyContext context,
            DynamicObject object,
            Object name,
            boolean checkName) {
        return checkName ? SymbolTable.checkInstanceVariableName(context, (String) name, object, currentNode) : name;
    }

    protected int getCacheLimit() {
        // TODO (pitr-ch 05-Jun-2019): ok?
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

}
