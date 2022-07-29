/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Test;
import org.truffleruby.fixtures.FluidForce;
import org.truffleruby.services.scriptengine.TruffleRubyScriptEngineFactory;
import org.truffleruby.shared.TruffleRuby;

public class JSR223InteropTest {

    private ScriptEngine scriptEngine = null;

    @After
    public void after() {
        if (scriptEngine != null) {
            close(scriptEngine);
            scriptEngine = null;
        }
    }

    private static void close(ScriptEngine scriptEngine) {
        try {
            ((AutoCloseable) scriptEngine).close();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void testVersion() {
        assertEquals(TruffleRuby.getEngineVersion(), new TruffleRubyScriptEngineFactory().getEngineVersion());
    }

    @Test
    public void testCreateEngine() throws ScriptException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        assertEquals(42, scriptEngine.eval("6 * 7"));
    }

    @Test
    public void testNoPermissionsByDefault() {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        try {
            scriptEngine.eval("Process.pid");
            fail("should have thrown");
        } catch (ScriptException scriptException) {
            assertEquals("org.graalvm.polyglot.PolyglotException: native access is not allowed (SecurityError)",
                    scriptException.getMessage());
        }
    }

    @Test
    public void testAllAccess() throws ScriptException {
        scriptEngine = new TruffleRubyScriptEngineFactory().getScriptEngine(true);
        assertTrue(scriptEngine.eval("Process.pid") instanceof Integer);
    }

    @Test
    public void testParameters() throws ScriptException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        final Bindings bindings = scriptEngine.createBindings();
        bindings.put("a", 14);
        bindings.put("b", 2);
        assertEquals(16, scriptEngine.eval("a + b", bindings));
    }

    @Test
    public void testCallingMethods() throws ScriptException, NoSuchMethodException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        assertEquals(
                0.909,
                (double) ((Invocable) scriptEngine).invokeMethod(scriptEngine.eval("Math"), "sin", 2),
                0.01);
    }

    @Test
    public void testCreatingObjects() throws ScriptException, NoSuchMethodException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        final Object time = ((Invocable) scriptEngine).invokeMethod(scriptEngine.eval("Time"), "new", 2021, 3, 18);
        final Object year = ((Invocable) scriptEngine).invokeMethod(time, "year");
        assertEquals(2021, year);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAccessingArrays() throws ScriptException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        assertEquals(4, ((List<Object>) scriptEngine.eval("[3, 4, 5]")).get(1));
    }

    @Test
    public void testAccessingHashes() throws ScriptException, NoSuchMethodException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        assertEquals(
                4,
                (int) ((Invocable) scriptEngine).invokeMethod(
                        scriptEngine.eval("{'a' => 3, 'b' => 4, 'c' => 5}"),
                        "fetch",
                        'b'));
    }

    @Test
    public void testImplementInterface() throws ScriptException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        final FluidForce fluidForce = ((Invocable) scriptEngine)
                .getInterface(scriptEngine.eval(FluidForce.RUBY_SOURCE), FluidForce.class);
        assertEquals(5587.008375144088, fluidForce.getFluidForce(2.0, 3.0, 6.0), 0.01);
    }

    @Test
    public void testParseOnceRunMany() throws ScriptException {
        final ScriptEngineManager m = new ScriptEngineManager();
        scriptEngine = m.getEngineByName("ruby");
        final CompiledScript compiled = ((Compilable) scriptEngine).compile("14");
        assertEquals(14, compiled.eval());
    }

}
