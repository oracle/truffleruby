/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.parser.parser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to suppress <a href="http://findbugs.sourceforge.net">FindBugs</a> warnings.
 */
@Retention(RetentionPolicy.CLASS)
@interface SuppressFBWarnings {
    /**
     * The set of FindBugs
     * <a href="http://findbugs.sourceforge.net/bugDescriptions.html">warnings</a> that are to be
     * suppressed in annotated element. The value can be a bug category, kind or pattern.
     */
    String[] value();
}
