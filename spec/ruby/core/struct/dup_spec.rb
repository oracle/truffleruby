require_relative '../../spec_helper'
require_relative 'fixtures/classes'

describe "Struct-based class#dup" do

  it "duplicates members" do
    klass = Struct.new(:foo, :bar)
    instance = klass.new(14, 2)
    duped = instance.dup
    duped.foo.should == 14
    duped.bar.should == 2
  end

  # From https://github.com/jruby/jruby/issues/3686
  it "retains an included module in the ancestor chain for the struct's singleton class" do
    klass = Struct.new(:foo)
    mod = Module.new do
      def hello
        "hello"
      end
    end

    klass.extend(mod)
    klass_dup = klass.dup
    klass_dup.hello.should == "hello"
  end

end
