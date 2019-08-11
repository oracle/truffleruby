/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code for parsing timezones in this class is transposed
 * from org.jruby.RubyTime and licensed under the same
 * EPL 1.0/GPL 2.0/LGPL 2.1 used throughout.
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
 */
package org.truffleruby.core.time;

import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.CyclicAssumption;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.parser.Helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GetTimeZoneNode extends RubyBaseNode {

    public static final ZoneId UTC = ZoneId.of("UTC");

    protected static final CyclicAssumption TZ_UNCHANGED = new CyclicAssumption("ENV['TZ'] is unmodified");

    public static void invalidateTZ() {
        TZ_UNCHANGED.invalidate();
    }

    @Child private CallDispatchHeadNode lookupEnvNode = CallDispatchHeadNode.createPrivate();

    public abstract TimeZoneAndName executeGetTimeZone();

    @Specialization(assumptions = "TZ_UNCHANGED.getAssumption()")
    public TimeZoneAndName getTimeZone(
            @Cached("getTZ()") Object tzValue,
            @Cached("getTimeZone(tzValue)") TimeZoneAndName zone) {
        return zone;
    }

    protected Object getTZ() {
        return lookupEnvNode.call(coreLibrary().getENV(), "[]", coreStrings().TZ.createInstance());
    }

    @TruffleBoundary
    protected TimeZoneAndName getTimeZone(Object tz) {
        String tzString = "";
        if (RubyGuards.isRubyString(tz)) {
            tzString = StringOperations.getString((DynamicObject) tz);
        }

        if (tz == nil()) {
            // $TZ is not set, use the system timezone
            return new TimeZoneAndName(getSystemTimeZone());
        } else if (tzString.equalsIgnoreCase("localtime")) {
            // On Solaris, $TZ is "localtime", so get it from Java
            return new TimeZoneAndName(getSystemTimeZone());
        } else if (RubyGuards.isRubyString(tz)) {
            return parse(tzString);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private ZoneId getSystemTimeZone() {
        if (TruffleOptions.AOT) {
            // TimeZone.getDefault() returns the image build time zone on SVM.
            final Path localtime = Paths.get("/etc/localtime");
            if (!Files.exists(localtime, LinkOption.NOFOLLOW_LINKS)) {
                RubyLanguage.LOGGER.config("could not find timezone (/etc/localtime does not exist), using UTC instead");
                return UTC;
            }

            String timeZoneID;
            try {
                if (Files.isSymbolicLink(localtime)) {
                    timeZoneID = getTimeZoneIDFromSymlink(localtime);
                } else {
                    timeZoneID = getTimeZoneIDByComparingFiles(localtime);
                }
            } catch (IOException e) {
                throw new JavaException(e);
            }

            if (timeZoneID.startsWith("posix/")) {
                timeZoneID = timeZoneID.substring("posix/".length());
            }

            return ZoneId.of(timeZoneID);
        } else {
            return ZoneId.systemDefault();
        }
    }

    private String getTimeZoneIDFromSymlink(Path localtime) throws IOException {
        final String resolved = Files.readSymbolicLink(localtime).toString();

        final int index = resolved.indexOf("zoneinfo/");
        if (index == -1) {
            RubyLanguage.LOGGER.config("could not find timezone (the /etc/localtime symlink does not contain zoneinfo/), using UTC instead");
            return "UTC";
        }

        return resolved.substring(index + "zoneinfo/".length());
    }

    private String getTimeZoneIDByComparingFiles(Path localtime) throws IOException {
        final byte[] bytes = Files.readAllBytes(localtime);

        final Path zoneinfo = Paths.get("/usr/share/zoneinfo");
        final Optional<Path> same = Files.walk(zoneinfo).filter(path -> {
            final String filename = path.getFileName().toString();
            if (filename.startsWith(".") || filename.equals("ROC") || filename.equals("posixrules") || filename.equals("localtime")) {
                return false;
            }

            return isSameFile(bytes, path);
        }).findFirst();

        if (same.isPresent()) {
            return zoneinfo.relativize(same.get()).toString();
        } else {
            if (isWSLTimeZone(zoneinfo, bytes)) {
                RubyLanguage.LOGGER.config("Windows Subsystem for Linux does not set a correct unix timezone, using UTC instead");
                RubyLanguage.LOGGER.config("running 'sudo dpkg-reconfigure tzdata' should configure a correct unix timezone");
            } else {
                RubyLanguage.LOGGER.config("could not find timezone (no file in " + zoneinfo + " is the same as /etc/localtime), using UTC instead");
            }
            return "UTC";
        }
    }

    private boolean isSameFile(byte[] bytes, Path path) {
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                return false;
            }
            final long size = Files.size(path);
            return size == bytes.length && Arrays.equals(Files.readAllBytes(path), bytes);
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    private boolean isWSLTimeZone(Path zoneinfo, byte[] bytes) {
        final Path wslTimeZone = zoneinfo.resolve("Msft/localtime");
        return isSameFile(bytes, wslTimeZone);
    }

    private static final Map<String, String> LONG_TZNAME = Helpers.map(
            "MET", "CET", // JRUBY-2759
            "ROC", "Asia/Taipei", // Republic of China
            "WET", "Europe/Lisbon" // Western European Time
    );

    private static final Pattern TZ_PATTERN = Pattern.compile("([a-zA-Z]{3,}+)([\\+-]?)(\\d+)(?::(\\d+))?(?::(\\d+))?");

    private TimeZoneAndName parse(String zone) {
        final Matcher tzMatcher = TZ_PATTERN.matcher(zone);
        if (tzMatcher.matches()) {
            String name = tzMatcher.group(1);
            String sign = tzMatcher.group(2);
            String hours = tzMatcher.group(3);
            String minutes = tzMatcher.group(4);
            String seconds = tzMatcher.group(5);

            // Sign is reversed in legacy TZ notation
            return getTimeZoneFromHHMM(name, sign.equals("-"), hours, minutes, seconds);
        } else {
            final String expandedZone = expandZoneName(zone);

            ZoneId zoneID;
            try {
                zoneID = ZoneId.of(expandedZone);
            } catch (DateTimeException | IllegalArgumentException e) {
                zoneID = UTC;
            }
            return new TimeZoneAndName(zoneID);
        }
    }

    private String expandZoneName(String zone) {
        final String upZone = zone.toUpperCase(Locale.ENGLISH);
        if (LONG_TZNAME.containsKey(upZone)) {
            return LONG_TZNAME.get(upZone);
        } else if (upZone.equals("UTC") || upZone.equals("GMT")) {
            // MRI behavior: With TZ equal to "GMT" or "UTC", Time.now
            // is *NOT* considered as a proper GMT/UTC time:
            //   ENV['TZ']="GMT"; Time.now.gmt? #=> false
            //   ENV['TZ']="UTC"; Time.now.utc? #=> false
            // Hence, we need to adjust for that.
            return "Etc/" + upZone;
        }

        return zone;
    }

    private TimeZoneAndName getTimeZoneFromHHMM(String name, boolean positive, String hours, String minutes, String seconds) {
        final int h = Integer.parseInt(hours);
        final int m = minutes != null ? Integer.parseInt(minutes) : 0;
        final int s = seconds != null ? Integer.parseInt(seconds) : 0;

        if (h > 23 || m > 59) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("utc_offset out of range", this));
        }

        final int offset = (positive ? +1 : -1) * ((h * 3600) + m * 60 + s);

        final ZoneOffset zoneOffset;
        try {
            zoneOffset = ZoneOffset.ofTotalSeconds(offset);
        } catch (DateTimeException e) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
        }

        return new TimeZoneAndName(zoneOffset, name);
    }

}
