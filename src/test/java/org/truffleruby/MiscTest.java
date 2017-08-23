/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MiscTest {

    @Test
    public void testMembersAndStringUnboxing() {
        try (Context context = Context.create()) {
            Value result = context.eval("ruby", "Truffle::Interop.object_literal(id: 42, text: '42', arr: [1,42,3])");
            assertTrue(result.hasMembers());

            int id = result.getMember("id").asInt();
            assertEquals(42, id);

            String text = result.getMember("text").asString();
            assertEquals("42", text);

            Value array = result.getMember("arr");
            assertTrue(array.hasArrayElements());
            assertEquals(3, array.getArraySize());
            assertEquals(42, array.getArrayElement(1).asInt());
        }
    }

    @Test
    public void testForeignThreadEntry() {
        try (Context context = Context.create()) {
            final Value result = context.eval("ruby", "proc { 14 + 2 }");

            assertEquals(16, result.execute().asInt());

            final Thread thread = new Thread(() -> {
                assertEquals(16, result.execute().asInt());
            });

            while (true) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    continue;
                }

                break;
            }

            assertEquals(16, result.execute().asInt());
        }
    }

    @Test
    public void testThreadsNotAllowed() {
        try (Context context = Context.newBuilder().allowCreateThread(false).build()) {
            assertFalse(context.eval("ruby", "Truffle.threads?").asBoolean());

            try {
                context.eval("ruby", "Thread.new { }");
            } catch (PolyglotException e) {
                assertTrue(e.getMessage().indexOf("Creating threads is not allowed") != -1);
            }
        }
    }

    @Ignore("Ruby doesn't shut down fibres properly")
    @Test
    public void testThreadsAllowed() {
        try (Context context = Context.newBuilder().allowCreateThread(true).build()) {
            assertTrue(context.eval("ruby", "Truffle.threads?").asBoolean());
            context.eval("ruby", "Thread.new { }");
        }
    }

    @Test
    public void testForeignThreadReference() {
        try (Context context = Context.create()) {
            final Value result = context.eval("ruby", "proc { Thread.current.object_id }");

            final int mainThreadId = result.execute().asInt();

            final Thread thread = new Thread(() -> {
                // This operation currently throws an exception, as there is no Ruby thread object - may change in the future
                try {
                    assertNotEquals(mainThreadId, result.execute().asInt());
                    fail();
                } catch (PolyglotException e) {
                    assertTrue(e.getMessage().indexOf("thread operation not supported on non-Ruby thread") != -1);
                }
            });

            thread.start();

            while (true) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    continue;
                }

                break;
            }

            assertEquals(mainThreadId, result.execute().asInt());
        }
    }

    @Ignore("Ruby doesn't shut down fibres properly")
    @Test
    public void testFiberShutdown() {
        try (Context context = Context.newBuilder().allowCreateThread(true).build()) {
            context.eval("ruby", "[1, 2, 3].each.tap { |e| e.next }.tap { |e| e.next }");
        }
    }

}
