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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ArgumentsDescriptorManager {

    private final Map<NonEmptyArgumentsDescriptor, WeakReference<NonEmptyArgumentsDescriptor>> CANONICAL_NON_EMPTY_DESCRIPTORS = Collections
            .synchronizedMap(new WeakHashMap<>());

    public ArgumentsDescriptor getArgumentsDescriptor(String[] keywordArgumentsNames, int keywordHashIndex,
            boolean keywordHashElidable) {
        assert !(keywordHashIndex == NonEmptyArgumentsDescriptor.NO_KEYWORD_HASH && keywordHashElidable);

        if (keywordArgumentsNames.length == 0 && keywordHashIndex == NonEmptyArgumentsDescriptor.NO_KEYWORD_HASH) {
            return EmptyArgumentsDescriptor.INSTANCE;
        }

        final NonEmptyArgumentsDescriptor reference = new NonEmptyArgumentsDescriptor(keywordArgumentsNames,
                keywordHashIndex, keywordHashElidable);

        final WeakReference<NonEmptyArgumentsDescriptor> canonical = CANONICAL_NON_EMPTY_DESCRIPTORS
                .putIfAbsent(reference, new WeakReference<>(reference));

        if (canonical == null) {
            return reference;
        } else {
            return canonical.get();
        }
    }

}
