/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.posix;

import jnr.constants.platform.Sysconf;
import jnr.posix.Times;

public interface TrufflePosix {

    int errno();
    long sysconf(Sysconf name);
    Times times();
    String getcwd();
    String nl_langinfo(int item);

}
