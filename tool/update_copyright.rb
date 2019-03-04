# Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'rugged'

RB_COPYRIGHT = <<-EOS
# Copyright (c) #{Time.now.year} Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

EOS

JAVA_COPYRIGHT = <<-EOS
/*
 * Copyright (c) #{Time.now.year} Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
EOS

NEW_COPYRIGHT = {
  '.rb' => RB_COPYRIGHT,
  '.java' => JAVA_COPYRIGHT
}

EXTENSIONS = %w[.java .rb]

COPYRIGHT = /Copyright \(c\) (?<year1>\d{4})(?:, (?<year2>\d{4}))? Oracle\b/

OTHER_COPYRIGHTS = [
  /Copyright \(c\) \d{4} Software Architecture Group, Hasso Plattner Institute/,
  /Copyright \(c\) \d{4}(?:-\d{4})?,? Evan Phoenix/,
  /Copyright \(c\) \d{4} Engine Yard/,
  /Copyright \(c\) \d{4} Akinori MUSHA/, # SHA-2
  /Copyright \(c\) \d{4}-\d{4} The JRuby project/, # io/console
  /Copyright \(C\) \d{4}-\d{4} Wayne Meissner/, # FFI
  /Copyright \(c\) \d{4}, Brian Shirai/, # rubysl-socket
  /Ruby is copyrighted free software by Yukihiro Matsumoto/, # MRI license
  #Copyright (C) 2000  Network Applied Communication Laboratory, Inc.
  /Copyright(?:::)?\s+\(C\)\s+\d{4}\s+Network Applied Communication Laboratory, Inc\./, # MRI stdlibs: thread, timeout
  /\* BEGIN LICENSE BLOCK \**\s*\n\s*\*\s*Version: EPL 1\.0\/GPL 2\.0\/LGPL 2\.1/,
  /#+\s*BEGIN LICENSE BLOCK\s*#+\s*\n\s*#\s*Version: EPL 1\.0\/GPL 2\.0\/LGPL 2\.1/,
]

truffle_paths = %w[
  lib/cext
  lib/truffle
  src
  test/truffle
  spec/truffle
] + [__FILE__]

excludes = %w[
  lib/truffle/date
  lib/truffle/pathname
  lib/truffle/securerandom
  test/truffle/pack-real-usage.rb
  test/truffle/cexts
  test/truffle/ecosystem
  src/main/c/openssl
]

excluded_files = %w[
  extconf.rb
]

truffle_paths.each do |path|
  puts "WARNING: incorrect path #{path}" unless File.exist? path
end

abort "USAGE: ruby #{$0} DAYS" unless ARGV.first
days = Integer(ARGV.first)
since = Time.now - days * 24 * 3600

puts "Fixing copyright years for commits in the last #{days} days"

now_year = Time.now.year # Hack this with previous year if needed
abort "Too far back in time: #{since} but we are in #{now_year}" unless since.year == now_year

repo = Rugged::Repository.new('.')

head_commit = repo.head.target
first_commit = nil

repo.walk(head_commit, Rugged::SORT_DATE) { |commit|
  break if commit.time < since
  first_commit = commit
}

abort "No commit in that range" unless first_commit

diff = first_commit.diff(head_commit)

paths = diff.each_delta.to_a.map { |delta|
  delta.new_file[:path]
}.select { |path|
  EXTENSIONS.include?(File.extname(path)) &&
    truffle_paths.any? { |prefix| path.start_with? prefix } &&
    excludes.none? { |prefix| path.start_with? prefix } &&
    !excluded_files.include?(File.basename(path)) &&
    File.exist?(path) &&
    File.readlines(path).size > 2
}

paths.each do |file|
  header = File.read(file, 400)

  unless COPYRIGHT =~ header
    if OTHER_COPYRIGHTS.none? { |copyright| copyright =~ header }
      puts "Adding copyright in #{file}"
      File.write(file, NEW_COPYRIGHT[File.extname(file)]+File.read(file))
    end
    next
  end

  year1, year2 = $~[:year1], $~[:year2]
  year1 = Integer(year1)
  year2 = Integer(year2 || year1)

  if now_year > year2
    contents = File.read(file)
    years = "#{year1}, #{now_year}"
    contents.sub!(COPYRIGHT, "Copyright (c) #{years} Oracle")
    File.write(file, contents)

    puts "Updated year in #{file}"
  end
end
