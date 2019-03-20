/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.truffleruby.collections.ConcurrentOperations;

public class GlobalVariables {

    private final DynamicObject defaultValue;
    private final ConcurrentMap<String, GlobalVariableStorage> variables = new ConcurrentHashMap<>();

    public GlobalVariables(DynamicObject defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * The returned storage must be checked if it is still valid with
     * {@link GlobalVariableStorage#getValidAssumption()}. A storage
     * becomes invalid when it is aliased and therefore the storage
     * instance needs to change.
     */
    @TruffleBoundary
    public GlobalVariableStorage getStorage(String name) {
        return ConcurrentOperations.getOrCompute(variables, name,
                k -> new GlobalVariableStorage(defaultValue, null, null, null));
    }

    public GlobalVariableReader getReader(String name) {
        return new GlobalVariableReader(this, name);
    }

    public GlobalVariableStorage put(String name, Object value) {
        assert !variables.containsKey(name);
        final GlobalVariableStorage storage = new GlobalVariableStorage(value, defaultValue, null, null, null);
        variables.put(name, storage);
        return storage;
    }

    public GlobalVariableStorage put(String name, DynamicObject getter, DynamicObject setter, DynamicObject isDefined) {
        assert !variables.containsKey(name);
        final GlobalVariableStorage storage = new GlobalVariableStorage(defaultValue, getter, setter, isDefined);
        variables.put(name, storage);
        return storage;
    }

    @TruffleBoundary
    public void alias(String oldName, String newName) {
        final GlobalVariableStorage storage = getStorage(oldName);

        final GlobalVariableStorage previousStorage = variables.put(newName, storage);
        if (previousStorage != null) {
            previousStorage.getValidAssumption().invalidate();
        }
    }

    @TruffleBoundary
    public String[] keys() {
        return variables.keySet().toArray(new String[0]);
    }

    @TruffleBoundary
    public Collection<DynamicObject> dynamicObjectValues() {
        final Collection<GlobalVariableStorage> storages = variables.values();
        final ArrayList<DynamicObject> values = new ArrayList<>(storages.size());
        for (GlobalVariableStorage storage : storages) {
            // TODO CS 11-Mar-17 handle hooked global variable storage?
            if (!storage.hasHooks()) {
                final Object value = storage.getValue();
                if (value instanceof DynamicObject) {
                    values.add((DynamicObject) value);
                }
            }
        }
        return values;
    }

}
