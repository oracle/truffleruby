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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.truffleruby.parser.ArgumentDescriptor;
import org.truffleruby.parser.ArgumentType;

public class Arity {

    public static final String[] NO_KEYWORDS = new String[]{};
    public static final Arity NO_ARGUMENTS = new Arity(0, 0, false);
    public static final Arity ONE_REQUIRED = new Arity(1, 0, false);

    private final int preRequired;
    private final int optional;
    private final boolean hasRest;
    private final int postRequired;
    private final boolean hasKeywordsRest;
    private final String[] keywordArguments;
    private final int arityNumber;

    public Arity(int preRequired, int optional, boolean hasRest) {
        this(preRequired, optional, hasRest, 0, NO_KEYWORDS, false);
    }


    public Arity(int preRequired, int optional, boolean hasRest, int postRequired, String[] keywordArguments, boolean hasKeywordsRest) {
        this.preRequired = preRequired;
        this.optional = optional;
        this.hasRest = hasRest;
        this.postRequired = postRequired;
        this.keywordArguments = keywordArguments;
        this.hasKeywordsRest = hasKeywordsRest;
        this.arityNumber = computeArityNumber();

        assert keywordArguments != null && preRequired >= 0 && optional >= 0 && postRequired >= 0 : toString();
    }

    public Arity withRest(boolean hasRest) {
        return new Arity(preRequired, optional, hasRest, postRequired, keywordArguments, hasKeywordsRest);
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

    private int computeArityNumber() {
        int count = getRequired();

        if (acceptsKeywords()) {
            count++;
        }

        if (optional > 0 || hasRest) {
            count = -count - 1;
        }

        return count;
    }

    public int getArityNumber() {
        return arityNumber;
    }

    public String[] getKeywordArguments() {
        return keywordArguments;
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
