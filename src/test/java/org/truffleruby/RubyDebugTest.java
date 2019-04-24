/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.DebugException;
import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.tck.DebuggerTester;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Ignore;
import org.truffleruby.shared.options.OptionsCatalog;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.truffleruby.shared.TruffleRuby;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RubyDebugTest {

    private static final int BREAKPOINT_LINE = 13;

    private Debugger debugger;
    private DebuggerSession debuggerSession;
    private final LinkedList<Runnable> run = new LinkedList<>();
    private SuspendedEvent suspendedEvent;
    private Breakpoint breakpoint;
    private Throwable ex;
    private Context context;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private static Source getSource(String path) {
        InputStream stream = ClassLoader.getSystemResourceAsStream(path);
        Reader reader = new InputStreamReader(stream);
        try {
            return Source.newBuilder(TruffleRuby.LANGUAGE_ID, reader, new File(path).getName()).build();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Before
    public void before() {
        suspendedEvent = null;

        context = RubyTest.setupContext(Context.newBuilder())
                // We also want to test instrumentation works well with lazy nodes
                .option(OptionsCatalog.LAZY_TRANSLATION_USER.getName(), Boolean.TRUE.toString())
                .out(out).err(err).build();

        Instrument debugInstrument = context.getEngine().getInstruments().get("debugger");
        debugger = debugInstrument.lookup(Debugger.class);

        debuggerSession = debugger.startSession(event -> {
            suspendedEvent = event;
            performWork();
            suspendedEvent = null;
        });

        context.eval(getSource("src/test/ruby/init.rb"));

        run.clear();
    }

    @After
    public void dispose() {
        debuggerSession.close();
        if (context != null) {
            context.close();
        }
    }

    @Ignore
    @Test
    public void testBreakpoint() throws Throwable {
        final Source factorial = createFactorial();

        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            breakpoint = Breakpoint.newBuilder(DebuggerTester.getSourceImpl(factorial)).lineIs(BREAKPOINT_LINE).build();
            debuggerSession.install(breakpoint);
        });

        // Init before eval:
        performWork();
        Assert.assertFalse("factorial not yet loaded", breakpoint.isResolved());

        context.eval(factorial);
        assertExecutedOK("Algorithm loaded");
        Assert.assertFalse("all methods are lazily translated (by the option)", breakpoint.isResolved());

        assertLocation(13, "1",
                        "n", "1",
                        "nMinusOne", "nil",
                        "nMOFact", "nil",
                        "res", "nil");

        continueExecution();

        final Value main = context.getPolyglotBindings().getMember("main");
        assertNotNull("main method found", main);
        Assert.assertFalse("not yet translated", breakpoint.isResolved());
        assertTrue(main.canExecute());
        Value value = main.execute();
        Assert.assertTrue("breakpoint must have stopped first execution", breakpoint.isResolved());
        int n = value.asInt();
        assertEquals("Factorial computed OK", 2, n);
        assertExecutedOK("Algorithm computed OK: " + n + "; Checking if it stopped at the breakpoint");
    }

    @Test
    public void stepInStepOver() throws Throwable {
        final Source factorial = createFactorial();
        context.eval(factorial);
        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            debuggerSession.suspendNextExecution();
        });

        assertLocation(23, "res = fac(2)", "res", "nil");
        stepInto(1);
        assertLocation(12, "if n <= 1",
                        "n", "2",
                        "nMinusOne", "nil",
                        "nMOFact", "nil",
                        "res", "nil");
        stepOver(1);
        assertLocation(15, "nMinusOne = n - 1",
                        "n", "2",
                        "nMinusOne", "nil",
                        "nMOFact", "nil",
                        "res", "nil");
        stepOver(1);
        assertLocation(16, "nMOFact = fac(nMinusOne)",
                        "n", "2",
                        "nMinusOne", "1",
                        "nMOFact", "nil",
                        "res", "nil");
        stepOver(1);
        assertLocation(17, "res = n * nMOFact",
                        "n", "2", "nMinusOne", "1",
                        "nMOFact", "1",
                        "res", "nil");
        continueExecution();

        // Init before eval:
        performWork();
        Value value = context.getPolyglotBindings().getMember("main").execute();

        int n = value.asInt();
        assertEquals("Factorial computed OK", 2, n);
        assertExecutedOK("Stepping went OK");
    }

    @Test
    public void testEvalThrow() throws Throwable {
        Source source = getSource("src/test/ruby/raise_ex.rb");
        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            debuggerSession.suspendNextExecution();
        });
        run.addLast(() -> {
            assertNotNull(suspendedEvent);
            int currentLine = suspendedEvent.getSourceSection().getStartLine();
            assertEquals(11, currentLine);
            run.removeFirst().run();
        });
        stepOver(2);
        assertLocation(21, "shortArg 10");
        run.addLast(() -> {
            assertNotNull(suspendedEvent);
            try {
                suspendedEvent.getTopStackFrame().eval("shortArg(90000)");
                Assert.fail("DebugException is expected.");
            } catch (DebugException dex) {
                assertNull(dex.getCatchLocation());
                Assert.assertFalse(dex.isInternalError());
                dex.getThrowLocation();
            }
            run.removeFirst().run();
        });
        continueExecution();
        performWork();
        context.eval(source);
        assertExecutedOK("OK");
    }

    @Test
    public void testInlineModifiesFrame() throws Throwable {
        Source source = getSource("src/test/ruby/modify.rb");
        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            debuggerSession.suspendNextExecution();
        });
        stepOver(2);
        run.addLast(() -> {
            assertNotNull(suspendedEvent);
            suspendedEvent.getTopStackFrame().eval("a = 22");
            run.removeFirst().run();
        });
        continueExecution();
        performWork();
        Assert.assertEquals(22 + 2 + 3, context.eval(source).asInt());
        assertExecutedOK("OK");
    }

    @Ignore
    @Test
    public void testProperties() throws Throwable {
        Source source = getSource("src/test/ruby/types.rb");
        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            breakpoint = Breakpoint.newBuilder(DebuggerTester.getSourceImpl(source)).lineIs(27).build();
            debuggerSession.install(breakpoint);
        });
        assertLocation(27, "nme + nm1",
                        "name", "\"Panama\"", // Possible bug: should really the quotes be included?
                        "cityArray", "[80, 97, 110, 97, 109, 97]",
                        "citySum", "590",
                        "weatherTemperature", "14",
                        "blt", "true",
                        "blf", "false",
                        "null", "nil",
                        "nm1", "1",
                        "nm11", "1.111",
                        "nme", "3.5e+46",
                        "nc", "(2+3i)",
                        "nr", "(5404319552844595/18014398509481984)",
                        "str", "\"A String\"",
                        "symbol", ":symbolic",
                        "arr", "[1, \"2\", 3.56, true, nil, \"A String\"]",
                        "hash", "{:a=>1, \"b\"=>2}");
        run.addLast(() -> {
            final DebugStackFrame frame = suspendedEvent.getTopStackFrame();
            DebugValue value = frame.getScope().getDeclaredValue("name");
            Collection<DebugValue> properties = value.getProperties();
            assertTrue("String has " + properties.size() + " properties.", properties.size() > 0); // BUG: String have no properties
            value = frame.getScope().getDeclaredValue("hash");
            properties = value.getProperties();
            assertEquals(2, properties.size()); // 'hash' has two properties
            try {
                properties.iterator().next().as(String.class);
            } catch (IllegalStateException ex) {
                "Value is not readable".equals(ex.getMessage()); // BUG
                Assert.fail(ex.getMessage());
            }
            assertNotNull(value.getProperty("a")); // == null, BUG
            assertEquals(0, frame.getScope().getDeclaredValue("symbol").getProperties().size());
            assertEquals(6, frame.getScope().getDeclaredValue("arr").getArray().size());
        });
        performWork();
        context.eval(source);
        assertExecutedOK("OK");
    }

    private void performWork() {
        try {
            if (ex == null && !run.isEmpty()) {
                Runnable c = run.removeFirst();
                c.run();
            }
        } catch (Throwable e) {
            ex = e;
        }
    }

    private void stepOver(final int size) {
        run.addLast(() -> suspendedEvent.prepareStepOver(size));
    }

    private void continueExecution() {
        run.addLast(() -> suspendedEvent.prepareContinue());
    }

    private void stepInto(final int size) {
        run.addLast(() -> suspendedEvent.prepareStepInto(size));
    }

    private void assertLocation(final int line, final String code, final Object... expectedFrame) {
        run.addLast(() -> {
            assertNotNull(suspendedEvent);
            final int currentLine = suspendedEvent.getSourceSection().getStartLine();
            assertEquals(line, currentLine);
            final String currentCode = suspendedEvent.getSourceSection().getCharacters().toString().trim();
            assertEquals(code, currentCode);
            final DebugStackFrame frame = suspendedEvent.getTopStackFrame();

            final AtomicInteger numFrameVars = new AtomicInteger(0);
            frame.getScope().getDeclaredValues().forEach(var -> { numFrameVars.incrementAndGet(); });
            assertEquals(expectedFrame.length / 2, numFrameVars.get());

            for (int i = 0; i < expectedFrame.length; i = i + 2) {
                String expectedIdentifier = (String) expectedFrame[i];
                Object expectedValue = expectedFrame[i + 1];
                DebugValue value = frame.getScope().getDeclaredValue(expectedIdentifier);
                assertNotNull(value);
                String valueStr = value.as(String.class);

                assertEquals(expectedValue, valueStr);
            }

            run.removeFirst().run();
        });
    }

    private void assertExecutedOK(String msg) throws Throwable {
        assertTrue(getErr(), getErr().isEmpty());

        if (ex != null) {
            if (ex instanceof AssertionError) {
                throw ex;
            } else {
                throw new AssertionError(msg + ". Error during execution ", ex);
            }
        }

        assertTrue(msg + ". Assuming all requests processed: " + run, run.isEmpty());
    }

    private static Source createFactorial() {
        return getSource("src/test/ruby/factorial.rb");
    }

    private final String getErr() {
        try {
            err.flush();
        } catch (IOException e) {
        }
        return new String(err.toByteArray());
    }

}
