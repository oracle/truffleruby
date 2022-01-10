# truffleruby_primitives: true

# Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Sharing is correctly propagated for" do

  before :all do
    # Start multithreading and make 'self' shared
    @share = nil
    Thread.new { self }.join
  end

  def shared?(obj)
    Truffle::Debug.shared?(obj)
  end

  it "Object with instance_variable_set" do
    obj = Object.new
    obj.instance_variable_set(:@initial, Object.new)
    shared?(obj).should == false

    @share = obj
    shared?(obj).should == true
    shared?(obj.instance_variable_get(:@initial)).should == true

    obj.instance_variable_set(:@wb, Object.new)
    shared?(obj.instance_variable_get(:@wb)).should == true
  end

  it "Object with attr_accessor" do
    cls = Class.new do
      attr_accessor :initial, :wb
    end
    obj = cls.new
    obj.initial = Object.new
    shared?(obj).should == false

    @share = obj
    shared?(obj).should == true
    shared?(obj.initial).should == true

    obj.wb = Object.new
    shared?(obj.wb).should == true
  end

  it "Object with @ivar accesses" do
    cls = Class.new do
      def initialize(initial)
        @initial = initial
      end

      def wb=(value); @wb = value; end

      def initial; @initial; end
      def wb; @wb; end
    end
    obj = cls.new(Object.new)
    shared?(obj).should == false

    @share = obj
    shared?(obj).should == true
    shared?(obj.initial).should == true

    obj.wb = Object.new
    shared?(obj.wb).should == true
  end

  it "Array" do
    ary = [Object.new]
    shared?(ary).should == false

    @share = ary
    shared?(ary).should == true
    shared?(ary[0]).should == true

    ary << Object.new
    shared?(ary.last).should == true
  end

  it "Array#initialize" do
    ary = Array.allocate
    @share = ary
    shared?(ary).should == true

    ary.send(:initialize, 3, Object.new)
    ary.each { |e| shared?(e).should == true }

    ary.send(:initialize, 3) { Object.new }
    ary.each { |e| shared?(e).should == true }

    ary.send(:initialize, [Object.new, Object.new])
    ary.each { |e| shared?(e).should == true }
  end

  it "Array#[]=" do
    ary = []
    @share = ary
    shared?(ary).should == true

    ary[0] = Object.new
    shared?(ary[0]).should == true

    ary[0] = "foobar"
    shared?(ary[0]).should == true

    ary[42] = Object.new
    ary.each { |e| shared?(e).should == true }

    ary[0, 2] = [Object.new, Object.new]
    ary.each { |e| shared?(e).should == true }

    ary[0, 3] = [Object.new]
    ary.each { |e| shared?(e).should == true }
  end

  it "Array#<<" do
    ary = []
    @share = ary
    shared?(ary).should == true

    ary << Object.new
    shared?(ary.last).should == true
  end

  it "Array#concat" do
    ary = [Object.new]
    @share = ary
    shared?(ary).should == true

    ary.concat([Object.new, Object.new])
    ary.each { |e| shared?(e).should == true }
  end

  it "Array#fill" do
    ary = [Object.new, Object.new]
    @share = ary
    shared?(ary).should == true

    ary.fill(Object.new)
    ary.each { |e| shared?(e).should == true }
  end

  it "Array#map!" do
    ary = [Object.new, Object.new]
    @share = ary
    shared?(ary).should == true

    ary.map! { Object.new }
    ary.each { |e| shared?(e).should == true }
  end

  it "Array#replace" do
    ary = []
    @share = ary
    shared?(ary).should == true

    new_ary = []
    ary.replace(new_ary)
    shared?(new_ary).should == true
  end

  it "Array :steal_array_storage" do
    ary = []
    @share = ary
    shared?(ary).should == true

    new_ary = []
    Primitive.steal_array_storage(ary, new_ary)
    shared?(new_ary).should == true
  end

  it "Array#shift with copy-on-write DelegatedArrayStorage" do
    ary = [Object.new, Object.new, Object.new]
    ary.shift
    @share = ary
    shared?(ary).should == true
    ary.each { |e| shared?(e).should == true }

    wb = Object.new
    ary[0] = wb
    ary.each { |e| shared?(e).should == true }
  end

  it "Hash" do
    initial = Object.new
    hsh = { initial => Object.new }
    shared?(hsh).should == false

    @share = hsh
    shared?(hsh).should == true
    shared?(initial).should == true
    shared?(hsh[initial]).should == true

    wb = Object.new
    hsh[wb] = Object.new
    shared?(wb).should == true
    shared?(hsh[wb]).should == true
  end

  it "Hash default value" do
    default = Object.new
    hsh = Hash.new(default)
    shared?(hsh).should == false

    @share = hsh
    shared?(hsh).should == true
    shared?(default).should == true

    wb = Object.new
    hsh.default = wb
    shared?(wb).should == true
  end

  it "Hash default Proc" do
    default_proc = proc { nil }
    hsh = Hash.new(&default_proc)
    shared?(hsh).should == false

    @share = hsh
    shared?(hsh).should == true
    shared?(default_proc).should == true

    wb = proc { 42 }
    hsh.default_proc = wb
    shared?(wb).should == true
  end

  it "Hash#initialize" do
    hsh = Hash.allocate
    @share = hsh
    shared?(hsh).should == true

    default = Object.new
    hsh.send(:initialize, default)
    shared?(default).should == true

    default_proc = proc { 42 }
    hsh.send(:initialize, &default_proc)
    shared?(default_proc).should == true
  end

  it "Hash#replace" do
    hsh = {}
    @share = hsh
    shared?(hsh).should == true

    new_hash = {}
    hsh.replace(new_hash)
    shared?(new_hash).should == true
  end

  it "Fiber local variables which share the value (since they can be accessed from other threads)" do
    require 'fiber'
    # Create a new Thread to make sure the root Fiber is shared as expected
    Thread.new do
      thread = Thread.current
      shared?(thread).should == true

      shared?(Fiber.current).should == true

      obj = Object.new
      thread[:sharing_spec] = obj
      begin
        shared?(obj).should == true
      ensure
        thread[:sharing_spec] = nil
      end
    end.join
  end

  it "Thread local variables which share the value (probably they should not)" do
    thread = Thread.current
    shared?(thread).should == true

    obj = Object.new
    thread.thread_variable_set(:sharing_spec, obj)
    shared?(obj).should == true # TODO (eregon, 5 Jun 2019): this is the current non-ideal behavior
  end

  it "classes and constants and they are not shared until sharing is started" do
    ruby_exe("p Truffle::Debug.shared?(Object)").should == "false\n"
  end

  it "thread-local IO buffers which should not trigger sharing" do
    ruby_exe("File.read(#{__FILE__.inspect}); p Truffle::Debug.shared?(Object)").should == "false\n"
  end

end
