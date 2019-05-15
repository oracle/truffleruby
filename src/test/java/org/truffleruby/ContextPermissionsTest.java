/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import org.graalvm.polyglot.Context;
import org.junit.Assert;
import org.junit.Test;

public class ContextPermissionsTest {

    @Test
    public void testNoPermissions() throws Throwable {
        try (Context context = Context.newBuilder("ruby").build()) {
            Assert.assertEquals(3, context.eval("ruby", "1 + 2").asInt());

            String option = "Truffle::Boot.get_option('platform-native')";
            Assert.assertEquals(false, context.eval("ruby", option).asBoolean());
            option = "Truffle::Boot.get_option('single-threaded')";
            Assert.assertEquals(true, context.eval("ruby", option).asBoolean());
        }
    }

    @Test
    public void testNativeNoThreads() throws Throwable {
        // TODO (eregon, 4 Feb 2019): This should run on GraalVM, not development jars
        String home = System.getProperty("user.dir") + "/mxbuild/truffleruby-jvm/jre/languages/ruby";
        try (Context context = Context.newBuilder("ruby").allowNativeAccess(true).allowExperimentalOptions(true).option("ruby.home", home).build()) {
            Assert.assertEquals(3, context.eval("ruby", "1 + 2").asInt());

            Assert.assertEquals(true, context.eval("ruby", "File.directory?('.')").asBoolean());
        }
    }

    @Test
    public void testThreadsNoNative() throws Throwable {
        // The ruby.single_threaded option needs to be set because --single-threaded defaults to --embedded.
        try (Context context = Context.newBuilder("ruby").allowCreateThread(true).allowExperimentalOptions(true).option("ruby.single-threaded", "false").build()) {
            Assert.assertEquals(3, context.eval("ruby", "1 + 2").asInt());

            Assert.assertEquals(7, context.eval("ruby", "Thread.new { 3 + 4 }.value").asInt());

            String code = "begin; File.stat('.'); rescue SecurityError => e; e.message; end";
            Assert.assertEquals("native access is not allowed", context.eval("ruby", code).asString());
        }
    }

    @Test
    public void testNoThreadsEnforcesSingleThreadedOption() throws Throwable {
        try (Context context = Context.newBuilder("ruby").allowExperimentalOptions(true).option("ruby.single-threaded", "false").build()) {
            String option = "Truffle::Boot.get_option('single-threaded')";
            Assert.assertEquals(true, context.eval("ruby", option).asBoolean());

            String code = "begin; Thread.new {}.join; rescue SecurityError => e; e.message; end";
            Assert.assertEquals("threads not allowed in single-threaded mode", context.eval("ruby", code).asString());
        }
    }

}
