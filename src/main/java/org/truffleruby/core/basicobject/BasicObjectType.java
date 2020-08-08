/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.basicobject;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.interop.RubyObjectType;
import org.truffleruby.interop.messages.RubyObjectMessages;

public class BasicObjectType extends RubyObjectType {

    protected final RubyClass logicalClass;
    protected final RubyClass metaClass;

    public BasicObjectType(RubyClass logicalClass, RubyClass metaClass) {
        this.logicalClass = logicalClass;
        this.metaClass = metaClass;
    }

    public RubyClass getLogicalClass() {
        return logicalClass;
    }

    public BasicObjectType setLogicalClass(RubyClass logicalClass) {
        return new BasicObjectType(logicalClass, metaClass);
    }

    public RubyClass getMetaClass() {
        return metaClass;
    }

    public BasicObjectType setMetaClass(RubyClass metaClass) {
        return new BasicObjectType(logicalClass, metaClass);
    }

    @Override
    public Class<?> dispatch() {
        return RubyObjectMessages.class;
    }

}
