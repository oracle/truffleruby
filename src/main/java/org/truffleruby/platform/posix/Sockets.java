/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.posix;

import jnr.ffi.Pointer;
import jnr.posix.Timeval;

public interface Sockets {

    /*
     * int
     * accept(int socket, struct sockaddr *restrict address,
     *        socklen_t *restrict address_len);
     */

    int accept(int socket, Pointer address, int[] addressLength);

    /*
     * int
     * select(int nfds, fd_set *restrict readfds, fd_set *restrict writefds,
     *        fd_set *restrict errorfds, struct timeval *restrict timeout);
     */

    int select(int nfds, Pointer readfds, Pointer writefds, Pointer errorfds, Timeval timeout);

}
