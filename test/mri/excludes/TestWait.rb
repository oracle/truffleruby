exclude :"test_wait_for_invalid_fd[To redirect Truffle log output to a file use one of the following options", "* '--log.file=<path>' if the option is passed using a guest language launcher."
exclude :test_wait_for_invalid_fd, "[Errno::EBADF] exception expected, not #<NameError: uninitialized constant IO::WRITABLE"
exclude :test_wait_for_valid_fd, "NameError: uninitialized constant IO::WRITABLE"
exclude :test_wait_for_closed_pipe, "NameError: uninitialized constant IO::READABLE"
