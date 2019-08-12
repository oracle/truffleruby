/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import java.util.Iterator;

import com.oracle.truffle.api.object.DynamicObject;

public class AncestorIterator implements Iterator<DynamicObject> {

    ModuleChain module;

    public AncestorIterator(ModuleChain top) {
        module = top;
    }

    @Override
    public boolean hasNext() {
        return module != null;
    }

    @Override
    public DynamicObject next() {
        assert hasNext();

        ModuleChain mod = module;
        if (mod instanceof PrependMarker) {
            mod = mod.getParentModule();
        }

        module = mod.getParentModule();

        return mod.getActualModule();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
