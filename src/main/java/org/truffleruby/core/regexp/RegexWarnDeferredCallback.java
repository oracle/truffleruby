/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import org.joni.WarnCallback;
import org.truffleruby.parser.RubyDeferredWarnings;

public class RegexWarnDeferredCallback implements WarnCallback {

    private final RubyDeferredWarnings warnings;

    public RegexWarnDeferredCallback(RubyDeferredWarnings warnings) {
        this.warnings = warnings;
    }

    @Override
    public void warn(String message) {
        // RegexWarnCallback uses isVerbose check to limit to verbose only
        warnings.warning(message);
    }

}
