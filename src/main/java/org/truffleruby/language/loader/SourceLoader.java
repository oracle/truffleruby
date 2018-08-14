/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.SourceSection;

import org.truffleruby.RubyContext;

public class SourceLoader {

    private final RubyContext context;

    public SourceLoader(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public String fileLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = context.getPath(section.getSource());

            if (section.isAvailable()) {
                return path + ":" + section.getStartLine();
            } else {
                return path;
            }
        }
    }

    public boolean isInternal(String canonicalPath) {
        if (canonicalPath.startsWith(context.getCoreLibrary().getCoreLoadPath())) {
            return context.getOptions().CORE_AS_INTERNAL;
        }

        if (canonicalPath.startsWith(context.getRubyHome() + "/lib/") &&
                !canonicalPath.startsWith(context.getRubyHome() + "/lib/ruby/gems/")) {
            return context.getOptions().STDLIB_AS_INTERNAL;
        }

        return false;
    }

}
