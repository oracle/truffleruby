/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.time;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.time.RubyDateFormatter.Token;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.parser.Helpers;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CoreClass("Time")
public abstract class TimeNodes {

    private static final ZonedDateTime ZERO = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static DynamicObject getShortZoneName(StringNodes.MakeStringNode makeStringNode, ZonedDateTime dt, TimeZoneAndName zoneAndName) {
        final String shortZoneName = zoneAndName.getName(dt);
        return makeStringNode.executeMake(shortZoneName, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, Layouts.TIME.build(ZERO, nil(), 0, false, false));
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyTime(from)")
        public Object initializeCopy(DynamicObject self, DynamicObject from) {
            Layouts.TIME.setDateTime(self, Layouts.TIME.getDateTime(from));
            Layouts.TIME.setOffset(self, Layouts.TIME.getOffset(from));
            Layouts.TIME.setZone(self, Layouts.TIME.getZone(from));
            Layouts.TIME.setRelativeOffset(self, Layouts.TIME.getRelativeOffset(from));
            Layouts.TIME.setIsUtc(self, Layouts.TIME.getIsUtc(from));
            return self;
        }

    }

    @Primitive(name = "time_localtime")
    public abstract static class LocalTimeNode extends PrimitiveArrayArgumentsNode {

        @Child private GetTimeZoneNode getTimeZoneNode = GetTimeZoneNodeGen.create();

        @Specialization(guards = "isNil(offset)")
        public DynamicObject localtime(DynamicObject time, DynamicObject offset,
                                       @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
            final TimeZoneAndName timeZoneAndName = getTimeZoneNode.executeGetTimeZone();
            final ZonedDateTime newDateTime = withZone(Layouts.TIME.getDateTime(time), timeZoneAndName.getZone());
            final DynamicObject zone = getShortZoneName(makeStringNode, newDateTime, timeZoneAndName);

            Layouts.TIME.setIsUtc(time, false);
            Layouts.TIME.setRelativeOffset(time, false);
            Layouts.TIME.setZone(time, zone);
            Layouts.TIME.setDateTime(time, newDateTime);

            return time;
        }

        @Specialization
        public DynamicObject localtime(DynamicObject time, long offset) {
            final ZoneId zone = getDateTimeZone((int) offset);
            final ZonedDateTime dateTime = withZone(Layouts.TIME.getDateTime(time), zone);

            Layouts.TIME.setIsUtc(time, false);
            Layouts.TIME.setRelativeOffset(time, true);
            Layouts.TIME.setZone(time, nil());
            Layouts.TIME.setDateTime(time, dateTime);

            return time;
        }

        @TruffleBoundary
        public ZoneId getDateTimeZone(int offset) {
            try {
                return ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(offset));
            } catch (DateTimeException e) {
                throw new RaiseException(getContext().getCoreExceptions().argumentError(e.getMessage(), this));
            }
        }

        @TruffleBoundary
        private ZonedDateTime withZone(ZonedDateTime dateTime, ZoneId zone) {
            return dateTime.withZoneSameInstant(zone);
        }

    }

    @Primitive(name = "time_add")
    public abstract static class TimeAddNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject add(DynamicObject time, long seconds, long nanoSeconds) {
            final ZonedDateTime dateTime = Layouts.TIME.getDateTime(time);
            Layouts.TIME.setDateTime(time, dateTime.plusSeconds(seconds).plusNanos(nanoSeconds));
            return time;
        }
    }

    @CoreMethod(names = { "gmtime", "utc" })
    public abstract static class GmTimeNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject gmtime(DynamicObject time) {
            final ZonedDateTime dateTime = Layouts.TIME.getDateTime(time);

            Layouts.TIME.setIsUtc(time, true);
            Layouts.TIME.setRelativeOffset(time, false);
            Layouts.TIME.setZone(time, makeStringNode.executeMake(getUTCDisplayName(), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT));
            Layouts.TIME.setDateTime(time, inUTC(dateTime));

            return time;
        }

        @TruffleBoundary
        private String getUTCDisplayName() {
            return UTC.getDisplayName(TextStyle.NARROW, Locale.ENGLISH);
        }

        @TruffleBoundary
        private ZonedDateTime inUTC(final ZonedDateTime dateTime) {
            return dateTime.withZoneSameInstant(UTC);
        }

    }

    @CoreMethod(names = "now", constructor = true)
    public static abstract class TimeNowNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private GetTimeZoneNode getTimeZoneNode = GetTimeZoneNodeGen.create();
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject timeNow(DynamicObject timeClass) {
            final TimeZoneAndName zoneAndName = getTimeZoneNode.executeGetTimeZone();
            final ZonedDateTime dt = now(zoneAndName.getZone());
            final DynamicObject zone = getShortZoneName(makeStringNode, dt, zoneAndName);
            return allocateObjectNode.allocate(timeClass, Layouts.TIME.build(dt, zone, nil(), false, false));
        }

        @TruffleBoundary
        private ZonedDateTime now(ZoneId timeZone) {
            return ZonedDateTime.now(timeZone);
        }

    }

    @Primitive(name = "time_at", lowerFixnum = 2)
    public static abstract class TimeAtPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private GetTimeZoneNode getTimeZoneNode = GetTimeZoneNodeGen.create();
        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject timeAt(DynamicObject timeClass, long seconds, int nanoseconds) {
            final TimeZoneAndName zoneAndName = getTimeZoneNode.executeGetTimeZone();
            final ZonedDateTime dateTime = getDateTime(seconds, nanoseconds, zoneAndName.getZone());
            final DynamicObject zone = getShortZoneName(makeStringNode, dateTime, zoneAndName);
            return allocateObjectNode.allocate(timeClass, Layouts.TIME.build(
                    dateTime, zone, nil(), false, false));
        }

        @TruffleBoundary
        private ZonedDateTime getDateTime(long seconds, int nanoseconds, ZoneId timeZone) {
            try {
                return ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoseconds), timeZone);
            } catch (DateTimeException e) {
                String message = StringUtils.format("UNIX epoch + %d seconds out of range for Time (java.time limitation)", seconds);
                throw new RaiseException(coreExceptions().rangeError(message, this));
            }
        }

    }

    @CoreMethod(names = { "to_i", "tv_sec" })
    public static abstract class TimeSecondsSinceEpochNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long timeSeconds(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).toInstant().getEpochSecond();
        }

    }

    @CoreMethod(names = { "usec", "tv_usec" })
    public static abstract class TimeMicroSecondsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeUSec(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getNano() / 1000;
        }

    }

    @CoreMethod(names = { "nsec", "tv_nsec" })
    public static abstract class TimeNanoSecondsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeNSec(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getNano();
        }

    }

    @Primitive(name = "time_set_nseconds", lowerFixnum = 1)
    public static abstract class TimeSetNSecondsPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long timeSetNSeconds(DynamicObject time, int nanoseconds) {
            final ZonedDateTime dateTime = Layouts.TIME.getDateTime(time);
            Layouts.TIME.setDateTime(time, dateTime.plusNanos(nanoseconds - dateTime.getNano()));
            return nanoseconds;
        }

    }

    @CoreMethod(names = { "utc_offset", "gmt_offset", "gmtoff" })
    public static abstract class TimeUTCOffsetNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeUTCOffset(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getOffset().getTotalSeconds();
        }
    }

    @CoreMethod(names = "sec")
    public static abstract class TimeSecNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeSec(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getSecond();
        }

    }

    @CoreMethod(names = "min")
    public static abstract class TimeMinNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeMin(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getMinute();
        }

    }

    @CoreMethod(names = "hour")
    public static abstract class TimeHourNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeHour(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getHour();
        }

    }

    @CoreMethod(names = { "day", "mday" })
    public static abstract class TimeDayNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeDay(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getDayOfMonth();
        }

    }

    @CoreMethod(names = { "mon", "month" })
    public static abstract class TimeMonthNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeMonth(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getMonthValue();
        }

    }

    @CoreMethod(names = "year")
    public static abstract class TimeYearNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeYear(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getYear();
        }

    }

    @CoreMethod(names = "wday")
    public static abstract class TimeWeekDayNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeWeekDay(DynamicObject time) {
            int wday = Layouts.TIME.getDateTime(time).getDayOfWeek().getValue();
            if (wday == 7) {
                wday = 0;
            }
            return wday;
        }

    }

    @CoreMethod(names = "yday")
    public static abstract class TimeYearDayNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int timeYeayDay(DynamicObject time) {
            return Layouts.TIME.getDateTime(time).getDayOfYear();
        }

    }

    @CoreMethod(names = { "dst?", "isdst" })
    public static abstract class TimeIsDSTNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public boolean timeIsDST(DynamicObject time) {
            final ZonedDateTime dateTime = Layouts.TIME.getDateTime(time);
            return dateTime.getZone().getRules().isDaylightSavings(dateTime.toInstant());
        }

    }

    @CoreMethod(names = { "utc?", "gmt?" })
    public abstract static class IsUTCNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isUTC(DynamicObject time) {
            return Layouts.TIME.getIsUtc(time);
        }

    }

    @Primitive(name = "time_zone")
    public static abstract class TimeZoneNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object timeZone(DynamicObject time) {
            return Layouts.TIME.getZone(time);
        }

    }

    @Primitive(name = "time_strftime")
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    public static abstract class TimeStrftimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization(guards = { "isRubyString(format)",
                "equalNode.execute(rope(format), cachedFormat)" }, limit = "getContext().getOptions().TIME_FORMAT_CACHE")
        public DynamicObject timeStrftime(VirtualFrame frame, DynamicObject time, DynamicObject format,
                                          @Cached("privatizeRope(format)") Rope cachedFormat,
                @Cached("compilePattern(cachedFormat)") List<Token> pattern,
                @Cached("create()") RopeNodes.EqualNode equalNode) {
            return makeStringNode.fromRope(formatTime(time, pattern));
        }

        @Specialization(guards = "isRubyString(format)")
        public DynamicObject timeStrftime(VirtualFrame frame, DynamicObject time, DynamicObject format) {
            final List<Token> pattern = compilePattern(StringOperations.rope(format));
            return makeStringNode.fromRope(formatTime(time, pattern));
        }

        @TruffleBoundary
        protected List<Token> compilePattern(Rope format) {
            return RubyDateFormatter.compilePattern(format, false, getContext(), this);
        }

        private Rope formatTime(DynamicObject time, List<Token> pattern) {
            return RubyDateFormatter.formatToRopeBuilder(
                    pattern, Layouts.TIME.getDateTime(time), Layouts.TIME.getZone(time), getContext(), this).toRope(CodeRange.CR_UNKNOWN);
        }

    }

    @Primitive(name = "time_s_from_array", needsSelf = true, lowerFixnum = { 1 /*sec*/, 2 /* min */, 3 /* hour */, 4 /* mday */, 5 /* month */, 6 /* year */, 7 /*nsec*/, 8 /*isdst*/})
    public static abstract class TimeSFromArrayPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private GetTimeZoneNode getTimeZoneNode = GetTimeZoneNodeGen.create();
        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private StringNodes.MakeStringNode makeStringNode;

        @Specialization(guards = "(isutc || !isDynamicObject(utcoffset)) || isNil(utcoffset)")
        public DynamicObject timeSFromArray(DynamicObject timeClass,
                int sec, int min, int hour, int mday, int month, int year,
                int nsec, int isdst, boolean isutc, Object utcoffset) {
            return buildTime(timeClass, sec, min, hour, mday, month, year, nsec, isdst, isutc, utcoffset);
        }

        @Specialization(guards = "!isInteger(sec) || !isInteger(nsec)")
        public Object timeSFromArrayFallback(DynamicObject timeClass,
                Object sec, int min, int hour, int mday, int month, int year,
                Object nsec, int isdst, boolean fromutc, Object utcoffset) {
            return FAILURE;
        }

        @TruffleBoundary
        private DynamicObject buildTime(DynamicObject timeClass, int sec, int min, int hour, int mday, int month, int year,
                int nsec, int isdst, boolean isutc, Object utcoffset) {
            if (sec < 0 || sec > 60 ||
                    min < 0 || min > 59 ||
                    hour < 0 || hour > 23 ||
                    mday < 1 || mday > 31 ||
                    month < 1 || month > 12) {
                throw new RaiseException(coreExceptions().argumentErrorOutOfRange(this));
            }

            final ZoneId zone;
            final boolean relativeOffset;
            DynamicObject zoneToStore = nil();
            TimeZoneAndName envZone = null;

            if (isutc) {
                zone = UTC;
                relativeOffset = false;
                zoneToStore = coreStrings().UTC.createInstance();
            } else if (utcoffset == nil()) {
                if (makeStringNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    makeStringNode = insert(StringNodes.MakeStringNode.create());
                }

                envZone = getTimeZoneNode.executeGetTimeZone();
                zone = envZone.getZone();
                relativeOffset = false;
            } else if (utcoffset instanceof Integer || utcoffset instanceof Long) {
                final int offset = ((Number) utcoffset).intValue();
                zone = getZoneOffset(offset);
                relativeOffset = true;
                zoneToStore = nil();
            } else {
                throw new UnsupportedOperationException(StringUtils.format("%s %s %s %s", isdst, isutc, utcoffset, utcoffset.getClass()));
            }

            ZonedDateTime dt;
            try {
                dt = ZonedDateTime.of(year, month, mday, hour, min, sec, nsec, zone);
            } catch (DateTimeException e) {
                // Time.new(1999, 2, 31) is legal and should return 1999-03-03
                dt = ZonedDateTime.of(year, 1, 1, hour, min, sec, nsec, zone).plusMonths(month - 1).plusDays(mday - 1);
            }

            if (isdst == 0) {
                dt = dt.withLaterOffsetAtOverlap();
            } else if (isdst == 1) {
                dt = dt.withEarlierOffsetAtOverlap();
            }

            if (envZone != null) {
                zoneToStore = getShortZoneName(makeStringNode, dt, envZone);
            }

            return allocateObjectNode.allocate(timeClass,
                    Layouts.TIME.build(dt, zoneToStore, utcoffset, relativeOffset, isutc));
        }

        private ZoneOffset getZoneOffset(int offset) {
            try {
                return ZoneOffset.ofTotalSeconds(offset);
            } catch (DateTimeException e) {
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this));
            }
        }

    }

    public static class TimeZoneParser {
        // Following private methods in this class were copied over from org.jruby.RubyTime.
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

        @TruffleBoundary(transferToInterpreterOnException = false)
        public static TimeZoneAndName parse(RubyNode node, String zoneString) {
            String zone = zoneString;

            Matcher tzMatcher = TZ_PATTERN.matcher(zone);
            if (tzMatcher.matches()) {
                String name = tzMatcher.group(1);
                String sign = tzMatcher.group(2);
                String hours = tzMatcher.group(3);
                String minutes = tzMatcher.group(4);
                String seconds = tzMatcher.group(5);

                if (name == null) {
                    name = "";
                }

                // Sign is reversed in legacy TZ notation
                return getTimeZoneFromHHMM(node, name, sign.equals("-"), hours, minutes, seconds);
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
                    return new TimeZoneAndName(UTC, null);
                }
            }
        }

        private static TimeZoneAndName getTimeZoneFromHHMM(RubyNode node,
                                                        String name,
                                                        boolean positive,
                                                        String hours,
                                                        String minutes,
                                                        String seconds) {
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
                throw new RaiseException(node.getContext().getCoreExceptions().argumentError("utc_offset out of range", node));
            }

            int offset = (positive ? +1 : -1) * ((h * 3600) + m * 60 + s) * 1000;
            return timeZoneWithOffset(node, name, offset);
        }

        private static TimeZoneAndName timeZoneWithOffset(RubyNode node, String zoneName, int offset) {
            final ZoneId zone;

            try {
                zone = ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(offset / 1000));
            } catch (DateTimeException e) {
                throw new RaiseException(node.getContext().getCoreExceptions().argumentError(e.getMessage(), node));
            }

            if (zoneName.isEmpty()) {
                return new TimeZoneAndName(zone, null);
            } else {
                return new TimeZoneAndName(zone, zoneName);
            }
        }

    }

}
