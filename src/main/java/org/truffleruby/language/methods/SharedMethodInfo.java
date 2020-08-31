/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.Arrays;

import org.truffleruby.RubyContext;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.ArgumentType;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.source.SourceSection;

/** {@link InternalMethod} objects are copied as properties such as visibility are changed. {@link SharedMethodInfo}
 * stores the state that does not change, such as where the method was defined. */
public class SharedMethodInfo {

    private final SourceSection sourceSection;
    private final LexicalScope lexicalScope;
    private final Arity arity;
    @CompilationFinal private RubyModule definitionModule;
    /** The original name of the method. Does not change when aliased. This is the name shown in backtraces:
     * "from FILE:LINE:in `NAME'". */
    private final String name;
    private final int blockDepth;
    /** Extra information. If blockDepth > 0 then it is the name of the method containing this block. */
    private final String notes;
    private final ArgumentDescriptor[] argumentDescriptors;
    private String descriptiveNameAndSource;

    public SharedMethodInfo(
            SourceSection sourceSection,
            LexicalScope lexicalScope,
            Arity arity,
            RubyModule definitionModule,
            String name,
            int blockDepth,
            String notes,
            ArgumentDescriptor[] argumentDescriptors) {
        assert lexicalScope != null;
        this.sourceSection = sourceSection;
        this.lexicalScope = lexicalScope;
        this.arity = arity;
        this.definitionModule = definitionModule;
        this.name = name;
        this.blockDepth = blockDepth;
        this.notes = notes;
        this.argumentDescriptors = argumentDescriptors;
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

    public SharedMethodInfo convertMethodMissingToMethod(String newName) {
        final ArgumentDescriptor[] oldArgs = getArgumentDescriptors();
        final ArgumentDescriptor[] newArgs = Arrays.copyOfRange(oldArgs, 1, oldArgs.length);
        newArgs[0] = new ArgumentDescriptor(ArgumentType.anonrest);

        return new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                arity.consumingFirstRequired(),
                definitionModule,
                newName,
                blockDepth,
                notes,
                newArgs);
    }

    public SharedMethodInfo forDefineMethod(RubyModule newDefinitionModule, String newName) {
        return new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                arity,
                newDefinitionModule,
                newName,
                0, // no longer a block
                null,
                argumentDescriptors);
    }

    /** Returns the method name on its own. */
    public String getMethodName() {
        return blockDepth == 0 ? name : notes;
    }

    /** A more complete name than just <code>this.name</code>, for tooling, to easily identify what a RubyRootNode
     * corresponds to. */
    public String getModuleAndMethodName() {
        if (blockDepth > 0) {
            assert name.startsWith("block ") : name;
            final String methodName = notes;
            return getBlockName(blockDepth, moduleAndMethodName(definitionModule, methodName));
        } else {
            return moduleAndMethodName(definitionModule, name);
        }
    }

    private static String moduleAndMethodName(RubyModule module, String methodName) {
        if (module != null && methodName != null) {
            if (RubyGuards.isMetaClass(module)) {
                final RubyDynamicObject attached = ((RubyClass) module).attached;
                return ((RubyModule) attached).fields.getName() + "." + methodName;
            } else {
                return module.fields.getName() + "#" + methodName;
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

            if (!BacktraceFormatter.isAvailable(sourceSection)) {
                descriptiveNameAndSource = descriptiveName;
            } else {
                descriptiveNameAndSource = descriptiveName + " " +
                        RubyContext.getPath(sourceSection.getSource()) + ":" + sourceSection.getStartLine();
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

    public RubyModule getDefinitionModule() {
        return definitionModule;
    }

    public void setDefinitionModuleIfUnset(RubyModule definitionModule) {
        if (this.definitionModule == null) {
            this.definitionModule = definitionModule;
        }
    }

}
