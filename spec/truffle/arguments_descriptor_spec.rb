# truffleruby_primitives: true

require_relative '../ruby/spec_helper'

module ArgumentsDescriptorSpecs
  Info = Struct.new(:values, :descriptor, :arguments)

  def self.tr?
    RUBY_ENGINE == 'truffleruby'
  end

  unless tr?
    module Primitive
      def self.arguments_descriptor
        nil
      end

      def self.arguments
        nil
      end
    end
  end

  def self.no_kws(a, b)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.only_kws(a:, b:)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.only_kws_and_opt(a:, b: 101)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.posn_kws(a, b:)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.posn_kws_defaults(a, b: 101, c: 102)
    Info.new([a, b, c], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.opt_and_kws(a, b=2, c: nil)
    Info.new([a, b, c], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.struct_new_like(a, *b, c: 101)
    Info.new([a, b, c], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.describe_like(a, b = nil)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.single(a)
    Info.new([a], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.rest(*a)
    Info.new([a], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.kw_rest(**a)
    Info.new([a], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.kw_and_kw_rest(a:, **b)
    Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
  end

  def self.mixture(a, b = nil, c = nil, d, e: nil, **f)
    Info.new([a, b, c, d, e, f], Primitive.arguments_descriptor, Primitive.arguments)
  end

  class NewKW
    def self.new(a:, b:)
      Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
    end
  end

  class NewRest
    def self.new(*a)
      Info.new([a], Primitive.arguments_descriptor, Primitive.arguments)
    end
  end

  class A
    def implicit_super(a:, b:)
      Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)

    end

    def explicit_super(a:, b:)
      Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
    end
  end

  class B < A
    def implicit_super(a:, b:)
      super
    end

    def explicit_super(a:, b:)
      super(a: a, b: b)
    end
  end

  def self.yielder(a, b)
    yield(a: a, b: b)
  end

  def self.use_yielder(x, y)
    yielder(x, y) do |a:, b:|
      Info.new([a, b], Primitive.arguments_descriptor, Primitive.arguments)
    end
  end
end

describe "Arguments descriptors" do
  it "are empty for simple calls" do
    info = ArgumentsDescriptorSpecs.no_kws(1, 2)
    info.values.should == [1, 2]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, 2] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain keyword arguments" do
    info = ArgumentsDescriptorSpecs.only_kws(a: 1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain optional keyword arguments" do
    info = ArgumentsDescriptorSpecs.only_kws_and_opt(a: 1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.only_kws_and_opt(a: 1)
    info.values.should == [1, 101]
    info.descriptor.should == [:a, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain keyword arguments with positional arguments" do
    info = ArgumentsDescriptorSpecs.posn_kws(1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:b, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain optional keyword arguments with positional arguments" do
    info = ArgumentsDescriptorSpecs.posn_kws_defaults(1, b: 2, c: 3)
    info.values.should == [1, 2, 3]
    info.descriptor.should == [:b, :c, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {b: 2, c: 3}] if ArgumentsDescriptorSpecs.tr?
  end

  it "do not contain missing optional keyword arguments" do
    info = ArgumentsDescriptorSpecs.posn_kws_defaults(1, b: 2)
    info.values.should == [1, 2, 102]
    info.descriptor.should == [:b, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {b: 2}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.posn_kws_defaults(1, c: 3)
    info.values.should == [1, 101, 3]
    info.descriptor.should == [:c, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {c: 3}] if ArgumentsDescriptorSpecs.tr?
  end

  it "do not contain the name of distant explicitly splatted keyword arguments" do
    distant = {b: 2}
    info = ArgumentsDescriptorSpecs.only_kws(a: 1, **distant)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, 0, false] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "do not contain the name of near explicitly splatted keyword arguments" do
    info = ArgumentsDescriptorSpecs.only_kws(a: 1, **{b: 2})
    info.values.should == [1, 2]
    info.descriptor.should == [:a, 0, false] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain the name of present optional keyword arguments without the optional positional" do
    info = ArgumentsDescriptorSpecs.opt_and_kws(1, c: 3)
    info.values.should == [1, 2, 3]
    info.descriptor.should == [:c, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {c: 3}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work for a call like our Struct.new" do
    info = ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b)
    info.values.should == ['A', [:a, :b], 101]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == ['A', :a, :b] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, c: 1)
    info.values.should == ['A', [:a, :b], 1]
    info.descriptor.should == [:c, 3, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == ['A', :a, :b, {c: 1}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, {c: 1})
    info.values.should == ['A', [:a, :b, {c: 1}], 101]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == ['A', :a, :b, {c: 1}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, **{c: 1})
    info.values.should == ['A', [:a, :b], 1]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == ['A', :a, :b, {c: 1}] if ArgumentsDescriptorSpecs.tr?

    distant = {c: 1}
    info = ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, **distant)
    info.values.should == ['A', [:a, :b], 1]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == ['A', :a, :b, {c: 1}] if ArgumentsDescriptorSpecs.tr?

    -> { ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, d: 1) }.should raise_error(ArgumentError)

    -> { ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, **{d: 1}) }.should raise_error(ArgumentError)

    distant = {d: 1}
    -> { ArgumentsDescriptorSpecs.struct_new_like('A', :a, :b, **distant) }.should raise_error(ArgumentError)
  end

  it "work for a call like a Struct's new" do
    klass = Struct.new(:a)
    a = klass.new({b: [1, 2, 3]})
    a.a.should == {b: [1, 2, 3]}
    b = klass.new(a)
    b.a.should == a
    a.dig(:a, :b).should == [1, 2, 3]
  end

  it "work for a call like MSpec #describe" do
    info = ArgumentsDescriptorSpecs.describe_like(1, b: 2)
    info.values.should == [1, {b: 2}]
    info.descriptor.should == [:b, 1, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "expand a hash not used for keyword arguments" do
    info = ArgumentsDescriptorSpecs.single({a: 1, b: 2})
    info.values.should == [{a: 1, b: 2}]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain keyword argument names passed to a rest" do
    info = ArgumentsDescriptorSpecs.rest(a: 1, b: 2)
    info.values.should == [[{a: 1, b: 2}]]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain keyword argument names passed to a keyword rest" do
    info = ArgumentsDescriptorSpecs.kw_rest(a: 1, b: 2)
    info.values.should == [{a: 1, b: 2}]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "contain keyword argument names passed to a combined keyword rest" do
    info = ArgumentsDescriptorSpecs.kw_and_kw_rest(a: 1)
    info.values.should == [1, {}]
    info.descriptor.should == [:a, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.kw_and_kw_rest(a: 1, b: 2, c: 3)
    info.values.should == [1, {b: 2, c: 3}]
    info.descriptor.should == [:a, :b, :c, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2, c: 3}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.kw_and_kw_rest("abc" => 123, a: 1, b: 2)
    info.values.should == [1, {"abc" => 123, b: 2}]
    info.descriptor.should == [:a, :b, 0, false] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{"abc" => 123, a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work for a mixture of arguments" do
    info = ArgumentsDescriptorSpecs.mixture(1, 2)
    info.values.should == [1, nil, nil, 2, nil, {}]
    info.descriptor.should == []  if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, 2] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.mixture(1, 2, e: 3)
    info.values.should == [1, nil, nil, 2, 3, {}]
    info.descriptor.should == [:e, 2, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, 2, {e: 3}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.mixture(1, 2, {foo: :bar})
    info.values.should == [1, 2, nil, {:foo=>:bar}, nil, {}]
    info.descriptor.should == [] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, 2, {foo: :bar}] if ArgumentsDescriptorSpecs.tr?

    info = ArgumentsDescriptorSpecs.mixture(1, {foo: :bar})
    info.values.should == [1, nil, nil, {foo: :bar}, nil, {}]
    info.descriptor.should == [-1] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [1, {foo: :bar}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work through an inlined call abstraction" do
    foo = -> (a:) { a }
    foo.(a: 1).should == 1
    -> { foo.() }.should raise_error(ArgumentError)
  end

  guard -> { RUBY_ENGINE == 'truffleruby' } do
    it "pass keyword arguments into foreign calls as a Hash" do
      Truffle::Debug.foreign_identity_function.call(a: 1, b: 2).should == {a: 1, b: 2}
    end
  end

  it "work through custom new with keyword arguments" do
    info = ArgumentsDescriptorSpecs::NewKW.new(a: 1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work through custom new with rest" do
    info = ArgumentsDescriptorSpecs::NewRest.new(a: 1, b: 2)
    info.values.should == [[{a: 1, b: 2}]]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work through implicit super" do
    info = ArgumentsDescriptorSpecs::A.new.implicit_super(a: 1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work through explicit super" do
    info = ArgumentsDescriptorSpecs::A.new.explicit_super(a: 1, b: 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end

  it "work through yield" do
    info = ArgumentsDescriptorSpecs.use_yielder(1, 2)
    info.values.should == [1, 2]
    info.descriptor.should == [:a, :b, 0, true] if ArgumentsDescriptorSpecs.tr?
    info.arguments.should == [{a: 1, b: 2}] if ArgumentsDescriptorSpecs.tr?
  end
end
