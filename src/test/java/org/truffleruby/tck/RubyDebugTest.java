/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.tck;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.DebugScope;
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
import org.truffleruby.RubyTest;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    public void testReenterArgumentsAndValues() throws Throwable {
        // Test that after a re-enter, arguments are kept and variables are cleared.
        final Source source = createReenterValuesTest();

        run.addLast(() -> {
            assertNull(suspendedEvent);
            assertNotNull(debuggerSession);
            debuggerSession.install(Breakpoint.newBuilder(DebuggerTester.getSourceImpl(source)).lineIs(12).build());
        });
        context.eval(source);

        // TODO tools team Jan-18: assertArguments(12, "x = n + m", "n", "11", "m", "20");
        assertLocation(12, "x = n + m", "n", "11", "m", "20", "x", "nil");
        stepOver(4);
        // TODO tools team Jan-18: assertArguments(16, "x = x + n * m", "n", "11", "m", "20");
        assertLocation(16, "x", "n", "9", "m", "10", "x", "121");
        run.addLast(() -> suspendedEvent.prepareUnwindFrame(suspendedEvent.getTopStackFrame()));
        // TODO tools team Jan-18: assertArguments(21, "res = fnc(i = i + 1, 20)");
        assertLocation(21, "res = fnc(i = i + 1, 20)", "i", "11", "res", "nil");
        continueExecution();
        // TODO tools team Jan-18: assertArguments(12, "x = n + m", "n", "11", "m", "20");
        assertLocation(12, "x = n + m", "n", "11", "m", "20", "x", "nil");
        continueExecution();

        performWork();
        Value value = context.getPolyglotBindings().getMember("main").execute();
        assertEquals(121, value.as(Number.class).intValue());
        assertExecutedOK("Unwind O.K.");
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

    // Used from commented out asserts in testReenterArgumentsAndValues
    private void assertArguments(final int line, final String code, final String... expectedArgs) {
        run.addLast(() -> {
            final DebugStackFrame frame = suspendedEvent.getTopStackFrame();
            DebugScope scope = frame.getScope();

            int n = expectedArgs.length/2;
            List<DebugValue> actualValues = new ArrayList<>(n);
            if (scope.getArguments() != null) {
                scope.getArguments().forEach((x) -> actualValues.add(x));
            }

            assertEquals(line + ": " + code, n, actualValues.size());

            for (int i = 0; i < n; i++) {
                int i2 = i << 1;
                assertEquals(expectedArgs[i2], actualValues.get(i).getName());
                assertEquals(expectedArgs[i2 + 1], actualValues.get(i).as(String.class));
            }

            run.removeFirst().run();
        });
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
            // There is (self) among the variables, hence substract 1:
            assertEquals(expectedFrame.length / 2, numFrameVars.get() - 1);

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

    private static Source createReenterValuesTest() {
        return getSource("src/test/ruby/reenter_values.rb");
    }

    private final String getErr() {
        try {
            err.flush();
        } catch (IOException e) {
        }
        return new String(err.toByteArray());
    }

}
