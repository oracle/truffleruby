require 'minitest/autorun'
require 'minitest/unit'
require 'did_you_mean'

module DidYouMean::TestHelper
  def assert_correction(expected, array)
    assert_equal [expected], array, "Expected #{array.inspect} to only include #{expected.inspect}"
  end
end

MiniTest::Test.include(DidYouMean::TestHelper)
