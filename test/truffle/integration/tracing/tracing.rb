# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

$trace = []

$trace_proc = proc { |*args|
  bind = args[4]
  # Capture the current values of variables at this point in time.
  capture = Hash[args[4].local_variables.sort.map { |v|
    [v, args[4].local_variable_get(v)]
  }]
  args[4] = capture
  $trace << args
}

def check(file)
  expected = nil
  
  File.open('test/truffle/integration/tracing/' + file) do |f|
    expected = f.each_line.map { |line| eval(line) }
  end
  
  actual = $trace
  
  #actual.each do |a|
  #  a[4] = expand_binding(a[4])
  #  p a
  #end
  
  empty_binding = {}
  
  while actual.size < expected.size
    actual.push ['missing', 'missing', :missing, :missing, empty_binding, :missing]
  end
  
  while expected.size < actual.size
    expected.push ['missing', 'missing', :missing, :missing, empty_binding, :missing]
  end
  
  success = true
  trace_event = 1
  
  expected.zip(actual).each do |e, a|
    unless a[0] == e[0]
      puts "Event #{trace_event}\n    Expected #{e[0].inspect}\n    actually #{a[0].inspect}"
      success = false
    end
    
    unless a[1].end_with?(e[1])
      puts "Event #{trace_event}\n    Expected #{e[1].inspect}\n    actually #{a[1].inspect}"
      success = false
    end
  
    unless a[2] == e[2]
      puts "Event #{trace_event}\n    Expected #{e[2].inspect}\n    actually #{a[2].inspect}"
      success = false
    end
  
    unless a[3] == e[3]
      puts "Event #{trace_event}\n    Expected #{e[3].inspect}\n    actually #{a[3].inspect}"
      success = false
    end
    
    unless a[4] == e[4]
      puts "Event #{trace_event}\n    Expected #{e[4].inspect}\n    actually #{a[4].inspect}"
      success = false
    end
  
    unless a[5] == e[5]
      puts "Event #{trace_event}\n    Expected #{e[5].inspect}\n    actually #{a[5].inspect}"
      success = false
    end
    trace_event += 1
  end
  
  unless success
    exit 1
  end
end
