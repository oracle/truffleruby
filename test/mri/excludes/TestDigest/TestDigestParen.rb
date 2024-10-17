exclude :test_no_lib_no_def, "[LoadError] exception expected, not #<NameError: uninitialized constant Digest::Nodef>."
exclude :test_race, "Exception raised: <#<NameError: uninitialized constant Digest::Foo>>"
exclude :test_race_mixed, "Exception raised: <#<NameError: uninitialized constant Digest::Foo>>"
