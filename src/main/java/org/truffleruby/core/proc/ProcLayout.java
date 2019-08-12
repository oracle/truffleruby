/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

// A instance of Proc behaves either as a proc or lambda (its type).
// Kernel#lambda is the only primitive which can produce a lambda-semantics Proc from a proc-semantics one.
// (possibly Module#define_method as well, but it does not need to be).
// The literal lambda -> *args { body } defines the Proc as lambda directly.
// callTargetForType caches the current CallTarget according to the type for faster access.
// See the documentation of Proc#lambda?, it is a good reference.

@Layout
public interface ProcLayout extends BasicObjectLayout {

    DynamicObjectFactory createProcShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    Object[] build(
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTargetForType,
            RootCallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            @Nullable InternalMethod method,
            @Nullable DynamicObject block,
            @Nullable FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext);

    boolean isProc(ObjectType objectType);

    boolean isProc(DynamicObject object);

    boolean isProc(Object object);

    ProcType getType(DynamicObject object);

    SharedMethodInfo getSharedMethodInfo(DynamicObject object);

    RootCallTarget getCallTargetForType(DynamicObject object);

    RootCallTarget getCallTargetForLambdas(DynamicObject object);

    MaterializedFrame getDeclarationFrame(DynamicObject object);

    InternalMethod getMethod(DynamicObject object);

    DynamicObject getBlock(DynamicObject object);

    FrameOnStackMarker getFrameOnStackMarker(DynamicObject object);

    DeclarationContext getDeclarationContext(DynamicObject object);

}
