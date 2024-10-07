exclude :test_glob_cases, "Dir.glob should return the filename with actual cases on the filesystem." if RUBY_PLATFORM.include?('darwin')
exclude :test_glob_ignore_casefold_invalid_encoding, "Errno::EILSEQ: Invalid or incomplete multibyte or wide character - /private/var/folders/gr/3kff5w4s7779h6ycnt4gxfgm0000gn/T/__test_dir__20240910-84824-6ncred/�a123" if RUBY_PLATFORM.include?('darwin')
exclude :test_instance_chdir, "cannot return the original directory"
