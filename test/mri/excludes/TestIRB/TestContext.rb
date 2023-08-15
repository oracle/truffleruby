exclude :test_echo_on_assignment, "<\"=> 1\\n\" + \"=> 2\\n\" + \"=> 3\\n\" + \"=> 4\\n\"> expected but was"
exclude :test_omit_multiline_on_assignment, "<\"=> \\n\" +"
exclude :test_omit_on_assignment, "<\"=> \\n\" +"
exclude :test_assignment_expression_with_local_variable, "a /1;x=1#/: should be an assignment expression"
