def glob(pattern)
  files = Dir.glob(pattern)
  raise "no libraries found with #{pattern}" if files.empty?
  files
end

stdlibs = glob('lib/mri/*.{rb,su}').map { |file|
  File.basename(file, '.*')
}

glob('lib/truffle/*.rb').map { |file|
  lib = File.basename(file, '.*')
  stdlibs << lib unless lib.end_with? '-stubs'
}

glob('lib/mri/net/*.rb').map { |file| File.basename(file, '.*') }.each { |file|
  stdlibs << "net/#{file}"
}

glob('lib/rubysl/rubysl-*') { |dir|
  stdlibs << File.basename(dir).sub(/^rubysl-/, '')
}

stdlibs += %w[json]

ignore = %w[continuation debug mathn profile psych_jars shell]

stdlibs -= ignore

stdlibs.uniq!

stdlibs.each { |lib| require lib }

puts 3 * 4
