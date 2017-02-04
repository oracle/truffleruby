require 'test/unit'
require 'power_assert'
require 'ripper'
require 'set'

class TestPowerAssert < Test::Unit::TestCase
  EXTRACT_METHODS_TEST = [
    [[[:method, "c", 4], [:method, "b", 2], [:method, "d", 8], [:method, "a", 0]],
      'a(b(c), d)'],

    [[[:method, "a", 0], [:method, "b", 2], [:method, "d", 6], [:method, "c", 4]],
      'a.b.c(d)'],

    [[[:method, "b", 2], [:method, "a", 0], [:method, "c", 5], [:method, "e", 9], [:method, "d", 7]],
      'a(b).c.d(e)'],

    [[[:method, "b", 4], [:method, "a", 2], [:method, "c", 7], [:method, "e", 13], [:method, "g", 11], [:method, "d", 9], [:method, "f", 0]],
      'f(a(b).c.d(g(e)))'],

    [[[:method, "c", 5], [:method, "e", 11], [:method, "a", 0]],
      'a(b: c, d: e)'],

    [[[:method, "b", 2], [:method, "c", 7], [:method, "d", 10], [:method, "e", 15], [:method, "a", 0]],
      'a(b => c, d => e)'],

    [[[:method, "b", 4], [:method, "d", 10]],
      '{a: b, c: d}'],

    [[[:method, "a", 1], [:method, "b", 6], [:method, "c", 9], [:method, "d", 14]],
      '{a => b, c => d}'],

    [[[:method, "a", 2], [:method, "b", 5], [:method, "c", 10], [:method, "d", 13]],
      '[[a, b], [c, d]]'],

    [[[:method, "a", 0], [:method, "b", 2], [:method, "c", 5]],
      'a b, c { d }'],

    [[[:method, "a", 20]],
      'assertion_message { a }'],

    [[[:method, "a", 0]],
      'a { b }'],

    [[[:method, "c", 4], [:method, "B", 2], [:method, "d", 8], [:method, "A", 0]],
      'A(B(c), d)'],

    [[[:method, "c", 6], [:method, "f", 17], [:method, "h", 25], [:method, "a", 0]],
      'a(b = c, (d, e = f), G = h)'],

    [[[:method, "b", 2], [:method, "c", 6], [:method, "d", 9], [:method, "e", 12], [:method, "g", 18], [:method, "i", 24], [:method, "j", 29], [:method, "a", 0]],
      'a(b, *c, d, e, f: g, h: i, **j)'],

    [[[:method, "a", 0], [:method, "b", 5], [:method, "c", 9], [:method, "+", 7], [:method, "==", 2]],
      'a == b + c'],

    [[[:ref, "var", 0], [:ref, "var", 8], [:method, "var", 4]],
      'var.var(var)'],

    [[[:ref, "B", 2], [:ref, "@c", 5], [:ref, "@@d", 9], [:ref, "$e", 14], [:method, "f", 18], [:method, "self", 20], [:ref, "self", 26], [:method, "a", 0]],
      'a(B, @c, @@d, $e, f.self, self)'],

    [[[:method, "a", 0], [:method, "c", 4], [:method, "b", 2]],
      'a.b c'],

    [[[:method, "b", 4]],
      '"a#{b}c"'],

    [[[:method, "b", 4]],
      '/a#{b}c/'],

    [[],
      '[]'],

    [[[:method, "a", 0], [:method, "[]", 1]],
      'a[0]'],

    # not supported
    [[],
      '[][]'],

    # not supported
    [[],
      '{}[]'],

    [[[:method, "a", 1], [:method, "!", 0]],
      '!a'],

    [[[:method, "a", 1], [:method, "+@", 0]],
      '+a'],

    [[[:method, "a", 1], [:method, "-@", 0]],
      '-a'],

    [[[:method, "a", 2], [:method, "!", 0], [:method, "b", 9], [:method, "+@", 8], [:method, "c", 15], [:method, "-@", 14],
        [:method, "==", 11], [:method, "==", 4]],
      '! a == (+b == -c)'],

    [[[:method, "b", 6]],
      '%x{a#{b}c}'],

    [[[:method, "a", 0], [:method, "b", 3]],
      "a..b"],

    [[[:method, "a", 0], [:method, "b", 4]],
      "a...b"],

    [[[:method, "b", 5]],
      ':"a#{b}c"'],

    # not supported
    [[],
      '->{}.()'],

    [[[:method, "a", 0], [:method, "b", 3], [:method, "call", 2]],
      'a.(b)'],
  ]

  EXTRACT_METHODS_TEST.each_with_index do |(expect, source), idx|
    define_method("test_extract_methods_#{'%03d' % idx}") do
      pa = PowerAssert.const_get(:Context).new(-> { var = nil; -> { var } }.(), nil, TOPLEVEL_BINDING)
      pa.instance_variable_set(:@line, source)
      pa.instance_variable_set(:@assertion_method_name, 'assertion_message')
      assert_equal expect, pa.send(:extract_idents, Ripper.sexp(source)).map(&:to_a), source
    end
  end

  class BasicObjectSubclass < BasicObject
    def foo
      "foo"
    end
  end

  def assertion_message(source = nil, source_binding = TOPLEVEL_BINDING, &blk)
    ::PowerAssert.start(source || blk, assertion_method: __callee__, source_binding: source_binding) do |pa|
      pa.yield
      pa.message
    end
  end

  def Assertion(&blk)
    ::PowerAssert.start(blk, assertion_method: __callee__) do |pa|
      pa.yield
      pa.message
    end
  end

  define_method(:bmethod) do
    false
  end

  def test_lazy_inspection
    PowerAssert.configure do |c|
      assert !c.lazy_inspection
    end
    assert_equal <<END.chomp, assertion_message {
      'a'.sub(/./, 'b').sub!(/./, 'c')
          |             |
          |             "c"
          "b"
END
      'a'.sub(/./, 'b').sub!(/./, 'c')
    }

    PowerAssert.configure do |c|
      c.lazy_inspection = true
    end
    begin
      assert_equal <<END.chomp, assertion_message {
        'a'.sub(/./, 'b').sub!(/./, 'c')
            |             |
            |             "c"
            "c"
END
        'a'.sub(/./, 'b').sub!(/./, 'c')
      }
    ensure
      PowerAssert.configure do |c|
        c.lazy_inspection = false
      end
    end
  end

  def test_assertion_message
    a = 0
    @b = 1
    @@c = 2
    $d = 3
    assert_equal <<END.chomp, assertion_message {
      String(a) + String(@b) + String(@@c) + String($d)
      |      |  | |      |   | |      |    | |      |
      |      |  | |      |   | |      |    | |      3
      |      |  | |      |   | |      |    | "3"
      |      |  | |      |   | |      |    "0123"
      |      |  | |      |   | |      2
      |      |  | |      |   | "2"
      |      |  | |      |   "012"
      |      |  | |      1
      |      |  | "1"
      |      |  "01"
      |      0
      "0"
END
      String(a) + String(@b) + String(@@c) + String($d)
    }


    assert_equal <<END.chomp, assertion_message {
      "0".class == "3".to_i.times.map {|i| i + 1 }.class
          |     |      |    |     |                |
          |     |      |    |     |                Array
          |     |      |    |     [1, 2, 3]
          |     |      |    #<Enumerator: 3:times>
          |     |      3
          |     false
          String
END
      "0".class == "3".to_i.times.map {|i| i + 1 }.class
    }


    assert_equal '', assertion_message {
      false
    }


    assert_equal <<END.chomp,
    assertion_message { "0".class }
                            |
                            String
END
    assertion_message { "0".class }


    assert_equal <<END.chomp,
      "0".class
          |
          String
END
    Assertion {
      "0".class
    }


    assert_equal <<END.chomp,
    Assertion { "0".class }
                    |
                    String
END
    Assertion { "0".class }


    assert_equal <<END.chomp, assertion_message {
      Set.new == Set.new([0])
      |   |   |  |   |
      |   |   |  |   #<Set: {0}>
      |   |   |  Set
      |   |   false
      |   #<Set: {}>
      Set
END
      Set.new == Set.new([0])
    }


    var = [10,20]
    assert_equal <<END.chomp, assertion_message {
      var[0] == 0
      |  |   |
      |  |   false
      |  10
      [10, 20]
END
      var[0] == 0
    }

    a = 1
    assert_equal <<END.chomp, assertion_message {
      ! a != (+a == -a)
      | | |   || |  ||
      | | |   || |  |1
      | | |   || |  -1
      | | |   || false
      | | |   |1
      | | |   1
      | | false
      | 1
      false
END
      ! a != (+a == -a)
    }

    assert_equal <<END.chomp, assertion_message {
      bmethod
      |
      false
END
      bmethod
    }


    a = :a
    assert_equal <<END.chomp, assertion_message {
      a == :b
      | |
      | false
      :a
END
      a == :b
    }


    assert_equal <<END.chomp, assertion_message {
      ! Object
      | |
      | Object
      false
END
      ! Object
    }


    if PowerAssert.respond_to?(:clear_global_method_cache, true)
      3.times do
        assert_equal <<END.chomp, assertion_message {
          String == Array
          |      |  |
          |      |  Array
          |      false
          String
END
          String == Array
        }
      end
    end
  end

  def test_inspection_failure
    assert_match Regexp.new(<<END.chomp.gsub('|', "\\|")),
      assertion_message { BasicObjectSubclass.new.foo }
                          |                   |   |
                          |                   |   "foo"
                          |                   InspectionFailure: NoMethodError: .*
                          TestPowerAssert::BasicObjectSubclass
END
      assertion_message { BasicObjectSubclass.new.foo }


    verbose = $VERBOSE
    default_external = Encoding.default_external
    default_internal = Encoding.default_internal
    begin
      $VERBOSE = nil
      Encoding.default_external = 'cp932'
      Encoding.default_internal = 'utf-8'
      ary = ["\u3042"]
      assert_match Regexp.new(<<END.chomp.gsub('|', "\\|")), assertion_message {
        ary.length
        |   |
        |   1
        InspectionFailure: Encoding::CompatibilityError: .*
END
        ary.length
      }
    ensure
      Encoding.default_internal = default_internal
      Encoding.default_external = default_external
      $VERBOSE = verbose
    end
  end

  def test_assertion_message_with_incompatible_encodings
    if Encoding.default_external == Encoding::UTF_8
      a = "\u3042"
      def a.inspect
        super.encode('utf-16le')
      end
      assert_equal <<END.chomp, assertion_message {
        a + a
        | | |
        | | "\u3042"(UTF-16LE)
        | "\u3042\u3042"
        "\u3042"(UTF-16LE)
END
        a + a
      }
    end


    a = "\xFF"
    def a.inspect
      "\xFF".force_encoding('ascii-8bit')
    end
    assert_equal <<END.chomp.b, assertion_message {
      a.length
      | |
      | 1
      \xFF
END
      a.length
    }.b
  end

  def test_assertion_message_with_string
    a, = 0, a # suppress "assigned but unused variable" warning
    @b = 1
    @@c = 2
    $d = 3
    assert_equal <<END.chomp, assertion_message(<<END, binding)
      String(a) + String(@b) + String(@@c) + String($d)
      |      |  | |      |   | |      |    | |      |
      |      |  | |      |   | |      |    | |      3
      |      |  | |      |   | |      |    | "3"
      |      |  | |      |   | |      |    "0123"
      |      |  | |      |   | |      2
      |      |  | |      |   | "2"
      |      |  | |      |   "012"
      |      |  | |      1
      |      |  | "1"
      |      |  "01"
      |      0
      "0"
END
      String(a) + String(@b) + String(@@c) + String($d)
END
  end

  def test_workaround_for_ruby_2_2
    assert_nothing_raised do
      assertion_message { Thread.new {}.join }
    end
  end

  class H < Hash
    alias aref []
    protected :aref
  end

  def test_workaround_for_bug11182
    assert_nothing_raised do
      {}[:a]
    end
  end
end
