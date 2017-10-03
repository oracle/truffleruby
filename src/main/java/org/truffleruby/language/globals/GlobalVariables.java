/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.truffleruby.collections.ConcurrentOperations;

public class GlobalVariables {

    public static final Set<String> READ_ONLY_GLOBAL_VARIABLES = new HashSet<>(
            Arrays.asList("$:", "$LOAD_PATH", "$-I", "$\"", "$LOADED_FEATURES", "$<", "$FILENAME", "$?", "$-a", "$-l", "$-p", "$!"));

    public static final Set<String> ALWAYS_DEFINED_GLOBALS = new HashSet<>(Arrays.asList("$!", "$~", "$SAFE"));

    public static final Set<String> THREAD_LOCAL_GLOBAL_VARIABLES = new HashSet<>(Arrays.asList("$!", "$?", "$SAFE"));

    public static final Set<String> THREAD_AND_FRAME_LOCAL_GLOBAL_VARIABLES = new HashSet<>(Arrays.asList("$~", "$_"));

    public static final Set<String> BACKREF_GLOBAL_VARIABLES = new HashSet<>(
            Arrays.asList("$+", "$&", "$`", "$'", "$1", "$2", "$3", "$4", "$5", "$6", "$7", "$8", "$9"));

    static {
        READ_ONLY_GLOBAL_VARIABLES.addAll(BACKREF_GLOBAL_VARIABLES);
    }

    private final DynamicObject defaultValue;
    private final ConcurrentMap<String, GlobalVariableStorage> variables = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    public GlobalVariables(DynamicObject defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOriginalName(String name) {
        return aliases.getOrDefault(name, name);
    }

    @TruffleBoundary
    public GlobalVariableStorage getStorage(String name) {
        return ConcurrentOperations.getOrCompute(variables, name,
                k -> new GlobalVariableStorage(defaultValue, null, null));
    }

    public GlobalVariableStorage put(String name, Object value) {
        assert !variables.containsKey(name);
        final GlobalVariableStorage storage = new GlobalVariableStorage(value, null, null);
        variables.put(name, storage);
        return storage;
    }

    public GlobalVariableStorage put(String name, DynamicObject getter, DynamicObject setter) {
        assert !variables.containsKey(name);
        final GlobalVariableStorage storage = new GlobalVariableStorage(null, getter, setter);
        variables.put(name, storage);
        return storage;
    }

    @TruffleBoundary
    public void alias(String oldName, String newName) {
        aliases.put(newName, oldName);
        final GlobalVariableStorage storage = getStorage(oldName);
        variables.put(newName, storage);
    }

    @TruffleBoundary
    public Collection<String> keys() {
        return variables.keySet();
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
