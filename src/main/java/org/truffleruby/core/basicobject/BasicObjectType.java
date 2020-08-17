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

import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.klass.RubyClass;

public final class BasicObjectType extends ObjectType {

    protected final RubyClass logicalClass;
    protected final RubyClass metaClass;

    public BasicObjectType(RubyClass logicalClass, RubyClass metaClass) {
        this.logicalClass = logicalClass;
        this.metaClass = metaClass;
    }

    public RubyClass getLogicalClass() {
        assert logicalClass == metaClass.nonSingletonClass;
        return logicalClass;
    }

    public BasicObjectType setLogicalClass(RubyClass logicalClass) {
        return new BasicObjectType(logicalClass, metaClass);
    }

    public RubyClass getMetaClass() {
        assert metaClass.nonSingletonClass == logicalClass;
        return metaClass;
    }

    public BasicObjectType setMetaClass(RubyClass metaClass) {
        return new BasicObjectType(logicalClass, metaClass);
    }

    public static RubyClass getLogicalClass(Shape shape) {
        return ((BasicObjectType) shape.getObjectType()).getLogicalClass();
    }

    public static RubyClass getMetaClass(Shape shape) {
        return ((BasicObjectType) shape.getObjectType()).getMetaClass();
    }

}
