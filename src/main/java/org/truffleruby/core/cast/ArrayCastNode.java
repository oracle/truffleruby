/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/*
 * TODO(CS): could probably unify this with SplatCastNode with some final configuration getContext().getOptions().
 */
@NodeChild("child")
public abstract class ArrayCastNode extends RubyNode {

    private final SplatCastNode.NilBehavior nilBehavior;

    @Child private CallDispatchHeadNode toArrayNode = CallDispatchHeadNode.createReturnMissing();

    public ArrayCastNode() {
        this(SplatCastNode.NilBehavior.NIL);
    }

    public ArrayCastNode(SplatCastNode.NilBehavior nilBehavior) {
        this.nilBehavior = nilBehavior;
    }

    protected abstract RubyNode getChild();

    @Specialization
    protected DynamicObject cast(boolean value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(int value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(long value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(double value) {
        return nil();
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected DynamicObject castBignum(DynamicObject value) {
        return nil();
    }

    @Specialization(guards = "isRubyArray(array)")
    protected DynamicObject castArray(DynamicObject array) {
        return array;
    }

    @Specialization(guards = "isNil(nil)")
    protected Object cast(Object nil) {
        switch (nilBehavior) {
            case EMPTY_ARRAY:
                return createArray(ArrayStrategy.NULL_ARRAY_STORE, 0);

            case ARRAY_WITH_NIL:
                return createArray(new Object[]{ nil() }, 1);

            case NIL:
                return nil;

            default: {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new UnsupportedOperationException();
            }
        }
    }

    @Specialization(guards = { "!isNil(object)", "!isRubyBignum(object)", "!isRubyArray(object)" })
    protected Object cast(VirtualFrame frame, DynamicObject object,
            @Cached BranchProfile errorProfile) {
        final Object result = toArrayNode.call(object, "to_ary");

        if (result == nil()) {
            return nil();
        }

        if (result == DispatchNode.MISSING) {
            return nil();
        }

        if (!RubyGuards.isRubyArray(result)) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(object, "Array", "to_ary", result, this));
        }

        return result;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        getChild().doExecuteVoid(frame);
    }

}
