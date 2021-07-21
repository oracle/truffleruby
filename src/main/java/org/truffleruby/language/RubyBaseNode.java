/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;

/** Base of all Ruby nodes */
@TypeSystemReference(RubyTypes.class)
@ImportStatic(RubyGuards.class)
public abstract class RubyBaseNode extends Node {

    public static final Object[] EMPTY_ARGUMENTS = ArrayUtils.EMPTY_ARRAY;

    public static final Nil nil = Nil.INSTANCE;

    public static final int MAX_EXPLODE_SIZE = 16;

    public static boolean isSingleContext() {
        return RubyLanguage.getCurrentLanguage().singleContext;
    }

    public static Object nilToNull(Object value) {
        return value == nil ? null : value;
    }

    public static Object nullToNil(Object value) {
        return value == null ? nil : value;
    }

    /** Variants for {@link com.oracle.truffle.api.library.Library}. The node argument should typically be
     * {@code node.getNode()} with {@code @CachedLibrary("this") ArrayStoreLibrary node} */
    public static void profileAndReportLoopCount(Node node, LoopConditionProfile loopProfile, int count) {
        loopProfile.profileCounted(count);
        LoopNode.reportLoopCount(node, count);
    }

    public void profileAndReportLoopCount(LoopConditionProfile loopProfile, int count) {
        loopProfile.profileCounted(count);
        LoopNode.reportLoopCount(this, count);
    }

    public void profileAndReportLoopCount(LoopConditionProfile loopProfile, long count) {
        loopProfile.profileCounted(count);
        reportLongLoopCount(count);
    }

    protected void reportLongLoopCount(long count) {
        assert count >= 0L;
        LoopNode.reportLoopCount(this, count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count);
    }

    protected Node getNode() {
        boolean adoptable = this.isAdoptable();
        CompilerAsserts.partialEvaluationConstant(adoptable);
        if (adoptable) {
            return this;
        } else {
            return EncapsulatingNodeReference.getCurrent().get();
        }
    }

    // Prefixed with "base" so as not to conflict with RubyNode.WithContext#getRubyLibraryCacheLimit
    public int getRubyLibraryCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.RUBY_LIBRARY_CACHE;
    }
}
