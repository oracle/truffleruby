exclude :test_exception_in_finalizer_procs, "transient"
exclude :test_gc_parameter, "slow: 32.16s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor: (16 vCPUs)"
exclude :test_interrupt_in_finalizer, "spurious; TypeError: no implicit conversion of nil into Integer"
