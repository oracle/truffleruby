/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.scriptengine;

import org.truffleruby.RubyLanguage;
import org.truffleruby.shared.TruffleRuby;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;

public class TruffleRubyScriptEngineFactory implements ScriptEngineFactory {

    @Override
    public String getEngineName() {
        return TruffleRuby.ENGINE_NAME;
    }

    @Override
    public String getEngineVersion() {
        return TruffleRuby.getEngineVersion();
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(RubyLanguage.EXTENSION);
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList(RubyLanguage.MIME_TYPE);
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(TruffleRuby.LANGUAGE_ID, TruffleRuby.ENGINE_NAME, TruffleRuby.ENGINE_ID);
    }

    @Override
    public String getLanguageName() {
        return "ruby";
    }

    @Override
    public String getLanguageVersion() {
        return TruffleRuby.LANGUAGE_VERSION;
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.NAME:
                return TruffleRuby.LANGUAGE_ID;
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            default:
                throw new IllegalArgumentException("Invalid key");
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
        return new TruffleRubyScriptEngine(this);
    }

}
