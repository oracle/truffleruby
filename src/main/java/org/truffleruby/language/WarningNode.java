/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;


/** Warns only if $VERBOSE is true. Corresponds to Kernel#warn(message, uplevel: 1) if $VERBOSE, but in Java with a
 * given SourceSection. */
public class WarningNode extends WarnNode {

    @Override
    public boolean shouldWarn() {
        final Object verbosity = readVerboseNode.execute();
        return verbosity == Boolean.TRUE;
    }

    public static class UncachedWarningNode extends UncachedWarnNode {

        public static final UncachedWarningNode INSTANCE = new UncachedWarningNode();

        private UncachedWarningNode() {
        }

        @Override
        public boolean shouldWarn() {
            return coreLibrary().isVerbose();
        }
    }

}
