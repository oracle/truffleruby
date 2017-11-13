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
import jnr.posix.Passwd;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface TrufflePosix {

    Passwd getpwnam(String which);
    int errno();
    long sysconf(Sysconf name);
    Times times();
    int posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends SpawnAttribute> spawnAttributes,
            Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp);
    int read(int fd, byte[] buf, int n);
    int write(int fd, ByteBuffer buf, int n);
    int read(int fd, ByteBuffer buf, int n);
    String getcwd();
    String nl_langinfo(int item);

}
