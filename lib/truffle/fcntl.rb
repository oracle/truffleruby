# Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Fcntl
  F_DUPFD = Truffle::Config['rbx.platform.fcntl.F_DUPFD']
  F_GETFD = Truffle::Config['rbx.platform.fcntl.F_GETFD']
  F_GETLK = Truffle::Config['rbx.platform.fcntl.F_GETLK']
  F_SETFD = Truffle::Config['rbx.platform.fcntl.F_SETFD']
  F_GETFL = Truffle::Config['rbx.platform.fcntl.F_GETFL']
  F_SETFL = Truffle::Config['rbx.platform.fcntl.F_SETFL']
  F_SETLK = Truffle::Config['rbx.platform.fcntl.F_SETLK']
  F_SETLKW = Truffle::Config['rbx.platform.fcntl.F_SETLKW']
  FD_CLOEXEC = Truffle::Config['rbx.platform.fcntl.FD_CLOEXEC']
  F_RDLCK = Truffle::Config['rbx.platform.fcntl.F_RDLCK']
  F_UNLCK = Truffle::Config['rbx.platform.fcntl.F_UNLCK']
  F_WRLCK = Truffle::Config['rbx.platform.fcntl.F_WRLCK']
  O_CREAT = Truffle::Config['rbx.platform.file.O_CREAT']
  O_EXCL = Truffle::Config['rbx.platform.file.O_EXCL']
  O_NOCTTY = Truffle::Config['rbx.platform.file.O_NOCTTY']
  O_TRUNC = Truffle::Config['rbx.platform.file.O_TRUNC']
  O_APPEND = Truffle::Config['rbx.platform.file.O_APPEND']
  O_NONBLOCK = Truffle::Config['rbx.platform.file.O_NONBLOCK']
  O_NDELAY = Truffle::Config['rbx.platform.file.O_NDELAY']
  O_RDONLY = Truffle::Config['rbx.platform.file.O_RDONLY']
  O_RDWR = Truffle::Config['rbx.platform.file.O_RDWR']
  O_WRONLY = Truffle::Config['rbx.platform.file.O_WRONLY']
  O_ACCMODE = Truffle::Config['rbx.platform.file.O_ACCMODE']
end
