/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.parser.Helpers;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (tz == nil()) {
            // $TZ is not set, use the system timezone
            return new TimeZoneAndName(ZoneId.systemDefault(), null);
        } else if (tzString.equalsIgnoreCase("localtime")) {
            // On Solaris, $TZ is "localtime", so get it from Java
            return new TimeZoneAndName(ZoneId.systemDefault(), null);
        } else if (RubyGuards.isRubyString(tz)) {
            return parse(tzString);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    // The following private methods in this class were copied over from org.jruby.RubyTime.
    // Slight modifications were made.

    /* Version: EPL 1.0/GPL 2.0/LGPL 2.1
     *
     * The contents of this file are subject to the Eclipse Public
     * License Version 1.0 (the "License"); you may not use this file
     * except in compliance with the License. You may obtain a copy of
     * the License at http://www.eclipse.org/legal/epl-v10.html
     *
     * Software distributed under the License is distributed on an "AS
     * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
     * implied. See the License for the specific language governing
     * rights and limitations under the License.
     *
     * Copyright (C) 2001 Chad Fowler <chadfowler@chadfowler.com>
     * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
     * Copyright (C) 2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
     * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
     * Copyright (C) 2004 Joey Gibson <joey@joeygibson.com>
     * Copyright (C) 2004 Charles O Nutter <headius@headius.com>
     * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
     * Copyright (C) 2006 Thomas E Enebo <enebo@acm.org>
     * Copyright (C) 2006 Ola Bini <ola.bini@ki.se>
     * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
     * Copyright (C) 2009 Joseph LaFata <joe@quibb.org>
     *
     * Alternatively, the contents of this file may be used under the terms of
     * either of the GNU General Public License Version 2 or later (the "GPL"),
     * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
     * in which case the provisions of the GPL or the LGPL are applicable instead
     * of those above. If you wish to allow use of your version of this file only
     * under the terms of either the GPL or the LGPL, and not to allow others to
     * use your version of this file under the terms of the EPL, indicate your
     * decision by deleting the provisions above and replace them with the notice
     * and other provisions required by the GPL or the LGPL. If you do not delete
     * the provisions above, a recipient may use your version of this file under
     * the terms of any one of the EPL, the GPL or the LGPL.
     */

    private static final Map<String, String> LONG_TZNAME = Helpers.map(
            "MET", "CET", // JRUBY-2759
            "ROC", "Asia/Taipei", // Republic of China
            "WET", "Europe/Lisbon" // Western European Time
    );

    private static final Pattern TZ_PATTERN =
            Pattern.compile("([a-zA-Z]{3,}+)([\\+-]?)(\\d+)(?::(\\d+))?(?::(\\d+))?");

    private TimeZoneAndName parse(String zoneString) {
        String zone = zoneString;

        Matcher tzMatcher = TZ_PATTERN.matcher(zone);
        if (tzMatcher.matches()) {
            String name = tzMatcher.group(1);
            String sign = tzMatcher.group(2);
            String hours = tzMatcher.group(3);
            String minutes = tzMatcher.group(4);
            String seconds = tzMatcher.group(5);

            // Sign is reversed in legacy TZ notation
            return getTimeZoneFromHHMM(name, sign.equals("-"), hours, minutes, seconds);
        } else {
            final String upZone = zone.toUpperCase(Locale.ENGLISH);
            if (LONG_TZNAME.containsKey(upZone)) {
                zone = LONG_TZNAME.get(upZone);
            } else if (upZone.equals("UTC") || upZone.equals("GMT")) {
                // MRI behavior: With TZ equal to "GMT" or "UTC", Time.now
                // is *NOT* considered as a proper GMT/UTC time:
                //   ENV['TZ']="GMT"; Time.now.gmt? #=> false
                //   ENV['TZ']="UTC"; Time.now.utc? #=> false
                // Hence, we need to adjust for that.
                zone = "Etc/" + upZone;
            }

            try {
                return new TimeZoneAndName(ZoneId.of(zone), null);
            } catch (DateTimeException | IllegalArgumentException e) {
                return new TimeZoneAndName(TimeNodes.UTC, null);
            }
        }
    }

    private TimeZoneAndName getTimeZoneFromHHMM(String name, boolean positive, String hours, String minutes, String seconds) {
        int h = Integer.parseInt(hours);
        int m = 0;
        int s = 0;
        if (minutes != null) {
            m = Integer.parseInt(minutes);
        }

        if (seconds != null) {
            s = Integer.parseInt(seconds);
        }

        if (h > 23 || m > 59) {
            throw new RaiseException(coreExceptions().argumentError("utc_offset out of range", this));
        }

        int offset = (positive ? +1 : -1) * ((h * 3600) + m * 60 + s) * 1000;
        return timeZoneWithOffset(name, offset);
    }

    private TimeZoneAndName timeZoneWithOffset(String zoneName, int offset) {
        final ZoneId zone;
        try {
            zone = ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(offset / 1000));
        } catch (DateTimeException e) {
            throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this));
        }

        return new TimeZoneAndName(zone, zoneName);
    }

}
