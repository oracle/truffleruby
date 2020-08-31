/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.MetaClassNode;

@GenerateUncached
public abstract class LookupMethodOnSelfNode extends RubyBaseNode {

    public static LookupMethodOnSelfNode create() {
        return LookupMethodOnSelfNodeGen.create();
    }

    public InternalMethod lookup(VirtualFrame frame, Object self, String name) {
        return execute(frame, self, name, false, false);
    }

    public InternalMethod lookup(
            VirtualFrame frame, Object self, String name, boolean ignoreVisibility, boolean onlyLookupPublic) {
        return execute(frame, self, name, ignoreVisibility, onlyLookupPublic);
    }

    public InternalMethod lookupIgnoringVisibility(VirtualFrame frame, Object self, String name) {
        return execute(frame, self, name, true, false);
    }

    protected abstract InternalMethod execute(Frame frame, Object self, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic);

    @Specialization
    protected InternalMethod doLookup(
            Frame frame,
            Object self,
            String name,
            boolean ignoreVisibility,
            boolean onlyLookupPublic,
            @Cached MetaClassNode metaClassNode,
            @Cached LookupMethodNode lookupMethod) {
        final RubyClass metaclass = metaClassNode.executeMetaClass(self);
        return lookupMethod.execute(frame, metaclass, name, ignoreVisibility, onlyLookupPublic);
    }
}
