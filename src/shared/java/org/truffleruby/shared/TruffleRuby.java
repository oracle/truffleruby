/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared;

import org.graalvm.nativeimage.ImageInfo;

public class TruffleRuby {

    public static final String FORMAL_NAME = "TruffleRuby";
    public static final String LANGUAGE_ID = "ruby";
    public static final String EXTENSION = ".rb";
    public static final String ENGINE_ID = "truffleruby";
    public static final String LANGUAGE_VERSION = "3.0.2";
    public static final String LANGUAGE_REVISION = BuildInformationImpl.INSTANCE.getFullRevision();
    public static final String BOOT_SOURCE_NAME = "main_boot_source";
    public static final String RUBY_COPYRIGHT = "truffleruby - Copyright (c) 2013-" +
            BuildInformationImpl.INSTANCE.getCopyrightYear() + " Oracle and/or its affiliates";

    public static String getVersionString(String implementationName) {
        final String buildName = BuildInformationImpl.INSTANCE.getBuildName();
        final String nameExtra;

        if (buildName == null) {
            nameExtra = "";
        } else {
            nameExtra = String.format(" (%s)", BuildInformationImpl.INSTANCE.getBuildName());
        }

        return String.format(
                "%s%s %s, like ruby %s, %s %s [%s-%s]",
                ENGINE_ID,
                nameExtra,
                getEngineVersion(),
                LANGUAGE_VERSION,
                implementationName,
                ImageInfo.inImageCode() ? "Native" : "JVM",
                BasicPlatform.getArchName(),
                BasicPlatform.getOSName());
    }

    public static String getEngineVersion() {
        // The property cannot be read in a static initializer, it's set later
        final String systemVersion = System.getProperty("org.graalvm.version");

        // No version information, or just "dev" - use 0.0-commit
        if (systemVersion == null || systemVersion.equals("dev")) {
            return "0.0-" + BuildInformationImpl.INSTANCE.getShortRevision();
        }

        // A "-dev" version number - append the commit as well
        if (systemVersion.endsWith("-dev")) {
            return systemVersion + "-" + BuildInformationImpl.INSTANCE.getShortRevision();
        }

        return systemVersion;
    }

}
