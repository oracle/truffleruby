/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.CompilerAsserts;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.ArgumentType;

public final class Arity {

    public static final String[] NO_KEYWORDS = StringUtils.EMPTY_STRING_ARRAY;
    public static final Arity NO_ARGUMENTS = new Arity(0, 0, false);
    public static final Arity ONE_REQUIRED = new Arity(1, 0, false);
    public static final Arity ANY_ARGUMENTS = new Arity(0, 0, true);

    private final int preRequired;
    private final int optional;
    private final boolean hasRest;
    private final int postRequired;
    private final boolean hasKeywordsRest;
    private final String[] keywordArguments;
    private final int requiredKeywordArgumentsCount;
    /** During parsing we cannot know if this Arity object belongs to proc or to lambda. So we calculate the arity
     * number for both cases and provide a ProcType-dependent interface. */
    private final int arityNumber;
    private final int procArityNumber;

    public Arity(int preRequired, int optional, boolean hasRest) {
        this(preRequired, optional, hasRest, 0, NO_KEYWORDS, 0, false);
    }


    public Arity(
            int preRequired,
            int optional,
            boolean hasRest,
            int postRequired,
            String[] keywordArguments,
            int requiredKeywordArgumentsCount,
            boolean hasKeywordsRest) {
        this.preRequired = preRequired;
        this.optional = optional;
        this.hasRest = hasRest;
        this.postRequired = postRequired;
        // Required keywords are located at the beginning of the `keywordArguments` array.
        // So we can specify them with only one `int` field (`requiredKeywordArgumentsCount`).
        this.keywordArguments = keywordArguments;
        this.requiredKeywordArgumentsCount = requiredKeywordArgumentsCount;
        this.hasKeywordsRest = hasKeywordsRest;
        this.arityNumber = computeArityNumber(false);
        this.procArityNumber = computeArityNumber(true);

        assert keywordArguments != null && preRequired >= 0 && optional >= 0 && postRequired >= 0 : toString();
    }

    public Arity withRest(boolean hasRest) {
        return new Arity(
                preRequired,
                optional,
                hasRest,
                postRequired,
                keywordArguments,
                requiredKeywordArgumentsCount,
                hasKeywordsRest);
    }

    public Arity consumingFirstRequired() {
        return new Arity(
                Integer.max(preRequired - 1, 0),
                optional,
                hasRest,
                postRequired,
                keywordArguments,
                requiredKeywordArgumentsCount,
                hasKeywordsRest);
    }

    public boolean checkPositionalArguments(int given) {
        CompilerAsserts.partialEvaluationConstant(this);

        int required = getRequired();
        return given >= required && (hasRest || given <= required + optional);
    }

    public int getPreRequired() {
        return preRequired;
    }

    public int getRequired() {
        return preRequired + postRequired;
    }

    public int getOptional() {
        return optional;
    }

    public boolean hasRest() {
        return hasRest;
    }

    public boolean acceptsKeywords() {
        return hasKeywords() || hasKeywordsRest();
    }

    public boolean hasKeywords() {
        return keywordArguments.length != 0;
    }

    public boolean hasKeywordsRest() {
        return hasKeywordsRest;
    }

    public boolean allKeywordsOptional() {
        return requiredKeywordArgumentsCount == 0;
    }

    private int computeArityNumber(boolean isProc) {
        int count = getRequired();

        if (acceptsKeywords() && !allKeywordsOptional()) {
            count++;
        }

        if (hasRest || (!isProc && (optional > 0 || (acceptsKeywords() && allKeywordsOptional())))) {
            count = -count - 1;
        }

        return count;
    }

    public int getArityNumber(ProcType type) {
        return type == ProcType.PROC ? procArityNumber : arityNumber;
    }

    public int getMethodArityNumber() {
        return arityNumber;
    }

    public String[] getKeywordArguments() {
        return keywordArguments;
    }

    public String[] getRequiredKeywordArguments() {
        final String[] requiredKeywords = new String[requiredKeywordArgumentsCount];

        for (int i = 0; i < requiredKeywords.length; i++) {
            requiredKeywords[i] = keywordArguments[i];
        }

        return requiredKeywords;
    }

    public ArgumentDescriptor[] toAnonymousArgumentDescriptors() {
        List<ArgumentDescriptor> descs = new ArrayList<>();

        for (int i = 0; i < preRequired; i++) {
            descs.add(new ArgumentDescriptor(ArgumentType.anonreq));
        }

        for (int i = 0; i < optional; i++) {
            descs.add(new ArgumentDescriptor(ArgumentType.anonopt));
        }

        if (hasRest) {
            descs.add(new ArgumentDescriptor(ArgumentType.anonrest));
        }

        for (int i = 0; i < postRequired; i++) {
            descs.add(new ArgumentDescriptor(ArgumentType.anonreq));
        }

        for (String keyword : keywordArguments) {
            descs.add(new ArgumentDescriptor(ArgumentType.key, keyword));
        }

        if (hasKeywordsRest) {
            descs.add(new ArgumentDescriptor(ArgumentType.anonkeyrest));
        }

        return descs.toArray(ArgumentDescriptor.EMPTY_ARRAY);
    }

    @Override
    public String toString() {
        return "Arity{" +
                "preRequired=" + preRequired +
                ", optional=" + optional +
                ", hasRest=" + hasRest +
                ", postRequired=" + postRequired +
                ", keywordArguments=" + Arrays.toString(keywordArguments) +
                ", hasKeywordsRest=" + hasKeywordsRest +
                '}';
    }
}
