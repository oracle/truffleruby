require_relative '../test/mri/tests/tool/test/init'

filename = ARGV[0]

def descendants(klass)
  klass.subclasses + klass.subclasses.flat_map { |c| descendants(c) }
end

require File.expand_path(filename).chomp('.rb')

if defined?(Test::Unit::Runner)
  Test::Unit::Runner.class_variable_set(:@@stop_auto_run, true)
elsif defined?(Test::Unit::AutoRunner)
  Test::Unit::AutoRunner.need_auto_run = false
end

puts descendants(Test::Unit::TestCase)
