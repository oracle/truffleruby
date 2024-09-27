exclude :test_autoload_parallel_race, "slow"
exclude :test_autoload_parent_namespace, "expected: /\\/some_const.rb to define SomeNamespace::SomeConst but it didn't/"
exclude :test_source_location, "<[\"-\", 8]> expected but was <[]>."
exclude :test_threaded_accessing_inner_constant, "spurious; <#<NameError: uninitialized constant AutoloadTest::X>>"
