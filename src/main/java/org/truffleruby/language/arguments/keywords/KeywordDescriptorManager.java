/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments.keywords;

import org.truffleruby.RubyLanguage;

import java.util.concurrent.ConcurrentHashMap;

public class KeywordDescriptorManager {

    public static final KeywordDescriptorManager INSTANCE = new KeywordDescriptorManager();

    private final ConcurrentHashMap<NonEmptyKeywordDescriptor, NonEmptyKeywordDescriptor> CANONICAL_DESCRIPTORS = new ConcurrentHashMap<>();

    public NonEmptyKeywordDescriptor get(RubyLanguage language, String[] keywords, boolean alsoSplat, int hashIndex) {
        final NonEmptyKeywordDescriptor reference = new NonEmptyKeywordDescriptor(language, keywords, alsoSplat,
                hashIndex);
        final NonEmptyKeywordDescriptor canonical = CANONICAL_DESCRIPTORS.putIfAbsent(reference, reference);

        if (canonical == null) {
            return reference;
        } else {
            return canonical;
        }
    }

}
