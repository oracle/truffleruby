/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.jcodings.Encoding;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.string.FrozenStringLiterals;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.ImmutableRubyObject;

import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

@ExportLibrary(InteropLibrary.class)
public class RubyEncoding extends ImmutableRubyObject implements ObjectGraphNode {

    public final Encoding jcoding;
    public final ImmutableRubyString name;
    public final int index;

    public RubyEncoding(Encoding jcoding, ImmutableRubyString name, int index) {
        this.jcoding = jcoding;
        this.name = name;
        this.index = index;
    }

    // Special constructor to define US-ASCII encoding which is used for RubyEncoding names
    public RubyEncoding(int index) {
        this.jcoding = USASCIIEncoding.INSTANCE;
        this.name = FrozenStringLiterals.encodingName((LeafRope) RopeConstants.US_ASCII, this);
        this.index = index;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, name);
    }

    // region InteropLibrary messages
    @ExportMessage
    protected Object toDisplayString(boolean allowSideEffects,
            @Cached DispatchNode dispatchNode,
            @Cached KernelNodes.ToSNode kernelToSNode) {
        if (allowSideEffects) {
            return dispatchNode.call(this, "inspect");
        } else {
            return kernelToSNode.executeToS(this);
        }
    }

    @ExportMessage
    protected boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    protected RubyClass getMetaObject(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().encodingClass;
    }
    // endregion

}
