/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.Arrays;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.ArgumentType;

import com.oracle.truffle.api.source.SourceSection;

/** {@link InternalMethod} objects are copied as properties such as visibility are changed. {@link SharedMethodInfo}
 * stores the state that does not change, such as where the method was defined. */
public class SharedMethodInfo {

    private final SourceSection sourceSection;
    /** LexicalScope if it can be determined statically at parse time, otherwise null */
    private final LexicalScope staticLexicalScope;
    private final Arity arity;
    /** The original name of the method. Does not change when aliased. Looks like "block in foo" or "block (2 levels) in
     * foo" for blocks. This is the name shown in backtraces: "from FILE:LINE:in `NAME'". */
    private final String backtraceName;
    /** The "static" name of this method at parse time, such as "M::C#foo", "M::C.foo", "<module:Inner>", "block (2
     * levels) in M::C.foo" or "block (2 levels) in <module:Inner>". This name is used for tools. */
    private final String parseName;
    private final int blockDepth;
    /** Extra information. If blockDepth > 0 then it is the name of the method containing this block. */
    private final String notes;
    private final ArgumentDescriptor[] argumentDescriptors;
    private String descriptiveNameAndSource;

    public SharedMethodInfo(
            SourceSection sourceSection,
            LexicalScope staticLexicalScope,
            Arity arity,
            String backtraceName,
            int blockDepth,
            String parseName,
            String notes,
            ArgumentDescriptor[] argumentDescriptors) {
        assert blockDepth == 0 || backtraceName.startsWith("block ") : backtraceName;
        this.sourceSection = sourceSection;
        this.staticLexicalScope = staticLexicalScope;
        this.arity = arity;
        this.backtraceName = backtraceName;
        this.blockDepth = blockDepth;
        this.parseName = parseName;
        this.notes = notes;
        this.argumentDescriptors = argumentDescriptors;
    }

    public SharedMethodInfo forDefineMethod(RubyModule declaringModule, String methodName) {
        return new SharedMethodInfo(
                sourceSection,
                staticLexicalScope,
                arity,
                methodName,
                0, // no longer a block
                moduleAndMethodName(declaringModule, methodName),
                null,
                argumentDescriptors);
    }

    public SharedMethodInfo convertMethodMissingToMethod(RubyModule declaringModule, String methodName) {
        final ArgumentDescriptor[] oldArgs = getArgumentDescriptors();
        final ArgumentDescriptor[] newArgs = Arrays.copyOfRange(oldArgs, 1, oldArgs.length);
        newArgs[0] = new ArgumentDescriptor(ArgumentType.anonrest);

        return new SharedMethodInfo(
                sourceSection,
                staticLexicalScope,
                arity.consumingFirstRequired(),
                methodName,
                blockDepth,
                moduleAndMethodName(declaringModule, methodName),
                notes,
                newArgs);
    }

    public SharedMethodInfo withArity(Arity newArity) {
        return new SharedMethodInfo(
                sourceSection,
                staticLexicalScope,
                newArity,
                backtraceName,
                blockDepth,
                parseName,
                notes,
                argumentDescriptors);
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public LexicalScope getStaticLexicalScope() {
        assert staticLexicalScope != null;
        return staticLexicalScope;
    }

    public LexicalScope getStaticLexicalScopeOrNull() {
        return staticLexicalScope;
    }

    public Arity getArity() {
        return arity;
    }

    public ArgumentDescriptor[] getArgumentDescriptors() {
        return argumentDescriptors == null ? arity.toAnonymousArgumentDescriptors() : argumentDescriptors;
    }

    public boolean isBlock() {
        return blockDepth > 0;
    }

    public String getBacktraceName() {
        return backtraceName;
    }

    /** Returns the method name on its own. Can start with "<" like "<module:Inner>" for module bodies. */
    public String getMethodName() {
        return blockDepth == 0 ? backtraceName : notes;
    }

    /** More efficient than {@link #getMethodName()} when we know blockDepth == 0 */
    public String getMethodNameForNotBlock() {
        assert blockDepth == 0;
        return backtraceName;
    }

    public String getParseName() {
        return parseName;
    }

    /** See also {@link org.truffleruby.core.module.ModuleOperations#constantName}. Version without context which
     * returns "Object::A" for top-level constant A. */
    public static String moduleAndConstantName(RubyModule module, String constantName) {
        return module.fields.getName() + "::" + constantName;
    }

    public static String moduleAndMethodName(RubyModule module, String methodName) {
        assert module != null && methodName != null;
        if (RubyGuards.isMetaClass(module)) {
            final RubyDynamicObject attached = ((RubyClass) module).attached;
            return ((RubyModule) attached).getName() + "." + methodName;
        } else {
            return module.getName() + "#" + methodName;
        }
    }

    public static String modulePathAndMethodName(String modulePath, String methodName, boolean onSingleton) {
        assert modulePath != null && methodName != null;
        if (onSingleton) {
            return modulePath + "." + methodName;
        } else {
            return modulePath + "#" + methodName;
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
            String descriptiveName = parseName;
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
                        RubyLanguage.getPath(sourceSection.getSource()) + ":" + sourceSection.getStartLine();
            }
        }

        return descriptiveNameAndSource;
    }

    private boolean hasNotes() {
        return notes != null && blockDepth == 0;
    }

    public String getNotes() {
        assert hasNotes();
        return notes;
    }

    @Override
    public String toString() {
        return getDescriptiveNameAndSource();
    }

}
