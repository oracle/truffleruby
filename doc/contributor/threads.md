# Threads

Each Ruby fiber is implemented as a Java thread. In Ruby, a thread has one or
more fibers. A Ruby thread has a root fiber which is created even if you aren't
explicitly using fibers, so you are always running in a fiber.

Fibers work by message passing. A fiber which has yielded waits for messages
from other fibers to continue.

The safepoint manager maintains a list of Java threads which implement fibers.

## Service threads

The finalization service has a Ruby thread running object finalizers.

## Thread shutdown

When a thread shuts down (exits naturally or is killed), all the fibers and
their Java threads need to be shut down as well. This is done by sending an exit
message.

## Context shutdown

When `main` returns in Java, the VM waits for all non-daemon threads (we do not
create any daemon threads) to exit before the VM exits. However TruffleRuby
exits through `System.exit` rather than returning from `main`, which kills all
other Java threads immediately.

When the main thread in Ruby finishes, all Ruby threads and fibers are killed.

To shutdown all Ruby threads and fibers on exit, a safepoint action is run to
call `shutdown` on each thread, executing on the thread itself.

After shutdown, the safepoint manager asserts that all Java threads have exited.
