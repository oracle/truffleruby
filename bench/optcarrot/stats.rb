# A fixed-workload harness for OptCarrot with minimal dependencies.
# This can be useful to debug or tune performance.
#
# Run with:
# jt build --env jvm-ce
# jt -u jvm-ce ruby bench/optcarrot/stats.rb

require_relative 'lib/optcarrot'
require 'benchmark'

number_of_iterations = 5000

rom = File.expand_path('../examples/Lan_Master.nes', __FILE__)
nes = Optcarrot::NES.new ['--headless', rom]
nes.reset

results = nil

total = Benchmark.realtime do
  results = (1..number_of_iterations).map do
    t = Benchmark.realtime { nes.step }
    1 / t
  end
end


max = results.max
mean = results.reduce(:+) / results.size
sorted = results.sort
median = sorted[results.size / 2]
q1 = sorted[results.size / 4]
q3 = sorted[results.size / 2 + results.size / 4]

f = '%.1f'

puts "q1: #{f % q1}, median: #{f % median}, q3: #{f % q3}, avg: #{f % mean}, max: #{f % max}, total: #{f % total}s"
