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

import org.truffleruby.Layouts;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.parser.ArgumentDescriptor;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

/**
 * {@link InternalMethod} objects are copied as properties such as visibility are changed.
 * {@link SharedMethodInfo} stores the state that does not change, such as where the method was defined.
 */
public class SharedMethodInfo {

    private final SourceSection sourceSection;
    private final LexicalScope lexicalScope;
    private final Arity arity;
    @CompilationFinal private DynamicObject definitionModule;
    /**
     * The original name of the method. Does not change when aliased.
     * This is the name shown in backtraces: "from FILE:LINE:in `NAME'".
     */
    private final String name;
    private final int blockDepth;
    /** Extra information. If blockDepth > 0 then it is the name of the method containing this block. */
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
            int blockDepth,
            String notes,
            ArgumentDescriptor[] argumentDescriptors,
            boolean alwaysClone) {

        assert lexicalScope != null;
        this.sourceSection = sourceSection;
        this.lexicalScope = lexicalScope;
        this.arity = arity;
        this.definitionModule = definitionModule;
        this.name = name;
        this.blockDepth = blockDepth;
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

    public SharedMethodInfo withMethodName(String newName) {
        return new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                arity,
                definitionModule,
                newName,
                blockDepth,
                notes,
                argumentDescriptors,
                alwaysClone);
    }

    public SharedMethodInfo forDefineMethod(DynamicObject newDefinitionModule, String newName) {
        return new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                arity,
                newDefinitionModule,
                newName,
                0, // no longer a block
                null,
                argumentDescriptors,
                alwaysClone);
    }

    /**
     * A more complete name than just <code>this.name</code>, for tooling, to easily identify what a
     * RubyRootNode corresponds to.
     */
    public String getModuleAndMethodName() {
        if (blockDepth > 0) {
            assert name.startsWith("block ") : name;
            final String methodName = notes;
            return getBlockName(blockDepth, moduleAndMethodName(definitionModule, methodName));
        } else {
            return moduleAndMethodName(definitionModule, name);
        }
    }

    private String moduleAndMethodName(DynamicObject module, String methodName) {
        if (module != null && methodName != null) {
            if (RubyGuards.isMetaClass(module)) {
                final DynamicObject attached = Layouts.CLASS.getAttached(module);
                return Layouts.MODULE.getFields(attached).getName() + "." + methodName;
            } else {
                return Layouts.MODULE.getFields(module).getName() + "#" + methodName;
            }
        } else if (methodName != null) {
            return methodName;
        } else {
            return "<unknown>";
        }
    }

    public static String getBlockName(int blockDepth, String methodName) {
        assert blockDepth > 0;
        if (blockDepth > 1) {
            return "block (" + blockDepth + " levels) in " + methodName;
        } else {
            return "block in " + methodName;
        }
    }

    public String getDescriptiveNameAndSource() {
        if (descriptiveNameAndSource == null) {
            String descriptiveName = getModuleAndMethodName();
            if (hasNotes()) {
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

    private boolean hasNotes() {
        return notes != null && blockDepth == 0;
    }

    @Override
    public String toString() {
        return getDescriptiveNameAndSource();
    }

    public DynamicObject getDefinitionModule() {
        return definitionModule;
    }

    public void setDefinitionModuleIfUnset(DynamicObject definitionModule) {
        if (this.definitionModule == null) {
            this.definitionModule = definitionModule;
        }
    }

}
