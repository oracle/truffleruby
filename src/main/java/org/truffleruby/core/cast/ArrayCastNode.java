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

import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;

/*
 * TODO(CS): could probably unify this with SplatCastNode with some final configuration getContext().getOptions().
 */

/** See also org.truffleruby.core.array.ArrayConvertNode */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ArrayCastNode extends RubyContextSourceNode {

    private final SplatCastNode.NilBehavior nilBehavior;

    @Child private DispatchNode toArrayNode = DispatchNode.create(PRIVATE_RETURN_MISSING);

    public static ArrayCastNode create() {
        return ArrayCastNodeGen.create(null);
    }

    public ArrayCastNode() {
        this(SplatCastNode.NilBehavior.NIL);
    }

    public ArrayCastNode(SplatCastNode.NilBehavior nilBehavior) {
        this.nilBehavior = nilBehavior;
    }

    public abstract Object execute(Object value);

    protected abstract RubyNode getChild();

    @Specialization
    protected Object cast(boolean value) {
        return nil;
    }

    @Specialization
    protected Object cast(int value) {
        return nil;
    }

    @Specialization
    protected Object cast(long value) {
        return nil;
    }

    @Specialization
    protected Object cast(double value) {
        return nil;
    }

    @Specialization
    protected Object castBignum(RubyBignum value) {
        return nil;
    }

    @Specialization
    protected RubyArray castArray(RubyArray array) {
        return array;
    }

    @Specialization
    protected Object cast(Nil nil) {
        switch (nilBehavior) {
            case EMPTY_ARRAY:
                return ArrayHelpers.createEmptyArray(getContext());

            case ARRAY_WITH_NIL:
                return createArray(new Object[]{ nil });

            case NIL:
                return nil;

            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    }

    @Specialization(guards = { "!isRubyBignum(object)", "!isRubyArray(object)" })
    protected Object cast(RubyDynamicObject object,
            @Cached BranchProfile errorProfile) {
        final Object result = toArrayNode.call(object, "to_ary");

        if (result == nil) {
            return nil;
        }

        if (result == DispatchNode.MISSING) {
            return nil;
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
