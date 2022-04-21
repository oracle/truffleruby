# frozen_string_literal: true

$LOAD_PATH.unshift "#{__dir__}/lib"
require 'liquid'
require_relative 'performance/tests/sm-de/data'

template = Liquid::Template.new.parse(File.read("#{__dir__}/performance/tests/sm-de/papers.liquid"))
expected_result = File.read("#{__dir__}/performance/tests/sm-de/result.html")
data = {'papers'=> get_bib_data}

dev_null = File.open('/dev/null', 'w')

benchmark do
  result = template.render!(data)
  dev_null.write result
  raise StandardError, "Incorrect rendering result" unless result == expected_result
end
