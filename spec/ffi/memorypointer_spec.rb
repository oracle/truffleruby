#
# This file is part of ruby-ffi.
# For licensing, see LICENSE.SPECS
#

require File.expand_path(File.join(File.dirname(__FILE__), "spec_helper"))

MemoryPointer = FFI::MemoryPointer

describe "MemoryPointer#total" do
  it "MemoryPointer.new(:char, 1).total == 1" do
    expect(MemoryPointer.new(:char, 1).total).to eq 1
  end

  it "MemoryPointer.new(:short, 1).total == 2" do
    expect(MemoryPointer.new(:short, 1).total).to eq 2
  end

  it "MemoryPointer.new(:int, 1).total == 4" do
    expect(MemoryPointer.new(:int, 1).total).to eq 4
  end

  it "MemoryPointer.new(:long_long, 1).total == 8" do
    expect(MemoryPointer.new(:long_long, 1).total).to eq 8
  end

  it "MemoryPointer.new(1024).total == 1024" do
    expect(MemoryPointer.new(1024).total).to eq 1024
  end
end
describe "MemoryPointer#clear" do
  it "should clear the memory" do
    ptr = MemoryPointer.new(:long)
    ptr.write_long 1234
    expect(ptr.read_long).to eq(1234)
    ptr.clear
    expect(ptr.read_long).to eq(0)
  end
  it "should deny changes when frozen" do
    skip "not yet supported on TruffleRuby" if RUBY_ENGINE == "truffleruby"
    skip "not yet supported on JRuby" if RUBY_ENGINE == "jruby"
    ptr = MemoryPointer.new(:long).freeze
    expect{ ptr.clear }.to raise_error(RuntimeError, /memory write/)
  end
end
describe "MemoryPointer#read_array_of_long" do
  it "foo" do
    ptr = MemoryPointer.new(:long, 1024)
    ptr[0].write_long 1234
    ptr[1].write_long 5678
    l = ptr.read_array_of_long(2)
    expect(l[0]).to eq 1234
    expect(l[1]).to eq 5678
  end
end
describe "MemoryPointer argument" do
  module Ptr
    extend FFI::Library
    ffi_lib FFI::Platform::LIBC
    attach_function :memset, [ :pointer, :int, :ulong ], :pointer
    attach_function :memcpy, [ :pointer, :pointer, :ulong ], :pointer
  end

  it "Pointer passed correctly" do
    p = MemoryPointer.new :int, 1
    ret = Ptr.memset(p, 0, p.total)
    expect(ret).to eq p
  end

  it "Data passed to native function" do
    p = MemoryPointer.new :int, 1
    p2 = MemoryPointer.new :int, 1
    p2.put_int(0, 0x5eadbeef)
    Ptr.memcpy(p, p2, p.total)
    expect(p.get_int(0)).to eq p2.get_int(0)
    expect(p2.get_int(0)).not_to eql 0
  end
end
describe "MemoryPointer return value" do
  module Stdio
    extend FFI::Library
    ffi_lib FFI::Platform::LIBC
    attach_function :fopen, [ :string, :string ], :pointer
    attach_function :fclose, [ :pointer ], :int
    attach_function :fwrite, [ :pointer, :ulong, :ulong, :string ], :ulong
  end

  it "fopen returns non-nil" do
    fp = Stdio.fopen("/dev/null", "w")
    expect(fp).to_not be_nil
    expect(Stdio.fclose(fp)).to  eq 0 unless fp.nil? or fp.null?
  end
end
describe "#autorelease" do
  it "should be true by default" do
    expect(MemoryPointer.new(8).autorelease?).to be true
  end

  it "should return false when autorelease=(false)" do
    ptr = MemoryPointer.new(8)
    ptr.autorelease = false
    expect(ptr.autorelease?).to be false
    ptr.free
  end

  it "should deny changes when frozen" do
    skip "not yet supported on TruffleRuby" if RUBY_ENGINE == "truffleruby"
    skip "not yet supported on JRuby" if RUBY_ENGINE == "jruby"
    ptr = MemoryPointer.new(8).freeze
    expect{ ptr.autorelease = false }.to raise_error(FrozenError)
  end
end
