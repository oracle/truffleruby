require_relative '../../spec_helper'

describe "GC.stat" do
  it "supports access by key" do
    keys = [:heap_free_slots, :total_allocated_objects, :count]
    keys.each do |key|
      GC.stat(key).should be_kind_of(Integer)
    end
  end

  it "returns hash of values" do
    stat = GC.stat
    stat.should be_kind_of(Hash)
    stat.keys.should include(:count)
  end

  it "increases count after GC is run" do
    count = GC.stat(:count)
    GC.start
    GC.stat(:count).should > count
  end

  it "increases major_gc_count after GC is run" do
    count = GC.stat(:major_gc_count)
    GC.start
    GC.stat(:major_gc_count).should > count
  end
end
