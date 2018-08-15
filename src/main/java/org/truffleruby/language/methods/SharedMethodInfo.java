/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.Layouts;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.parser.ArgumentDescriptor;

/**
 * {@link InternalMethod} objects are copied as properties such as visibility are changed.
 * {@link SharedMethodInfo} stores the state that does not change, such as where the method was defined.
 */
public class SharedMethodInfo {

    private final SourceSection sourceSection;
    private final LexicalScope lexicalScope;
    private final Arity arity;
    private DynamicObject definitionModule;
    /** The original name of the method. Does not change when aliased. */
    private final String name;
    private final String notes;
    private final ArgumentDescriptor[] argumentDescriptors;
    private boolean alwaysClone;
    private String descriptiveNameAndSource;

    public SharedMethodInfo(
            SourceSection sourceSection,
            LexicalScope lexicalScope,
            Arity arity,
            DynamicObject definitionModule,
            String name,
            String notes,
            ArgumentDescriptor[] argumentDescriptors,
            boolean alwaysClone) {

        assert lexicalScope != null;
        this.sourceSection = sourceSection;
        this.lexicalScope = lexicalScope;
        this.arity = arity;
        this.definitionModule = definitionModule;
        this.name = name;
        this.notes = notes;
        this.argumentDescriptors = argumentDescriptors;
        this.alwaysClone = alwaysClone;
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public LexicalScope getLexicalScope() {
        return lexicalScope;
    }

    public Arity getArity() {
        return arity;
    }

    public String getName() {
        return name;
    }

    public ArgumentDescriptor[] getArgumentDescriptors() {
        return argumentDescriptors == null ? arity.toAnonymousArgumentDescriptors() : argumentDescriptors;
    }

    public boolean shouldAlwaysClone() {
        return alwaysClone;
    }

    public void setAlwaysClone(boolean alwaysClone) {
        this.alwaysClone = alwaysClone;
    }

    public SharedMethodInfo withName(String newName) {
        return new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                arity,
                definitionModule,
                newName,
                notes,
                argumentDescriptors,
                alwaysClone);
    }

    public String getModuleAndMethodName() {
        if (definitionModule != null && name != null) {
            if (RubyGuards.isMetaClass(definitionModule)) {
                final DynamicObject attached = Layouts.CLASS.getAttached(definitionModule);
                return Layouts.MODULE.getFields(attached).getName() + "." + name;
            } else {
                return Layouts.MODULE.getFields(definitionModule).getName() + "#" + name;
            }
        } else if (name != null) {
            return name;
        } else {
            return "<unknown>";
        }
    }

    public String getDescriptiveNameAndSource() {
        if (descriptiveNameAndSource == null) {
            String descriptiveName = getModuleAndMethodName();
            if (notes != null) {
                if (descriptiveName.length() > 0) {
                    descriptiveName += " (" + notes + ")";
                } else {
                    descriptiveName += notes;
                }
            }

            if (sourceSection == null || !sourceSection.isAvailable()) {
                descriptiveNameAndSource = descriptiveName;
            } else {
                descriptiveNameAndSource = descriptiveName + " " + sourceSection.getSource().getName() + ":" + sourceSection.getStartLine();
            }
        }

        return descriptiveNameAndSource;
    }

    @Override
    public String toString() {
        return getDescriptiveNameAndSource();
    }

    public void setDefinitionModuleIfUnset(DynamicObject definitionModule) {
        if (this.definitionModule == null) {
            this.definitionModule = definitionModule;
        }
    }

}
