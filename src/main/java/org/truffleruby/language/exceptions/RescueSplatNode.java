/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class RescueSplatNode extends RescueNode {

    @Child private SplatCastNode splatCastNode;
    @Child private ArrayStoreLibrary stores;
    private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

    public RescueSplatNode(RubyLanguage language, RubyNode handlingClassesArray, RubyNode rescueBody) {
        super(rescueBody);
        this.splatCastNode = SplatCastNodeGen.create(
                language,
                SplatCastNode.NilBehavior.EMPTY_ARRAY,
                false,
                handlingClassesArray);
        this.splatCastNode.doNotCopy();
        this.stores = ArrayStoreLibrary.createDispatched(language);
    }

    @Override
    public boolean canHandle(VirtualFrame frame, Object exceptionObject) {
        final RubyArray handlingClasses = (RubyArray) splatCastNode.execute(frame);

        int i = 0;
        try {
            for (; loopProfile.inject(i < handlingClasses.size); ++i) {
                if (matches(exceptionObject, stores.read(handlingClasses.getStore(), i))) {
                    return true;
                }
                TruffleSafepoint.poll(this);
            }
        } finally {
            profileAndReportLoopCount(loopProfile, i);
        }

        return false;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return RubyNode.defaultIsDefined(this);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new RescueSplatNode(
                getLanguage(),
                splatCastNode.getChildNode().cloneUninitialized(),
                getRescueBody().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
