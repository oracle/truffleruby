# Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

abort "USAGE: ruby #{$0} NEW_YEAR" unless ARGV.first
year = Integer(ARGV.first)
new_copyright_year = year

RB_COPYRIGHT = <<-EOS
# Copyright (c) #{new_copyright_year} Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

EOS

JAVA_COPYRIGHT = <<-EOS
/*
 * Copyright (c) #{new_copyright_year} Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
EOS

NEW_COPYRIGHT = {
  '.rb' => RB_COPYRIGHT,
  '.java' => JAVA_COPYRIGHT,
  '.c' => JAVA_COPYRIGHT,
  '.h' => JAVA_COPYRIGHT,
}

EXTENSIONS = %w[.java .rb .c .h .md]

COPYRIGHT = /(?<copyright>Copyright) \(c\) (?<year1>\d{4})(?:(?<sep>, )(?<year2>\d{4}))? Oracle\b/
COPYRIGHT_MARKDOWN = /(?<copyright>copyright) \(c\) (?<year1>\d{4})(?:(?<sep>-)(?<year2>\d{4}))? Oracle\b/

OTHER_COPYRIGHTS = [
  /Copyright \(c\) \d{4}(?:-\d{4})?,? Evan Phoenix/,
  /Copyright \(c\) \d{4} Engine Yard/,
  /Copyright \(c\) \d{4} Akinori MUSHA/, # SHA-2
  /Copyright \(c\) \d{4}-\d{4} The JRuby project/, # io/console
  /Copyright \(C\) \d{4}-\d{4} Wayne Meissner/, # FFI
  /Copyright \(c\) \d{4}, Brian Shirai/, # rubysl-socket
  /Ruby is copyrighted free software by Yukihiro Matsumoto/, # MRI license
  /Copyright(?:::)?\s+\(C\)\s+\d{4}\s+Network Applied Communication Laboratory, Inc\./, # MRI stdlibs: thread, timeout
  /\* BEGIN LICENSE BLOCK \**\s*\n\s*\*\s*Version: EPL 2\.0\/GPL 2\.0\/LGPL 2\.1/,
  /#+\s*BEGIN LICENSE BLOCK\s*#+\s*\n\s*#\s*Version: EPL 2\.0\/GPL 2\.0\/LGPL 2\.1/,
  /This file is part of ruby-ffi\./,
]

truffle_paths = %w[
  lib/cext/include/truffleruby
  lib/cext/preprocess.rb
  lib/truffle
  src
  test/truffle
  tool/generate
  spec/truffle
  LICENCE.md
  README.md
  doc/legal/legal.md
] + [__FILE__]

excludes = %w[
  lib/cext/include/truffleruby/config_
  lib/cext/include/truffleruby/internal
  lib/truffle/date
  lib/truffle/ffi
  lib/truffle/io/console/size.rb
  lib/truffle/pathname
  lib/truffle/securerandom
  src/main/c/bigdecimal
  src/main/c/cext/st.c
  src/main/c/date
  src/main/c/etc
  src/main/c/io-console
  src/main/c/nkf
  src/main/c/openssl
  src/main/c/psych
  src/main/c/rbconfig-sizeof
  src/main/c/ripper
  src/main/c/syslog
  src/main/c/zlib
  spec/truffle/fixtures/hello-world
  test/truffle/pack-real-usage.rb
  test/truffle/cexts
  test/truffle/ecosystem
  test/truffle/integration/backtraces/fixtures
]

excluded_files = %w[
  extconf.rb
]

truffle_paths.each do |path|
  next if %w[tool/generate].include?(path)
  abort "WARNING: incorrect path #{path}" unless File.exist? path
end

paths = `git ls-files`.lines(chomp: true)
paths = paths.select { |path|
  EXTENSIONS.include?(File.extname(path)) &&
    truffle_paths.any? { |prefix| path.start_with? prefix } &&
    excludes.none? { |prefix| path.start_with? prefix } &&
    !excluded_files.include?(File.basename(path)) &&
    File.exist?(path) &&
    File.readlines(path).size > 2
}

paths.each do |file|
  ext = File.extname(file)
  md = ext == '.md'
  header = File.read(file, *(400 unless md))

  copyright_regexp = md ? COPYRIGHT_MARKDOWN : COPYRIGHT

  unless copyright_regexp =~ header
    next if md
    if OTHER_COPYRIGHTS.none? { |copyright| copyright =~ header }
      puts "Adding copyright in #{file}"
      File.write(file, NEW_COPYRIGHT[ext]+File.read(file))
    end
    next
  end

  copyright, year1, sep, year2 = $~[:copyright], $~[:year1], $~[:sep], $~[:year2]
  year1 = Integer(year1)
  year2 = Integer(year2 || year1)

  if year > year2
    contents = File.read(file)
    years = "#{year1}#{sep}#{year}"
    contents.sub!(copyright_regexp, "#{copyright} (c) #{years} Oracle")
    File.write(file, contents)

    puts "Updated year in #{file}"
  end
end
