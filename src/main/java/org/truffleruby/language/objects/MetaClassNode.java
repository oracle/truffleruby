/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.range.RubyIntOrLongRange;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NoImplicitCastsToLong;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@TypeSystemReference(NoImplicitCastsToLong.class)
public abstract class MetaClassNode extends RubyBaseNode {

    @NeverDefault
    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public static MetaClassNode getUncached() {
        return MetaClassNodeGen.getUncached();
    }

    public final RubyClass execute(Object value) {
        return execute(value, coreLibrary());
    }

    protected abstract RubyClass execute(Object value, CoreLibrary coreLibrary);

    // Cover all primitives, nil and symbols

    @Specialization(guards = "value")
    protected RubyClass metaClassTrue(boolean value, CoreLibrary coreLibrary) {
        return coreLibrary.trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass metaClassFalse(boolean value, CoreLibrary coreLibrary) {
        return coreLibrary.falseClass;
    }

    @Specialization
    protected RubyClass metaClassInt(int value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

    @Specialization
    protected RubyClass metaClassLong(long value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

    @Specialization
    protected RubyClass metaClassBignum(RubyBignum value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

    @Specialization
    protected RubyClass metaClassDouble(double value, CoreLibrary coreLibrary) {
        return coreLibrary.floatClass;
    }

    @Specialization
    protected RubyClass metaClassNil(Nil value, CoreLibrary coreLibrary) {
        return coreLibrary.nilClass;
    }

    @Specialization
    protected RubyClass metaClassSymbol(RubySymbol value, CoreLibrary coreLibrary) {
        return coreLibrary.symbolClass;
    }

    @Specialization
    protected RubyClass metaClassEncoding(RubyEncoding value, CoreLibrary coreLibrary) {
        return coreLibrary.encodingClass;
    }

    @Specialization
    protected RubyClass metaClassImmutableString(ImmutableRubyString value, CoreLibrary coreLibrary) {
        return coreLibrary.stringClass;
    }

    @Specialization
    protected RubyClass metaClassRegexp(RubyRegexp value, CoreLibrary coreLibrary) {
        return coreLibrary.regexpClass;
    }

    @Specialization
    protected RubyClass metaClassIntRange(RubyIntOrLongRange value, CoreLibrary coreLibrary) {
        return coreLibrary.rangeClass;
    }

    // Cover all RubyDynamicObject cases with cached and uncached

    @Specialization(
            guards = { "object == cachedObject", "metaClass.isSingleton" },
            limit = "getIdentityCacheContextLimit()")
    protected RubyClass singletonClassCached(RubyDynamicObject object, CoreLibrary coreLibrary,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("object.getMetaClass()") RubyClass metaClass) {
        return metaClass;
    }

    @Specialization(replaces = "singletonClassCached")
    protected RubyClass metaClassObject(RubyDynamicObject object, CoreLibrary coreLibrary) {
        return object.getMetaClass();
    }

    // Foreign object
    @InliningCutoff
    @Specialization(guards = "isForeignObject(object)")
    protected RubyClass metaClassForeign(Object object, CoreLibrary coreLibrary,
            @Cached ForeignClassNode foreignClassNode) {
        return foreignClassNode.execute(object);
    }

    protected int getCacheLimit() {
        return getLanguage().options.CLASS_CACHE;
    }
}
