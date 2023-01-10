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

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.graalvm.polyglot.Context;

public class TruffleRubyScriptEngineFactory implements ScriptEngineFactory {

    @Override
    public String getEngineName() {
        return "TruffleRuby";

    }

    @Override
    public String getEngineVersion() {
        return query("RUBY_ENGINE_VERSION");
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(".rb");
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("application/x-ruby");
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("ruby", "truffleruby", "TruffleRuby");
    }

    @Override
    public String getLanguageName() {
        return "ruby";
    }

    @Override
    public String getLanguageVersion() {
        return query("RUBY_VERSION");
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.NAME:
                return "ruby";
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            case "THREADING":
                return "MULTITHREADED";
            default:
                return null;
        }
    }

    @Override
    public String getMethodCallSyntax(String object, String method, String... args) {
        final StringBuilder builder = new StringBuilder().append(object).append('.').append(method).append('(');

        final int length = args.length;

        if (length > 0) {
            builder.append(args[0]);
        }

        for (int i = 1; i < length; i++) {
            builder.append(',').append(args[i]);
        }

        builder.append(')');

        return builder.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return String.format("puts %s", toDisplay);
    }

    @Override
    public String getProgram(String... statements) {
        return String.join("\n", statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return getScriptEngine(false);
    }

    public ScriptEngine getScriptEngine(boolean allowAllAccess) {
        return new TruffleRubyScriptEngine(this, allowAllAccess);
    }

    private String query(String expression) {
        try (Context context = Context.create("ruby")) {
            return context.eval("ruby", expression).asString();
        }
    }

}
