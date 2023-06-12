/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.Set;

import org.truffleruby.RubyContext;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.SourceSection;

public class RubyConstant implements ObjectGraphNode {

    public static final RubyConstant[] EMPTY_ARRAY = new RubyConstant[0];

    private final RubyModule declaringModule;
    private final String name;
    private final Object value;
    private final boolean isPrivate;
    private final boolean isDeprecated;

    private final AutoloadConstant autoloadConstant;

    private final SourceSection sourceSection;

    public RubyConstant(
            RubyModule declaringModule,
            String name,
            Object value,
            boolean isPrivate,
            AutoloadConstant autoloadConstant,
            boolean isDeprecated,
            SourceSection sourceSection) {
        this.declaringModule = declaringModule;
        this.name = name;
        this.value = value;
        this.isPrivate = isPrivate;
        this.isDeprecated = isDeprecated;
        this.autoloadConstant = autoloadConstant;
        this.sourceSection = sourceSection;
    }

    public RubyConstant copy(RubyModule declaringModule) {
        assert !isAutoload();
        return new RubyConstant(declaringModule, name, value, isPrivate, null, isDeprecated, sourceSection);
    }

    public RubyModule getDeclaringModule() {
        return declaringModule;
    }

    public String getName() {
        return name;
    }

    public boolean hasValue() {
        return !isAutoload();
    }

    public Object getValue() {
        assert hasValue();
        return value;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public RubyConstant withPrivate(boolean isPrivate) {
        if (isPrivate == this.isPrivate) {
            return this;
        } else {
            return new RubyConstant(
                    declaringModule,
                    name,
                    value,
                    isPrivate,
                    autoloadConstant,
                    isDeprecated,
                    sourceSection);
        }
    }

    public RubyConstant withDeprecated() {
        if (this.isDeprecated()) {
            return this;
        } else {
            return new RubyConstant(
                    declaringModule,
                    name,
                    value,
                    isPrivate,
                    autoloadConstant,
                    true,
                    sourceSection);
        }
    }

    @TruffleBoundary
    public boolean isVisibleTo(RubyContext context, LexicalScope lexicalScope, RubyModule module) {
        assert lexicalScope == null || lexicalScope.getLiveModule() == module;

        if (!isPrivate) {
            return true;
        }

        // Look in lexical scope
        if (lexicalScope != null) {
            while (lexicalScope != context.getRootLexicalScope()) {
                if (lexicalScope.getLiveModule() == declaringModule) {
                    return true;
                }
                lexicalScope = lexicalScope.getParent();
            }
        }

        // Look in ancestors
        if (module instanceof RubyClass) {
            for (RubyModule included : module.fields.ancestors()) {
                if (included != module && included == declaringModule) {
                    return true;
                }
            }
        }

        // Allow Object constants if looking with lexical scope.
        if (lexicalScope != null && context.getCoreLibrary().objectClass == declaringModule) {
            return true;
        }

        return false;
    }

    public boolean isAutoload() {
        return autoloadConstant != null;
    }

    public AutoloadConstant getAutoloadConstant() {
        return autoloadConstant;
    }

    @Override
    public void getAdjacentObjects(Set<Object> adjacent) {
        if (ObjectGraph.isRubyObject(value)) {
            adjacent.add(value);
        }
    }

    @Override
    public String toString() {
        return getDeclaringModule().fields.getName() + "::" + getName();
    }

}
