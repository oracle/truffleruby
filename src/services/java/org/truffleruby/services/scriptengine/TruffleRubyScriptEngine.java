/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.services.scriptengine;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

public class TruffleRubyScriptEngine extends AbstractScriptEngine
        implements ScriptEngine, Invocable, Compilable, AutoCloseable {

    private final TruffleRubyScriptEngineFactory factory;

    private final Context polyglot;

    TruffleRubyScriptEngine(TruffleRubyScriptEngineFactory factory, boolean allowAllAccess) {
        this.factory = factory;
        this.polyglot = Context.newBuilder("ruby").allowAllAccess(allowAllAccess).build();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public Bindings createBindings() {
        return new TruffleRubyBindings();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try {
            return polyglot.eval("ruby", script).as(Object.class);
        } catch (PolyglotException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(readAllOfReader(reader), context);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        // We parameterise scripts by wrapping them in a lambda and executing it

        final List<Map.Entry<String, Object>> entries = new ArrayList<>(bindings.entrySet());
        final String parameterisedScript = parameteriseScript(
                script,
                entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        final Object[] values = entries.stream().map(Map.Entry::getValue).toArray();

        try {
            return polyglot.eval("ruby", parameterisedScript).execute(values).as(Object.class);
        } catch (PolyglotException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return eval(readAllOfReader(reader), bindings);
    }

    @Override
    public Object invokeMethod(Object receiver, String name, Object... args) {
        return polyglot.asValue(receiver).getMember(name).execute(args).as(Object.class);
    }

    @Override
    public Object invokeFunction(String name, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInterface(Class<T> interfaceClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInterface(Object receiver, Class<T> interfaceClass) {
        return polyglot.asValue(receiver).as(interfaceClass);
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        final Value compiled;

        try {
            compiled = polyglot.eval("ruby", parameteriseScript(script, Collections.emptyList()));
        } catch (PolyglotException e) {
            throw new ScriptException(e);
        }

        return new CompiledScript() {

            @Override
            public Object eval(ScriptContext context) throws ScriptException {
                try {
                    return compiled.execute().as(Object.class);
                } catch (PolyglotException e) {
                    throw new ScriptException(e);
                }
            }

            @Override
            public ScriptEngine getEngine() {
                return TruffleRubyScriptEngine.this;
            }

        };
    }

    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        return compile(readAllOfReader(script));
    }

    @Override
    public void close() {
        polyglot.close();
    }

    private String readAllOfReader(Reader reader) throws ScriptException {
        final StringBuilder stringBuilder = new StringBuilder(4096);

        final char[] buffer = new char[4096];

        while (true) {
            final int read;

            try {
                read = reader.read(buffer);
            } catch (IOException e) {
                throw new ScriptException(e);
            }

            if (read == -1) {
                break;
            }

            stringBuilder.append(buffer, 0, read);
        }

        return stringBuilder.toString();
    }

    private String parameteriseScript(String script, List<String> parameters) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("->(");

        if (parameters.size() > 0) {
            stringBuilder.append(parameters.get(0));
        }

        for (int n = 1; n < parameters.size(); n++) {
            stringBuilder.append(", ");
            stringBuilder.append(parameters.get(n));
        }

        stringBuilder.append(") { ");
        stringBuilder.append(script);
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

}
