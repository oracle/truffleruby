exclude :test_message_encoding, "Expected Exception(NoMethodError) was raised, but the message doesn't match. Expected /undefined\\ method\\ `☄'\\ for\\ an\\ instance\\ of\\ String/ to match \"undefined method `☄' for \\\"☀\\\":String\"."
exclude :test_new_name_args_priv, "NoMethodError: undefined method `private_call?' for #<NoMethodError: Message>"
exclude :test_new_receiver, "ArgumentError: wrong number of arguments (given 2, expected 0..1)"
