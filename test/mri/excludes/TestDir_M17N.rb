exclude :test_filename_as_bytes_extutf8, "Exception raised: <#<Errno::ENOENT: No such file or directory - ���>>" if RUBY_PLATFORM.include?('darwin')
exclude :test_filename_bytes_euc_jp, "Errno::EILSEQ: Invalid or incomplete multibyte or wide character - ��" if RUBY_PLATFORM.include?('darwin')
exclude :test_filename_euc_jp, "Errno::EILSEQ: Invalid or incomplete multibyte or wide character - ��" if RUBY_PLATFORM.include?('darwin')
exclude :test_filename_ext_euc_jp_and_int_utf_8, "Errno::EILSEQ: Invalid or incomplete multibyte or wide character - ��" if RUBY_PLATFORM.include?('darwin')
exclude :test_filename_extutf8_inteucjp_representable, "Exception raised: <#<Errno::ENOENT: No such file or directory - ��>>"
exclude :test_filename_extutf8_inteucjp_unrepresentable, "Errno::ENOENT: No such file or directory - ��"
exclude :test_filename_extutf8_invalid, "NameError: uninitialized constant TestDir_M17N::Bug"
exclude :test_glob_encoding, "NameError: uninitialized constant TestDir_M17N::Bug"
exclude :test_glob_warning_match_all, "expected: /ΑΒΓΔΕ/"
exclude :test_glob_warning_match_dir, "expected: /ΑΒΓΔΕ/"
exclude :test_glob_warning_opendir, "expected: /ΑΒΓΔΕ/"
exclude :test_inspect_nonascii, "Errno::EILSEQ: Invalid or incomplete multibyte or wide character - ��" if RUBY_PLATFORM.include?('darwin')
