# frozen_string_literal: false
require 'test/unit'
require "c/string"

class Test_TooManyDummyEncodings < Test::Unit::TestCase
  def test_exceed_encoding_table_size
    assert_separately([], "#{<<~"begin;"}\n#{<<~'end;'}")
    begin;
      require "c/string"
      assert_raise_with_message(EncodingError, /too many encoding/) do
        1_000.times{|i| Bug::String.rb_define_dummy_encoding("R_#{i}") } # now 256 entries
      end
    end;
  end
end
