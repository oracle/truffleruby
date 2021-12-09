# truffleruby_primitives: true

require_relative '../ruby/spec_helper'

def truffleruby?
  RUBY_ENGINE == 'truffleruby'
end

class NewKW
  def self.new(a:, b:)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end
end

class NewRest
  def self.new(*a)
    [a, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end
end

class GemCommandA
  attr_reader :trace

  def initialize(command, summary=nil, defaults={})
    @trace = [command, summary, defaults, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end
end

class GemCommandB < GemCommandA
  def initialize
    super 'uninstall', 'Uninstall gems from the local repository',
          :version => 14, :user_install => true,
          :check_dev => false, :vendor => false
  end
end

describe "Keyword arguments" do
  def no_kws(a, b)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def only_kws(a:, b:)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def only_kws_and_opt(a:, b: 101)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def posn_kws(a, b:)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def posn_kws_defaults(a, b: 101, c: 102)
    [a, b, c, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def opt_and_kws(a, b=2, c: nil)
    [a, b, c, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def struct_new_like(a, *b, c: 101)
    [a, b, c, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def describe_like(a, b = nil)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def single(a)
    [a, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def rest(*a)
    [a, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def kw_rest(**a)
    [a, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def kw_and_kw_rest(a:, **b)
    [a, b, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  def mixture(a, b = nil, c = nil, d, e: nil, **f)
    [a, b, c, d, e, f, *(truffleruby? ? [Primitive.keyword_descriptor, Primitive.keyword_arguments, Primitive.needs_expanded_hash?] : []) ]
  end

  describe "pass a descriptor describing static keyword arguments that" do
    it "is empty for simple calls" do
      no_kws(1, 2).should == [1, 2, *(truffleruby? ? [[], [], true] : []) ]
    end

    it "contains only keyword arguments" do
      only_kws(a: 1, b: 2).should == [1, 2, *(truffleruby? ? [[:a, :b, false, 0], [1, 2], false] : []) ]
    end

    it "works for keyword arguments and keyword optional" do
      only_kws_and_opt(a: 1, b: 2).should == [1, 2, *(truffleruby? ? [[:a, :b, false, 0], [1, 2], false] : []) ]
      only_kws_and_opt(a: 1).should == [1, 101, *(truffleruby? ? [[:a, false, 0], [1], false] : []) ]
    end

    it "contains the names of simple keyword arguments" do
      posn_kws(1, b: 2).should == [1, 2, *(truffleruby? ? [[:b, false, 1], [2], false] : []) ]
    end

    it "contains the names of present optional keyword arguments" do
      posn_kws_defaults(1, b: 2, c: 3).should == [1, 2, 3, *(truffleruby? ? [[:b, :c, false, 1], [2, 3], false] : []) ]
    end

    it "does not contain the name of missing optional keyword arguments" do
      posn_kws_defaults(1, b: 2).should == [1, 2, 102, *(truffleruby? ? [[:b, false, 1], [2], false] : []) ]
      posn_kws_defaults(1, c: 3).should == [1, 101, 3, *(truffleruby? ? [[:c, false, 1], [3], false] : []) ]
    end

    it "does not contain the name of distant explicitly splatted keyword arguments" do
      distant = {b: 2}
      only_kws(a: 1, **distant).should == [1, 2, *(truffleruby? ? [[:a, true, 0], [1], false] : []) ]
    end

    it "does not contain the name of near explicitly splatted keyword arguments" do
      only_kws(a: 1, **{b: 2}).should == [1, 2, *(truffleruby? ? [[:a, true, 0], [1], false] : []) ]
    end

    it "contains the name of present optional keyword argument without the optional positional" do
      opt_and_kws(1, c: 3).should == [1, 2, 3, *(truffleruby? ? [[:c, false, 1], [3], true] : []) ]
    end

    it "works for a call like our Struct.new" do
      struct_new_like('A', :a, :b).should == ['A', [:a, :b], 101, *(truffleruby? ? [[], [], true] : []) ]
      struct_new_like('A', :a, :b, c: 1).should == ['A', [:a, :b], 1, *(truffleruby? ? [[:c, false, 3], [1], true] : []) ]
      struct_new_like('A', :a, :b, {c: 1}).should == ['A', [:a, :b, {c: 1}], 101, *(truffleruby? ? [[], [], true] : []) ]
      struct_new_like('A', :a, :b, **{c: 1}).should == ['A', [:a, :b], 1, *(truffleruby? ? [[true, 3], [], true] : []) ]
      distant = {c: 1}
      struct_new_like('A', :a, :b, **distant).should == ['A', [:a, :b], 1, *(truffleruby? ? [[true, 3], [], true] : []) ]
      -> { struct_new_like('A', :a, :b, d: 1) }.should raise_error(ArgumentError)
      -> { struct_new_like('A', :a, :b, **{d: 1}) }.should raise_error(ArgumentError)
      distant = {d: 1}
      -> { struct_new_like('A', :a, :b, **distant) }.should raise_error(ArgumentError)
    end

    it "works for a call like a Struct's new" do
      klass = Struct.new(:a)
      a = klass.new({b: [1, 2, 3]})
      a.a.should == {b: [1, 2, 3]}
      b = klass.new(a)
      b.a.should == a
      a.dig(:a, :b).should == [1, 2, 3]
    end

    it "works for a call like MSpec #describe" do
      describe_like(1, b: 2).should == [1, {b: 2}, *(truffleruby? ? [[:b, false, 1], [2], true] : []) ]
    end

    it "expands a hash not used for keyword arguments" do
      single({a: 1, b: 2}).should == [{a: 1, b: 2}, *(truffleruby? ? [[], [], true] : []) ]
    end

    it "expands a rest" do
      rest(a: 1, b: 2).should == [[{a: 1, b: 2}], *(truffleruby? ? [[:a, :b, false, 0], [1, 2], true] : []) ]
    end

    it "expands a keyword rest" do
      kw_rest(a: 1, b: 2).should == [{a: 1, b: 2}, *(truffleruby? ? [[:a, :b, false, 0], [1, 2], true] : []) ]
    end

    it "expands keyword and keyword rest arguments" do
      kw_and_kw_rest(a: 1).should == [1, {}, *(truffleruby? ? [[:a, false, 0], [1], true] : []) ]
      kw_and_kw_rest(a: 1, b: 2, c: 3).should == [1, {b: 2, c: 3}, *(truffleruby? ? [[:a, :b, :c, false, 0], [1, 2, 3], true] : []) ]
      kw_and_kw_rest("abc" => 123, a: 1, b: 2).should == [1, {"abc" => 123, b: 2}, *(truffleruby? ? [[:a, :b, true, 0], [1, 2], true] : []) ]
    end

    it "works for a mixture of arguments" do
      mixture(1, 2).should == [1, nil, nil, 2, nil, {}, *(truffleruby? ? [[], [], true] : []) ]
      mixture(1, 2, e: 3).should == [1, nil, nil, 2, 3, {}, *(truffleruby? ? [[:e, false, 2], [3], true] : []) ]
      mixture(1, 2, {foo: :bar}).should == [1, 2, nil, {:foo=>:bar}, nil, {}, *(truffleruby? ? [[], [], true] : []) ]
      mixture(1, {foo: :bar}).should == [1, nil, nil, {foo: :bar}, nil, {}, *(truffleruby? ? [[], [], true] : []) ]
    end

    it "works through an inlined call abstraction" do
      foo = -> (a:) { a }
      foo.(a: 1).should == 1
      -> { foo.() }.should raise_error(ArgumentError)
    end

    guard -> { RUBY_ENGINE == 'truffleruby' } do
      it "works with foreign calls" do
        Truffle::Debug.foreign_identity_function.call(a: 1, b: 2).should == {a: 1, b: 2}
      end
    end

    it "works through custom new with keyword arguments, by expanding" do
      NewKW.new(a: 1, b: 2).should == [1, 2, *(truffleruby? ? [[:a, :b, false, 0], [1, 2], false] : []) ]
    end

    it "works through custom new with rest, by expanding" do
      NewRest.new(a: 1, b: 2).should == [[{a: 1, b: 2}], *(truffleruby? ? [[:a, :b, false, 0], [1, 2], true] : []) ]
    end

    it "works for a pattern like that found in Gem::Commands::UninstallCommand" do
      foo = GemCommandB.new
      foo.trace.should == [
        "uninstall",
        "Uninstall gems from the local repository",
        {:version=>14, :user_install=>true, :check_dev=>false, :vendor=>false},
        *(truffleruby? ? [[:version, :user_install, :check_dev, :vendor, false, 2], [14, true, false, false], true] : [])
      ]
    end
  end
end
