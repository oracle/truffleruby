# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

$started = false

t = Thread.new {
  $started = true
  Truffle::Debug.dead_block
}

# Another thread, but this one goes to the safepoint
Thread.new {
  sleep
}

# Let the other thread start
Thread.pass until $started
sleep 1

# Uses the SafepointManager
t.kill
