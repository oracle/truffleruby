require 'json'

$tests = 0
$failures = 0

def test(program, extra='', shape_expected)
  $tests += 1
  got = `tool/jt.rb graph --json --describe #{program} #{extra} 2>&1`.lines.last.strip

  begin
    decoded = JSON.parse(got, symbolize_names: true)
  rescue JSON::ParseError
    puts "#{program}: error #{got.inspect}"
    $failures += 1
  end

  nodes_got = decoded.delete(:node_count)
  shape_got = decoded

  mismatched_features = shape_expected.keys - shape_got.keys

  mismatched_features += shape_got.keys.select do |actual_key|
    actual_value = shape_got[actual_key]

    if shape_expected.key?(actual_key)
      actual_value == shape_expected[actual_key] ? nil : actual_key
    else
      # Verify the actual shape does not have features that the test did not expect.
      actual_value == false ? nil : actual_key
    end
  end

  if mismatched_features.empty?
    puts "#{program}: ✓"
  else
    puts "#{program}: mismatched features: #{mismatched_features} differ; (expected #{shape_expected}, got #{shape_got})"
    $failures += 1
  end
end

test 'test/truffle/compiler/graphs/args-req.rb', { linear: true }
test 'test/truffle/compiler/graphs/args-opt-set.rb', { linear: true }
test 'test/truffle/compiler/graphs/args-opt-unset.rb', { linear: true}
test 'test/truffle/compiler/graphs/args-rest.rb', { linear: true }

puts "#{$tests} tests, #{$failures} failures"
exit 1 if $failures > 0
