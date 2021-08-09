/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class LogicalClassNode extends RubyBaseNode {

    public static LogicalClassNode create() {
        return LogicalClassNodeGen.create();
    }

    public static LogicalClassNode getUncached() {
        return LogicalClassNodeGen.getUncached();
    }

    public abstract RubyClass execute(Object value);

    @Specialization(guards = "value")
    protected RubyClass logicalClassTrue(boolean value) {
        return coreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass logicalClassFalse(boolean value) {
        return coreLibrary().falseClass;
    }

    @Specialization
    protected RubyClass logicalClassInt(int value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass logicalClassLong(long value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass logicalClassRubyBignum(RubyBignum value) {
        return coreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass logicalClassDouble(double value) {
        return coreLibrary().floatClass;
    }

    @Specialization
    protected RubyClass logicalClassNil(Nil value) {
        return coreLibrary().nilClass;
    }

    @Specialization
    protected RubyClass logicalClassSymbol(RubySymbol value) {
        return coreLibrary().symbolClass;
    }

    @Specialization
    protected RubyClass logicalClassEncoding(RubyEncoding value) {
        return coreLibrary().encodingClass;
    }

    @Specialization
    protected RubyClass logicalImmutableString(ImmutableRubyString value) {
        return coreLibrary().stringClass;
    }

    @Specialization
    protected RubyClass logicalClassRegexp(RubyRegexp value) {
        return coreLibrary().regexpClass;
    }


    @Specialization
    protected RubyClass logicalClassObject(RubyDynamicObject object) {
        return object.getLogicalClass();
    }

    @Specialization(guards = "isForeignObject(object)")
    protected RubyClass logicalClassForeign(Object object) {
        return coreLibrary().truffleInteropForeignClass;
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.CLASS_CACHE;
    }

}
