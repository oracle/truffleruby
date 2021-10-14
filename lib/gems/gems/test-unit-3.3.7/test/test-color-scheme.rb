class TestUnitColorScheme < Test::Unit::TestCase
  def test_register
    inverted_scheme_spec = {
      "success" => {:name => "red"},
      "failure" => {:name => "green"},
    }
    Test::Unit::ColorScheme["inverted"] = inverted_scheme_spec
    assert_equal({
                   "success" => color("red"),
                   "failure" => color("green"),
                 },
                 Test::Unit::ColorScheme["inverted"].to_hash)
  end

  def test_new_with_colors
    scheme = Test::Unit::ColorScheme.new(:success => color("blue"),
                                         "failure" => color("green",
                                                            :underline => true))
    assert_equal({
                   "success" => color("blue"),
                   "failure" => color("green", :underline => true),
                 },
                 scheme.to_hash)
  end

  def test_new_with_spec
    scheme = Test::Unit::ColorScheme.new(:success => {
                                           :name => "blue",
                                           :bold => true
                                         },
                                         "failure" => {:name => "green"})
    assert_equal({
                   "success" => color("blue", :bold => true),
                   "failure" => color("green"),
                 },
                 scheme.to_hash)
  end

  private
  def color(name, options={})
    Test::Unit::Color.new(name, options)
  end

  module CleanEnvironment
    def setup
      @original_term, ENV["TERM"] = ENV["TERM"], nil
      @original_color_term, ENV["COLORTERM"] = ENV["COLORTERM"], nil
      @original_vte_version, ENV["VTE_VERSION"] = ENV["VTE_VERSION"], nil
      ENV["TERM"] = "xterm"
    end

    def teardown
      ENV["TERM"] = @original_term
      ENV["COLORTERM"] = @original_color_term
      ENV["VTE_VERSION"] = @original_vte_version
    end
  end

  class TestFor8Colors < self
    include CleanEnvironment

    def test_default
      expected_schema_keys = [
        "pass",
        "pass-marker",
        "failure",
        "failure-marker",
        "pending",
        "pending-marker",
        "omission",
        "omission-marker",
        "notification",
        "notification-marker",
        "error",
        "error-marker",
        "case",
        "suite",
        "diff-inserted-tag",
        "diff-deleted-tag",
        "diff-difference-tag",
        "diff-inserted",
        "diff-deleted",
      ]
      assert_equal(expected_schema_keys.sort,
                   Test::Unit::ColorScheme.default.to_hash.keys.sort)
    end
  end

  class TestGuessAvailableColors < self
    include CleanEnvironment
    {
      "rxvt"                 => 8,
      "xterm-color"          => 8,
      "alacritty"            => 256,
      "iTerm.app"            => 256,
      "screen-256color"      => 256,
      "screenxterm-256color" => 256,
      "tmux-256color"        => 256,
      "vte-256color"         => 256,
      "vscode-direct"        => 2**24,
      "vte-direct"           => 2**24,
      "xterm-direct"         => 2**24,
    }.each do |term, colors|
      data("%20s => %8d" % [term, colors], {term: term, colors: colors})
    end
    def test_term_env(data)
      ENV["TERM"] = data[:term]
      assert_equal(data[:colors],
                   Test::Unit::ColorScheme.available_colors,
                   "Incorrect available_colors for TERM=%s" % [data[:term]])
    end
  end

  class TestDefaultScheme < self
    include CleanEnvironment

    def test_direct_color
      ENV["TERM"] = "xterm-direct"
      assert_equal(Test::Unit::ColorScheme.default_for_256_colors,
                   Test::Unit::ColorScheme.default)
    end
  end
end
