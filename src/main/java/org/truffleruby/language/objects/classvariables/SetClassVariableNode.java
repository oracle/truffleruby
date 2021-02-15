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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.FinalLocationException;
import com.oracle.truffle.api.object.IncompatibleLocationException;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "module", type = RubyNode.class)
@NodeChild(value = "name", type = RubyNode.class)
@NodeChild(value = "value", type = RubyNode.class)
public abstract class SetClassVariableNode extends RubyContextSourceNode {

    public static SetClassVariableNode create() {
        return SetClassVariableNodeGen.create(null, null, null);
    }

    public abstract Object execute(RubyModule module, String name, Object value);

    @Specialization(guards = { "name == cachedName", "module.getClassVariables().getShape() == cachedClassVariableStorageShape", "cachedProperty != null" })
    protected Object setClassVariable(RubyModule module, String name, Object value,
                                      @Cached("name") String cachedName,
                                      @Cached("module.getClassVariables()") ClassVariableStorage cachedClassVariableStorage,
                                      @Cached("cachedClassVariableStorage.getShape()") Shape cachedClassVariableStorageShape,
                                      @Cached("cachedClassVariableStorage.getShape().getProperty(cachedName)") Property cachedProperty) {
        try {
            cachedProperty.set(cachedClassVariableStorage, value, cachedClassVariableStorageShape);
        } catch (IncompatibleLocationException | FinalLocationException e) {
            uncachedSetClassVariableNode(module, name, value);
        }

        return value;
    }

    @Specialization(replaces = "setClassVariable")
    protected Object uncachedSetClassVariableNode(RubyModule module, String name, Object value) {
        ModuleOperations.setClassVariable(getLanguage(), getContext(), module, name, value, this);
        return value;
    }

}
