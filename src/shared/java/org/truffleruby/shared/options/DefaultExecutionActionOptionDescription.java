/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import org.graalvm.options.OptionCategory;

public class DefaultExecutionActionOptionDescription extends EnumOptionDescription<DefaultExecutionAction> {

    DefaultExecutionActionOptionDescription(OptionCategory category, String name, String description, String[] rubyOptions, DefaultExecutionAction defaultValue) {
        super(category, name, description, rubyOptions, defaultValue, DefaultExecutionAction.class);
    }

}
