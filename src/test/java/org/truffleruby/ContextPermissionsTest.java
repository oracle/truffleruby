/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
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

            String option = "Truffle::Boot.get_option('platform.native')";
            Assert.assertEquals(false, context.eval("ruby", option).asBoolean());
            option = "Truffle::Boot.get_option('single_threaded')";
            Assert.assertEquals(true, context.eval("ruby", option).asBoolean());
        }
    }

    @Test
    public void testNativeNoThreads() throws Throwable {
        String home = System.getProperty("user.dir");
        try (Context context = Context.newBuilder("ruby").allowNativeAccess(true).option("ruby.home", home).build()) {
            Assert.assertEquals(3, context.eval("ruby", "1 + 2").asInt());

            Assert.assertEquals(true, context.eval("ruby", "File.directory?('.')").asBoolean());
        }
    }

    @Test
    public void testThreadsNoNative() throws Throwable {
        // The ruby.single_threaded option needs to be set because -Xsingle_threaded defaults to -Xembedded.
        try (Context context = Context.newBuilder("ruby").allowCreateThread(true).option("ruby.single_threaded", "false").build()) {
            Assert.assertEquals(3, context.eval("ruby", "1 + 2").asInt());

            Assert.assertEquals(7, context.eval("ruby", "Thread.new { 3 + 4 }.value").asInt());
        }
    }

    @Test
    public void testNoThreadsEnforcesSingleThreadedOption() throws Throwable {
        try (Context context = Context.newBuilder("ruby").option("ruby.single_threaded", "false").build()) {
            String option = "Truffle::Boot.get_option('single_threaded')";
            Assert.assertEquals(true, context.eval("ruby", option).asBoolean());

            String code = "begin; Thread.new {}.join; rescue SecurityError => e; e.message; end";
            Assert.assertEquals("threads not allowed in single-threaded mode", context.eval("ruby", code).asString());
        }
    }

}
