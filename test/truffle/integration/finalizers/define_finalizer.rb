# Getting MRI to run a finalizer for a test seems problematic, due to the
# conservative GC and the separate C and Ruby stacks.

loop do
  ObjectSpace.define_finalizer Object.new, proc {
    puts 'finalized!'
    exit! 0
  }
end
