# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

def glob(pattern)
  files = Dir.glob(pattern)
  raise "no libraries found with #{pattern}" if files.empty?
  files
end

stdlibs = glob('lib/mri/*.{rb,su}').map { |file|
  File.basename(file, '.*')
}

glob('lib/truffle/*.rb').map { |file|
  stdlibs << File.basename(file, '.*')
}

glob('lib/mri/net/*.rb').map { |file| File.basename(file, '.*') }.each { |file|
  stdlibs << "net/#{file}"
}

stdlibs += %w[json]

# 'continuation' warns on being required, as MRI
ignore = %w[
  continuation
  dbm
  gdbm
  sdbm
  debug
  profile
  profiler
  pty
  ripper
  shell
  win32
  win32ole
]

stdlibs -= ignore

stdlibs.uniq!

stdlibs.each { |lib| require lib }

puts 3 * 4
