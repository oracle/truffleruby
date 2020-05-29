# A fixed-workload harness for OptCarrot with minimal dependencies.
# This can be useful to debug or tune performance.
#
# Run with:
# jt build --env jvm-ce
# jt -u jvm-ce ruby bench/optcarrot/fixed-workload.rb

require_relative 'lib/optcarrot'
require 'benchmark'

number_of_iterations = 5000

rom = File.expand_path('../examples/Lan_Master.nes', __FILE__)
nes = Optcarrot::NES.new ['--headless', rom]
nes.reset

total = Benchmark.realtime do
  number_of_iterations.times do
    t = Benchmark.realtime { nes.step }
    Truffle::Debug.print 1/t
  end
end

Truffle::Debug.print "Total: #{total}"
