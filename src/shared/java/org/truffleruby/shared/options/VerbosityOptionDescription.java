/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import org.graalvm.options.OptionCategory;

public class VerbosityOptionDescription extends EnumOptionDescription<Verbosity> {

    VerbosityOptionDescription(OptionCategory category, String name, String description, String[] rubyOptions, Verbosity defaultValue) {
        super(category, name, description, rubyOptions, defaultValue, Verbosity.class);
    }

}
