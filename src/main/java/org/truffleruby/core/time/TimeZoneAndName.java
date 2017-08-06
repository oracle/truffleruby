/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.time;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Locale;

public class TimeZoneAndName {

    private static final DateTimeFormatter SHORT_ZONE_NAME_FORMATTER =
            new DateTimeFormatterBuilder().appendZoneText(TextStyle.SHORT).toFormatter(Locale.ENGLISH);

    private final ZoneId zone;

    /**
     * The short name optionally captured from the $TZ environment variable such as PST if TZ=PST8:00:00.
     * If $TZ is like America/New_York, the short zone name is computed later in getName().
     */
    private final String name;

    public TimeZoneAndName(ZoneId zone, String name) {
        // A name is given if and only if the ZoneId is just an offset
        assert name != null == zone instanceof ZoneOffset;
        this.zone = zone;
        this.name = name;
    }

    public ZoneId getZone() {
        return zone;
    }

    @TruffleBoundary
    public String getName(ZonedDateTime dateTime) {
        if (name != null) {
            return name;
        } else {
            return dateTime.format(SHORT_ZONE_NAME_FORMATTER);
        }
    }

    public DynamicObject getNameAsRubyObject(RubyContext context) {
        if (name == null) {
            return context.getCoreLibrary().getNil();
        } else {
            return StringOperations.createString(context, context.getRopeTable().getRope(name));
        }
    }
}
