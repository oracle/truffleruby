/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.object.HiddenKey;

import org.truffleruby.parser.TranslatorEnvironment;

public abstract class Layouts {

    // Standard identifiers

    public static final HiddenKey OBJECT_ID_IDENTIFIER = new HiddenKey("object_id"); // long
    public static final HiddenKey FROZEN_IDENTIFIER = new HiddenKey("frozen?"); // boolean
    public static final HiddenKey OBJECT_LOCK = new HiddenKey("object_lock"); // ReentrantLock
    public static final HiddenKey ASSOCIATED_IDENTIFIER = new HiddenKey("associated"); // Pointer[]
    public static final HiddenKey FINALIZER_REF_IDENTIFIER = new HiddenKey("finalizerRef"); // FinalizerReference
    public static final HiddenKey MARKED_OBJECTS_IDENTIFIER = new HiddenKey("marked_objects"); // Object[]
    public static final HiddenKey VALUE_WRAPPER_IDENTIFIER = new HiddenKey("value_wrapper"); // ValueWrapper
    public static final HiddenKey ALLOCATION_TRACE_IDENTIFIER = new HiddenKey("allocation_trace"); // AllocationTrace

    // Frame slot name for special variable storage.

    public static final String SPECIAL_VARIABLES_STORAGE = TranslatorEnvironment.TEMP_PREFIX + "$~_";

}
