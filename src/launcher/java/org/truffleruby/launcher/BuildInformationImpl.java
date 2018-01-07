/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher;

public class BuildInformationImpl implements BuildInformation {

    public static final BuildInformation INSTANCE = new BuildInformationImpl();

    @Override
    public String getRevision() {
        return "f00fc3fc33";
    }

    @Override
    public String getCompileDate() {
        return "2018-01-07";
    }

}
