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

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;

import java.time.ZoneId;

public class TimeZoneAndName {

    private final ZoneId zone;

    /**
     * The short name optionally captured from the $TZ environment variable such as PST if TZ=PST8:00:00.
     * If $TZ is like America/New_York, the short zone name is computed later by TimeZoneParser.getShortZoneName.
     */
    private final String name;

    public TimeZoneAndName(ZoneId zone, String name) {
        this.zone = zone;
        this.name = name;
    }

    public ZoneId getZone() {
        return zone;
    }

    public String getName() {
        return name;
    }

    public DynamicObject getNameAsRubyObject(RubyContext context) {
        if (name == null) {
            return context.getCoreLibrary().getNilObject();
        } else {
            return StringOperations.createString(context, context.getRopeTable().getRope(name));
        }
    }
}
