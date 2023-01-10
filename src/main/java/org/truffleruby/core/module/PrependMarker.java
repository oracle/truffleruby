/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;


public class PrependMarker extends ModuleChain {

    private final ModuleFields origin;

    public PrependMarker(ModuleFields origin) {
        super(origin);
        assert origin != null;
        this.origin = origin;
    }

    @Override
    public RubyModule getActualModule() {
        throw new UnsupportedOperationException();
    }

    public ModuleFields getOrigin() {
        return origin;
    }

}
