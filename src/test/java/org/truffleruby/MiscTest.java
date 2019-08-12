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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Timer;
import java.util.TimerTask;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import org.junit.Test;
import org.truffleruby.shared.options.OptionsCatalog;

public class MiscTest {

    @Test
    public void testMembersAndStringUnboxing() {
        try (Context context = RubyTest.createContext()) {
            Value result = context.eval("ruby", "Struct.new(:id, :text, :arr).new(42, '42', [1, 42, 3])");
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
    public void timeoutExecution() {
        Context context = RubyTest.createContext();

        Timer timer = new Timer();
        // schedule a timeout in 1s
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    context.close(true);
                } catch (PolyglotException e) {
                    assertTrue(e.isCancelled());
                }
            }
        }, 1000);

        try {
            String maliciousCode = "while true; end";
            context.eval("ruby", maliciousCode);
            Assert.fail();
        } catch (PolyglotException e) {
            assertTrue(e.isCancelled());
        }
    }

    @Test
    public void testEvalFromIntegratorThreadSingleThreaded() throws Throwable {
        final String codeDependingOnCurrentThread = "Thread.current.object_id";

        try (Context context = RubyTest.createContext()) {
            long thread1 = context.eval("ruby", codeDependingOnCurrentThread).asLong();

            TestingThread thread = new TestingThread(() -> {
                long thread2 = context.eval("ruby", codeDependingOnCurrentThread).asLong();
                assertNotEquals(thread1, thread2);
            });
            thread.start();
            thread.join();
        }
    }

    @Test
    public void testFiberFromIntegratorThread() throws Throwable {
        try (Context context = RubyTest.setupContext(Context.newBuilder()).option(OptionsCatalog.SINGLE_THREADED.getName(), Boolean.FALSE.toString()).allowCreateThread(true).build()) {
            context.eval("ruby", ":init");

            TestingThread thread = new TestingThread(() -> {
                int value = context.eval("ruby", "Fiber.new { 6 * 7 }.resume").asInt();
                assertEquals(42, value);
            });
            thread.start();
            thread.join();
        }
    }

}
