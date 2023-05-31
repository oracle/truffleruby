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

import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.range.RubyIntOrLongRange;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NoImplicitCastsToLong;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;

// Specializations are order by their frequency on railsbench using --engine.SpecializationStatistics
@GenerateInline
@GenerateCached(false)
@GenerateUncached
@TypeSystemReference(NoImplicitCastsToLong.class)
public abstract class ImmutableClassNode extends RubyBaseNode {

    public final RubyClass execute(Node node, Object value) {
        return execute(node, value, coreLibrary(node));
    }

    protected abstract RubyClass execute(Node node, Object value, CoreLibrary coreLibrary);

    @Specialization
    protected static RubyClass metaClassInt(int value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

    @Specialization
    protected static RubyClass metaClassImmutableString(ImmutableRubyString value, CoreLibrary coreLibrary) {
        return coreLibrary.stringClass;
    }

    @Specialization
    protected static RubyClass metaClassSymbol(RubySymbol value, CoreLibrary coreLibrary) {
        return coreLibrary.symbolClass;
    }

    @Specialization
    protected static RubyClass metaClassNil(Nil value, CoreLibrary coreLibrary) {
        return coreLibrary.nilClass;
    }

    @Specialization(guards = "value")
    protected static RubyClass metaClassTrue(boolean value, CoreLibrary coreLibrary) {
        return coreLibrary.trueClass;
    }

    @Specialization(guards = "!value")
    protected static RubyClass metaClassFalse(boolean value, CoreLibrary coreLibrary) {
        return coreLibrary.falseClass;
    }

    @Specialization
    protected static RubyClass metaClassLong(long value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

    @Specialization
    protected static RubyClass metaClassEncoding(RubyEncoding value, CoreLibrary coreLibrary) {
        return coreLibrary.encodingClass;
    }

    @Specialization
    protected static RubyClass metaClassRegexp(RubyRegexp value, CoreLibrary coreLibrary) {
        return coreLibrary.regexpClass;
    }

    @Specialization
    protected static RubyClass metaClassRange(RubyIntOrLongRange value, CoreLibrary coreLibrary) {
        return coreLibrary.rangeClass;
    }

    @Specialization
    protected static RubyClass metaClassDouble(double value, CoreLibrary coreLibrary) {
        return coreLibrary.floatClass;
    }

    @Specialization
    protected static RubyClass metaClassBignum(RubyBignum value, CoreLibrary coreLibrary) {
        return coreLibrary.integerClass;
    }

}
