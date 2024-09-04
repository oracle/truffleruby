exclude :test_getsockopt, "[IOError] exception expected, not #<Errno::EBADF: Bad file descriptor - Unable to get socket option>."
exclude :test_read_write_nonblock, "[Feature #13362]."
exclude :test_setsockopt, "Failed assertion, no message given."
exclude :test_write_nonblock_buffered, "<:wait_readable> expected but was <\"h\">."
