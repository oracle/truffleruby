/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.truffleruby.RubyContext;

public class InteropManager {

    private final RubyContext context;

    private final Map<String, Object> exported = new ConcurrentHashMap<>();

    public InteropManager(RubyContext context) {
        this.context = context;
    }

    public void exportObject(String name, Object object) {
        exported.put(name, object);
        context.getEnv().exportSymbol(name, object);
    }

    public Object findExportedObject(String name) {
        return exported.get(name);
    }

    public Object importObject(String name) {
        return context.getEnv().importSymbol(name);
    }

}
