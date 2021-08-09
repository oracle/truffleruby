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
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;

import java.math.BigInteger;

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
        // Checkstyle: stop
        loopProfile.profileCounted(count);
        LoopNode.reportLoopCount(node, count);
        // Checkstyle: resume
    }

    protected void profileAndReportLoopCount(LoopConditionProfile loopProfile, int count) {
        // Checkstyle: stop
        loopProfile.profileCounted(count);
        LoopNode.reportLoopCount(this, count);
        // Checkstyle: resume
    }

    protected void profileAndReportLoopCount(LoopConditionProfile loopProfile, long count) {
        // Checkstyle: stop
        loopProfile.profileCounted(count);
        reportLongLoopCount(count);
        // Checkstyle: resume
    }

    protected void reportLongLoopCount(long count) {
        assert count >= 0L;
        // Checkstyle: stop
        LoopNode.reportLoopCount(this, count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count);
        // Checkstyle: resume
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

    public final RubyLanguage getLanguage() {
        return RubyLanguage.get(this);
    }

    public final RubyContext getContext() {
        return RubyContext.get(this);
    }

    // Helpers methods for terseness. They are `final` so we ensure they are not needlessly redeclared in subclasses.

    protected final RubyEncoding getLocaleEncoding() {
        return getContext().getEncodingManager().getLocaleEncoding();
    }

    protected final RubyBignum createBignum(BigInteger value) {
        return BignumOperations.createBignum(value);
    }

    protected final RubySymbol getSymbol(String name) {
        return getLanguage().getSymbol(name);
    }

    protected final RubySymbol getSymbol(Rope name, RubyEncoding encoding) {
        return getLanguage().getSymbol(name, encoding);
    }

    protected final CoreStrings coreStrings() {
        return getLanguage().coreStrings;
    }

    protected final CoreSymbols coreSymbols() {
        return getLanguage().coreSymbols;
    }

    protected final RubyArray createArray(Object store, int size) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store, size);
    }

    protected final RubyArray createArray(int[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    protected final RubyArray createArray(long[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    protected final RubyArray createArray(Object[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    protected final RubyArray createEmptyArray() {
        return ArrayHelpers.createEmptyArray(getContext(), getLanguage());
    }

    protected final CoreLibrary coreLibrary() {
        return getContext().getCoreLibrary();
    }

    protected final CoreExceptions coreExceptions() {
        return getContext().getCoreExceptions();
    }

    protected final int getIdentityCacheLimit() {
        return getLanguage().options.IDENTITY_CACHE;
    }

    protected final int getDefaultCacheLimit() {
        return getLanguage().options.DEFAULT_CACHE;
    }

    protected final int getDynamicObjectCacheLimit() {
        return getLanguage().options.INSTANCE_VARIABLE_CACHE;
    }

    protected final int getInteropCacheLimit() {
        return getLanguage().options.METHOD_LOOKUP_CACHE;
    }

    protected final int getRubyLibraryCacheLimit() {
        return getLanguage().options.RUBY_LIBRARY_CACHE;
    }
}
