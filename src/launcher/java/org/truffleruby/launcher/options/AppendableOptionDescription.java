/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher.options;

public abstract class AppendableOptionDescription<T> extends OptionDescription<T> {

    public AppendableOptionDescription(String name, String description, String[] rubyOptions) {
        super(name, description, rubyOptions);
    }

    abstract String append(String currentValues, String newElement);

}
