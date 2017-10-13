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
