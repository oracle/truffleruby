# Getting MRI to run a finalizer for a test seems problematic, due to the
# conservative GC and the separate C and Ruby stacks.

1_000_000.times do
  object = Object.new
  
  ObjectSpace.define_finalizer object, proc {
    puts 'finalized!'
    exit! 1
  }
  
  ObjectSpace.undefine_finalizer object
end
