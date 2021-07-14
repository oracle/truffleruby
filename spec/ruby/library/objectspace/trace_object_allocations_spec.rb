require_relative '../../spec_helper'
require 'objspace'

describe "ObjectSpace.trace_object_allocations" do
  it "runs a block" do
    ObjectSpace.trace_object_allocations do
      ScratchPad.record :a
    end
    ScratchPad.recorded.should == :a
  end

  it "records info for allocation_class_path" do
    ObjectSpace.trace_object_allocations do
      o = Object.new
      ObjectSpace.allocation_class_path(o).should == "Class"
      a = [1, 2, 3]
      ObjectSpace.allocation_class_path(a).should == nil
    end
  end

  it "records info for allocation_generation" do
    ObjectSpace.trace_object_allocations do
      o = Object.new
      ObjectSpace.allocation_generation(o).kind_of?(Integer).should == true
      a = [1, 2, 3]
      ObjectSpace.allocation_generation(a).kind_of?(Integer).should == true
    end
  end

  it "records info for allocation_method_id" do
    ObjectSpace.trace_object_allocations do
      o = Object.new
      ObjectSpace.allocation_method_id(o).should == :new
      a = [1, 2, 3]
      ObjectSpace.allocation_method_id(a).should == nil
    end
  end

  it "records info for allocation_sourcefile" do

    ObjectSpace.trace_object_allocations do
      filename = File.basename(__FILE__)
      o = Object.new
      ObjectSpace.allocation_sourcefile(o).end_with?(filename).should == true
      a = [1, 2, 3]
      ObjectSpace.allocation_sourcefile(a).end_with?(filename).should == true
    end
  end

  it "records info for allocation_sourceline" do
    ObjectSpace.trace_object_allocations do
      o = Object.new
      ObjectSpace.allocation_sourceline(o).should == __LINE__ - 1
      a = [1, 2, 3]
      ObjectSpace.allocation_sourceline(a).should == __LINE__ - 1
    end
  end

  it "can be cleared using trace_object_allocations_clear" do
    ObjectSpace.trace_object_allocations do
      o = Object.new
      ObjectSpace.allocation_class_path(o).should == "Class"
      ObjectSpace.trace_object_allocations_clear
      ObjectSpace.allocation_class_path(o).should be_nil
    end
  end

  it "does not clears allocation data after returning" do
    o = nil
    ObjectSpace.trace_object_allocations do
      o = Object.new
    end
    ObjectSpace.allocation_class_path(o).should == "Class"
  end

  it "can be used without a block using trace_object_allocations_start and _stop" do
    ObjectSpace.trace_object_allocations_start
    o = Object.new
    ObjectSpace.allocation_class_path(o).should == "Class"
    a = [1, 2, 3]
    ObjectSpace.allocation_class_path(a).should == nil
    ObjectSpace.trace_object_allocations_stop
  end

  it "does not clears allocation data after trace_object_allocations_stop" do
    ObjectSpace.trace_object_allocations_start
    o = Object.new
    ObjectSpace.trace_object_allocations_stop
    ObjectSpace.allocation_class_path(o).should == "Class"
  end

  it "can be nested" do
    ObjectSpace.trace_object_allocations do
      ObjectSpace.trace_object_allocations do
        o = Object.new
        ObjectSpace.allocation_class_path(o).should == "Class"
      end
    end
  end

  it "can be nested without a block using trace_object_allocations_start and _stop" do
    ObjectSpace.trace_object_allocations_start
    ObjectSpace.trace_object_allocations_start
    o = Object.new
    ObjectSpace.allocation_class_path(o).should == "Class"
    ObjectSpace.trace_object_allocations_stop
    ObjectSpace.trace_object_allocations_stop
  end

  it "can be nested with more _stop than _start" do
    ObjectSpace.trace_object_allocations_start
    o = Object.new
    ObjectSpace.allocation_class_path(o).should == "Class"
    ObjectSpace.trace_object_allocations_stop
    ObjectSpace.trace_object_allocations_stop
  end
end
