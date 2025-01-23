/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.DetailedInspectingSupport;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.parser.ArgumentDescriptor;

import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.parser.OpenModule;
import org.truffleruby.parser.ParserContext;

import java.util.Arrays;

/** SharedMethodInfo represents static information from the parser for either a method definition or a block like its
 * name, SourceSection, etc. Such information is always "original" since it comes from the source as opposed to
 * "aliased" (e.g. the aliased name of a method). In contrast, {@link InternalMethod} are runtime objects containing
 * properties that change for a method. */
public final class SharedMethodInfo implements DetailedInspectingSupport {

    private final SourceSection sourceSection;
    /** LexicalScope if it can be determined statically at parse time, otherwise null */
    private final LexicalScope staticLexicalScope;
    private final Arity arity;
    /** The original name of the method. Does not change when aliased. Looks like "block in foo" or "block (2 levels) in
     * foo" for blocks. This is the name shown in backtraces: "from FILE:LINE:in `NAME'". */
    private final String originalName;
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
            String originalName,
            int blockDepth,
            String parseName,
            String notes,
            ArgumentDescriptor[] argumentDescriptors) {
        assert blockDepth == 0 || originalName.startsWith("block ") : originalName;
        this.sourceSection = sourceSection;
        this.staticLexicalScope = staticLexicalScope;
        this.arity = arity;
        this.originalName = originalName;
        this.blockDepth = blockDepth;
        this.parseName = parseName;
        this.notes = notes;
        this.argumentDescriptors = argumentDescriptors;
    }

    public SharedMethodInfo forDefineMethod(RubyModule declaringModule, String methodName, RubyProc proc) {
        return new SharedMethodInfo(
                sourceSection,
                staticLexicalScope,
                proc.arity,
                methodName,
                0, // no longer a block
                moduleAndMethodName(declaringModule, methodName),
                null,
                proc.argumentDescriptors);
    }

    public SharedMethodInfo convertMethodMissingToMethod(RubyModule declaringModule, String methodName) {
        return new SharedMethodInfo(
                sourceSection,
                staticLexicalScope,
                arity.consumingFirstRequired(),
                methodName,
                blockDepth,
                moduleAndMethodName(declaringModule, methodName),
                notes,
                ArgumentDescriptor.ANY_UNNAMED);
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

    public ArgumentDescriptor[] getRawArgumentDescriptors() {
        return argumentDescriptors;
    }

    public ArgumentDescriptor[] getArgumentDescriptors() {
        return argumentDescriptors == null ? arity.toUnnamedArgumentDescriptors() : argumentDescriptors;
    }

    public boolean isBlock() {
        return blockDepth > 0;
    }

    @TruffleBoundary
    public boolean isModuleBody() {
        boolean isModuleBody = isModuleBody(getOriginalName());
        assert !(isModuleBody && isBlock()) : this;
        return isModuleBody;
    }

    public static boolean isModuleBody(String name) {
        // Handles cases: <main> | <top (required)> | <module: | <class: | <singleton
        if (name.startsWith("<")) {
            assert name.equals(ParserContext.TOP_LEVEL_FIRST.getTopLevelName()) ||
                    name.equals(ParserContext.TOP_LEVEL.getTopLevelName()) ||
                    name.startsWith(OpenModule.MODULE.getPrefix()) ||
                    name.startsWith(OpenModule.CLASS.getPrefix()) ||
                    name.startsWith(OpenModule.SINGLETON_CLASS.getPrefix()) : name;
            return true;
        } else {
            return false;
        }
    }

    /** See {@link #originalName} */
    public String getOriginalName() {
        return originalName;
    }

    /** Returns the method name on its own. Can start with "<" like "<module:Inner>" for module bodies. */
    public String getMethodName() {
        return blockDepth == 0 ? originalName : notes;
    }

    /** More efficient than {@link #getMethodName()} when we know blockDepth == 0 */
    public String getMethodNameForNotBlock() {
        assert blockDepth == 0;
        return originalName;
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
                descriptiveNameAndSource = descriptiveName + " " + RubyLanguage.fileLineRange(sourceSection);
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

    @Override
    public String toStringWithDetails() {
        final String string = Arrays.deepToString(argumentDescriptors);

        // skip sourceSection as far as it contains not accurate location details (provided by JRuby parser)
        return StringUtils.format(
                "SharedMethodInfo(staticLexicalScope = %s, arity = %s, originName = %s, blockDepth = %s, parseName = %s, notes = %s, argumentDescriptors = %s)",
                staticLexicalScope, arity, originalName, blockDepth, parseName, notes, string);
    }

}
