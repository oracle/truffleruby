exclude :test_deferred_free, "| <internal:core> core/kernel.rb:269:in `require': cannot load such file -- c/typeddata (LoadError)"
exclude :test_wrong_argtype, "Expected Exception(TypeError) was raised, but the message doesn't match. <\"wrong argument type false (expected typed_data)\"> expected but was <\"wrong argument type FalseClass (expected T_DATA)\">."
