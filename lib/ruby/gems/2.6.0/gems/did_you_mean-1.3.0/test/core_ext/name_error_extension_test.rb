require 'test_helper'

class NameErrorExtensionTest < Minitest::Test
  SPELL_CHECKERS = DidYouMean::SPELL_CHECKERS

  class TestSpellChecker
    def initialize(*); end
    def corrections; ["does_exist"]; end
  end

  def setup
    @org, SPELL_CHECKERS['NameError'] = SPELL_CHECKERS['NameError'], TestSpellChecker

    @error = assert_raises(NameError){ doesnt_exist }
  end

  def teardown
    SPELL_CHECKERS['NameError'] = @org
  end

  def test_message
    message = <<~MESSAGE.chomp
      undefined local variable or method `doesnt_exist' for #{to_s}
      Did you mean?  does_exist
    MESSAGE

    assert_equal message, @error.to_s
    assert_equal message, @error.message
  end

  def test_to_s_does_not_make_disruptive_changes_to_error_message
    error = assert_raises(NameError) do
      raise NameError, "uninitialized constant Object"
    end

    error.to_s
    assert_equal 1, error.to_s.scan("Did you mean?").count
  end

  def test_correctable_error_objects_are_dumpable
   error = begin
             File.open('./tmp/.keep').sizee
           rescue NoMethodError => e
             e
           end

   error.to_s

   assert_equal "undefined method `sizee' for #<File:./tmp/.keep>", Marshal.load(Marshal.dump(error)).original_message
  end
end
