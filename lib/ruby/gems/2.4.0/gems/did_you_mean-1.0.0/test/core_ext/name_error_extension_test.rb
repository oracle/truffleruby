require 'test_helper'

class NameErrorExtensionTest < Minitest::Test
  SPELL_CHECKERS = DidYouMean::SPELL_CHECKERS

  class TestSpellChecker
    def initialize(*); end
    def corrections; ["Y U SO SLOW?"]; end
  end

  def setup
    @org, SPELL_CHECKERS['NameError'] = SPELL_CHECKERS['NameError'], TestSpellChecker

    @error = assert_raises(NameError){ doesnt_exist }
  end

  def teardown
    SPELL_CHECKERS['NameError'] = @org
  end

  def test_message_provides_original_message
    assert_match "undefined local variable or method", @error.to_s
  end

  def test_message
    assert_match "Did you mean?  Y U SO SLOW?", @error.to_s
    assert_match "Did you mean?  Y U SO SLOW?", @error.message
  end

  def test_to_s_does_not_make_disruptive_changes_to_error_message
    error = assert_raises(NameError) do
      raise NameError, "uninitialized constant Object"
    end

    assert_equal 1, error.to_s.scan("Did you mean?").count
  end
end

class IgnoreCallersTest < Minitest::Test
  SPELL_CHECKERS = DidYouMean::SPELL_CHECKERS

  class Boomer
    def initialize(*)
      raise Exception, "spell checker was created when it shouldn't!"
    end
  end

  def setup
    @org, SPELL_CHECKERS['NameError'] = SPELL_CHECKERS['NameError'], Boomer
    DidYouMean::IGNORED_CALLERS << /( |`)do_not_correct_typo'/

    @error = assert_raises(NameError){ doesnt_exist }
  end

  def teardown
    SPELL_CHECKERS['NameError'] = @org
    DidYouMean::IGNORED_CALLERS.clear
  end

  def test_ignore
    assert_nothing_raised { do_not_correct_typo }
  end

  private

  def do_not_correct_typo; @error.message end

  def assert_nothing_raised
    yield
  end
end
