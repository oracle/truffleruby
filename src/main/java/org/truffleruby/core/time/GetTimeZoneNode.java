/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.time;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.time.TimeNodes.TimeZoneParser;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import java.time.ZoneId;

public abstract class GetTimeZoneNode extends RubyNode {

    protected static final CyclicAssumption TZ_UNCHANGED = new CyclicAssumption("ENV['TZ'] is unmodified");

    public static void invalidateTZ() {
        TZ_UNCHANGED.invalidate();
    }

    @Child private CallDispatchHeadNode lookupEnvNode = CallDispatchHeadNode.createOnSelf();

    public abstract TimeZoneAndName executeGetTimeZone();

    @Specialization(assumptions = "TZ_UNCHANGED.getAssumption()")
    public TimeZoneAndName getTimeZone(
            @Cached("getTZ()") Object tzValue,
            @Cached("getTimeZone(tzValue)") TimeZoneAndName zone) {
        return zone;
    }

    protected Object getTZ() {
        return lookupEnvNode.call(null, coreLibrary().getENV(), "[]", coreStrings().TZ.createInstance());
    }

    @TruffleBoundary
    protected TimeZoneAndName getTimeZone(Object tz) {
        String tzString = "";
        if (RubyGuards.isRubyString(tz)) {
            tzString = StringOperations.getString((DynamicObject) tz);
        }

        // TODO CS 4-May-15 not sure how TZ ends up being nil
        if (tz == nil()) {
            return new TimeZoneAndName(ZoneId.systemDefault(), null);
        } else if (tzString.equalsIgnoreCase("localtime")) {
            // On Solaris, $TZ is "localtime", so get it from Java
            return new TimeZoneAndName(ZoneId.systemDefault(), null);
        } else if (RubyGuards.isRubyString(tz)) {
            return TimeZoneParser.parse(this, tzString);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
