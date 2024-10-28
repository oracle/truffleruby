require_relative '../test/mri/tests/tool/test/init'

ARGV.each do |test_file|
  require File.expand_path(test_file).chomp('.rb')
end

if defined?(Test::Unit::Runner)
  Test::Unit::Runner.class_variable_set(:@@stop_auto_run, true)
elsif defined?(Test::Unit::AutoRunner)
  Test::Unit::AutoRunner.need_auto_run = false
end

def descendants(klass)
  klass.subclasses + klass.subclasses.flat_map { |c| descendants(c) }
end

puts descendants(Test::Unit::TestCase)
