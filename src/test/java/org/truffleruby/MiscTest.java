/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.truffleruby.shared.options.OptionsCatalog;

public class MiscTest {

    @Test
    public void testMembersAndStringUnboxing() {
        try (Context context = RubyTest.createContext()) {
            Value result = context.eval("ruby", "Struct.new(:id, :text, :arr).new(42, '42', [1, 42, 3])");
            assertTrue(result.hasMembers());

            int id = result.invokeMember("id").asInt();
            assertEquals(42, id);

            String text = result.invokeMember("text").asString();
            assertEquals("42", text);

            Value array = result.invokeMember("arr");
            assertTrue(array.hasArrayElements());
            assertEquals(3, array.getArraySize());
            assertEquals(42, array.getArrayElement(1).asInt());
        }
    }

    @Test
    public void timeoutExecution() throws Throwable {
        Context context = RubyTest.createContext();

        // schedule a timeout in 1s
        TestingThread thread = new TestingThread(() -> {
            try {
                Thread.sleep(1000);
                context.close(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            } catch (PolyglotException e) {
                assertTrue(e.isCancelled());
            }
        });

        thread.start();
        try {
            String maliciousCode = "while true; end";
            context.eval("ruby", maliciousCode);
            Assert.fail();
        } catch (PolyglotException e) {
            assertTrue(e.isCancelled());
        } finally {
            thread.join();
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
        try (Context context = RubyTest
                .setupContext(Context.newBuilder())
                .option(OptionsCatalog.SINGLE_THREADED.getName(), "false")
                .allowCreateThread(true)
                .build()) {
            context.eval("ruby", ":init");

            TestingThread thread = new TestingThread(() -> {
                int value = context.eval("ruby", "Fiber.new { 6 * 7 }.resume").asInt();
                assertEquals(42, value);
            });
            thread.start();
            thread.join();
        }
    }

    @Test
    public void testIntegratorThreadRubyThreadInitialization() throws Throwable {
        try (Context context = Context.newBuilder("ruby").allowCreateThread(true).build()) {
            context.initialize("ruby");
            TestingThread thread = new TestingThread(() -> {
                // Run code that requires the Ruby Thread object to be initialized
                Value recursiveArray = context.eval("ruby", "a = [0]; a[0] = a; a.inspect"); // Access @recursive_objects
                Assert.assertEquals("[[...]]", recursiveArray.asString());
                Assert.assertTrue(context.eval("ruby", "Thread.current.thread_variable_get('foo')").isNull());
                Assert.assertTrue(context.eval("ruby", "rand").fitsInDouble());
            });
            thread.start();
            thread.join();
        }
    }

    @Ignore // TODO (eregon, 8 April 2020): not yet working
    @Test
    public void testIntegratorThreadContextClosedOnOtherThread() throws Throwable {
        try (Context context = Context.newBuilder("ruby").allowCreateThread(true).build()) {
            TestingThread thread = new TestingThread(() -> {
                Assert.assertEquals(42, context.eval("ruby", "6 * 7").asInt());
            });
            thread.start();
            thread.join();
        }
    }

}
