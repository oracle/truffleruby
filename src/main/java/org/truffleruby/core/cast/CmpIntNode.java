/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code modified from JRuby's RubyComparable.java
 *
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 * Copyright (C) 2006 Thomas E Enebo <enebo@acm.org>
 */

package org.truffleruby.core.cast;

import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.objects.LogicalClassNode;

/** This is a port of MRI's rb_cmpint, as taken from RubyComparable and broken out into specialized nodes. */
public abstract class CmpIntNode extends RubyBaseNode {

    public static CmpIntNode create() {
        return CmpIntNodeGen.create();
    }

    public abstract int executeCmpInt(Object cmpResult, Object a, Object b);

    @Specialization
    protected int cmpInt(int value, Object receiver, Object other) {
        return value;
    }

    @Specialization
    protected int cmpLong(long value, Object receiver, Object other) {
        if (value > 0) {
            return 1;
        }

        if (value < 0) {
            return -1;
        }

        return 0;
    }

    @Specialization
    protected int cmpBignum(RubyBignum value, Object receiver, Object other) {
        return value.value.signum();
    }

    @Specialization
    protected int cmpNil(Nil nil, Object receiver, Object other) {
        throw new RaiseException(getContext(), coreExceptions().argumentError(formatMessage(receiver, other), this));
    }

    @TruffleBoundary
    private String formatMessage(Object receiver, Object other) {
        return StringUtils.format(
                "comparison of %s with %s failed",
                LogicalClassNode.getUncached().execute(receiver).fields.getName(),
                LogicalClassNode.getUncached().execute(other).fields.getName());
    }

    @Specialization(guards = { "!isRubyInteger(value)", "!isNil(value)" })
    protected int cmpObject(Object value, Object receiver, Object other,
            @Cached DispatchNode gtNode,
            @Cached DispatchNode ltNode,
            @Cached BooleanCastNode gtCastNode,
            @Cached BooleanCastNode ltCastNode) {

        if (gtCastNode.executeToBoolean(gtNode.call(value, ">", 0))) {
            return 1;
        }

        if (ltCastNode.executeToBoolean(ltNode.call(value, "<", 0))) {
            return -1;
        }

        return 0;
    }
}
