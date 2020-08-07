/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.objects.ObjectGraph;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class GlobalVariables {

    private final ConcurrentMap<String, GlobalVariableStorage> variables = new ConcurrentHashMap<>();

    @TruffleBoundary
    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    /** The returned storage must be checked if it is still valid with
     * {@link GlobalVariableStorage#getValidAssumption()} . A storage becomes invalid when it is aliased and therefore
     * the storage instance needs to change. */
    @TruffleBoundary
    public GlobalVariableStorage getStorage(String name) {
        return ConcurrentOperations.getOrCompute(
                variables,
                name,
                k -> new GlobalVariableStorage(null, null, null));
    }

    public GlobalVariableReader getReader(String name) {
        return new GlobalVariableReader(this, name);
    }

    public GlobalVariableStorage define(String name, Object value) {
        return define(name, new GlobalVariableStorage(value, null, null, null));
    }

    public GlobalVariableStorage define(String name, RubyProc getter, RubyProc setter, RubyProc isDefined) {
        return define(name, new GlobalVariableStorage(getter, setter, isDefined));
    }

    private GlobalVariableStorage define(String name, GlobalVariableStorage storage) {
        final GlobalVariableStorage previous = variables.putIfAbsent(name, storage);
        if (previous != null) {
            throw new IllegalArgumentException("Global variable $" + name + " is already defined");
        }
        return storage;
    }

    @TruffleBoundary
    public void alias(String oldName, String newName) {
        final GlobalVariableStorage storage = getStorage(oldName);

        final GlobalVariableStorage previousStorage = variables.put(newName, storage);
        // If previousStorage == storage, we already have that alias and should not invalidate
        if (previousStorage != null && previousStorage != storage) {
            previousStorage.getValidAssumption().invalidate();
        }
    }

    @TruffleBoundary
    public String[] keys() {
        return variables.keySet().toArray(StringUtils.EMPTY_STRING_ARRAY);
    }

    @TruffleBoundary
    public Collection<Object> objectGraphValues() {
        final Collection<GlobalVariableStorage> storages = variables.values();
        final ArrayList<Object> values = new ArrayList<>(storages.size());
        for (GlobalVariableStorage storage : storages) {
            // TODO CS 11-Mar-17 handle hooked global variable storage?
            if (!storage.hasHooks()) {
                final Object value = storage.getValue();
                if (ObjectGraph.isSymbolOrDynamicObject(value)) {
                    values.add(value);
                }
            }
        }
        return values;
    }

}
