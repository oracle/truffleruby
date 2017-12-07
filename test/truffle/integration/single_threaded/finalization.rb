x = Object.new

ObjectSpace.define_finalizer x, -> { exit! 0 }

x = nil

GC.start

sleep 1

y = Object.new

# Defining a new finalizer should cause the old finalizer to run in this thread

ObjectSpace.define_finalizer y, -> { exit! 1 }

exit! 1
