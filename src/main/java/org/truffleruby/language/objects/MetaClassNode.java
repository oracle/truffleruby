/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class MetaClassNode extends RubyBaseNode {

    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public static MetaClassNode getUncached() {
        return MetaClassNodeGen.getUncached();
    }

    public abstract RubyClass execute(Object value);

    // Cover all primitives, nil and symbols

    @Specialization(guards = "value")
    protected RubyClass metaClassTrue(boolean value) {
        return coreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass metaClassFalse(boolean value) {
        return coreLibrary().falseClass;
    }

    @Specialization
    protected RubyClass metaClassInt(int value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass metaClassLong(long value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass metaClassBignum(RubyBignum value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass metaClassDouble(double value) {
        return coreLibrary().floatClass;
    }

    @Specialization
    protected RubyClass metaClassNil(Nil value) {
        return coreLibrary().nilClass;
    }

    @Specialization
    protected RubyClass metaClassSymbol(RubySymbol value) {
        return coreLibrary().symbolClass;
    }

    @Specialization
    protected RubyClass metaClassEncoding(RubyEncoding value) {
        return coreLibrary().encodingClass;
    }

    @Specialization
    protected RubyClass metaClassImmutableString(ImmutableRubyString value) {
        return coreLibrary().stringClass;
    }

    @Specialization
    protected RubyClass metaClassRegexp(RubyRegexp value) {
        return coreLibrary().regexpClass;
    }


    // Cover all RubyDynamicObject cases with cached and uncached

    @Specialization(
            guards = { "object == cachedObject", "metaClass.isSingleton" },
            limit = "getIdentityCacheContextLimit()")
    protected RubyClass singletonClassCached(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("object.getMetaClass()") RubyClass metaClass) {
        return metaClass;
    }

    @Specialization(replaces = "singletonClassCached")
    protected RubyClass metaClassObject(RubyDynamicObject object) {
        return object.getMetaClass();
    }

    // Foreign object
    @Specialization(guards = "isForeignObject(object)")
    protected RubyClass metaClassForeign(Object object,
            @Cached ForeignClassNode foreignClassNode) {
        return foreignClassNode.execute(object);
    }

    protected int getCacheLimit() {
        return getLanguage().options.CLASS_CACHE;
    }

    protected int getIdentityCacheContextLimit() {
        return getLanguage().options.CONTEXT_SPECIFIC_IDENTITY_CACHE;
    }
}
