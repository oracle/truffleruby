/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import java.util.Arrays;

public class NonEmptyArgumentsDescriptor extends ArgumentsDescriptor {

    public static final int NO_KEYWORD_HASH = -1;

    private final String[] keywordArgumentNames;
    private final int keywordHashIndex;
    private final boolean keywordHashElidable;

    public NonEmptyArgumentsDescriptor(
            String[] keywordArgumentNames,
            int keywordHashIndex,
            boolean keywordHashElidable) {
        this.keywordArgumentNames = keywordArgumentNames;
        this.keywordHashIndex = keywordHashIndex;
        this.keywordHashElidable = keywordHashElidable;
    }

    public String[] getKeywordArgumentNames() {
        return keywordArgumentNames;
    }

    public int getKeywordHashIndex() {
        return keywordHashIndex;
    }

    public boolean isKeywordHashElidable() {
        return keywordHashElidable;
    }

    public boolean equals(Object other) {
        if (other instanceof NonEmptyArgumentsDescriptor) {
            final NonEmptyArgumentsDescriptor nonEmpty = (NonEmptyArgumentsDescriptor) other;
            return Arrays.equals(keywordArgumentNames, nonEmpty.keywordArgumentNames) &&
                    keywordHashIndex == nonEmpty.keywordHashIndex &&
                    keywordHashElidable == nonEmpty.keywordHashElidable;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(keywordArgumentNames) ^ (keywordHashIndex << 32) ^
                (keywordHashElidable ? (1 << 24) : (1 << 12));
    }

}
