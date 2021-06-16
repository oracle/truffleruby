# truffleruby_primitives: true

# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.
#

require_relative '../../ruby/spec_helper'

describe "IO bufffers" do

  before do
    @mem_check = Proc.new do |*ptrs|
      bytes = "\xde\xad\xbe\xef\x53"
      ptrs.each do |ptr|
        ptr.write_bytes(bytes * ((ptr.size + bytes.bytesize - 1) / bytes.bytesize) , 0, ptr.size)
      end
      ptrs.each do |ptr|
        ptr.read_bytes(ptr.size).bytes.should == (bytes * ((ptr.size + bytes.bytesize - 1) / bytes.bytesize)).bytes[0...ptr.size]
      end
    end
  end

  it "allocate buffer space and return a non-null pointer" do
    ptr = Primitive.io_thread_buffer_allocate(1)
    begin
      ptr.null?.should == false
      @mem_check.call(ptr)
    ensure
      Primitive.io_thread_buffer_free(ptr)
    end
  end

  it "allocate buffer space on a new thread and return a non-null pointer" do
    Thread.new do
      ptr = Primitive.io_thread_buffer_allocate(1)
      begin
        ptr.null?.should == false
        @mem_check.call(ptr)
      ensure
        Primitive.io_thread_buffer_free(ptr)
      end
    end.join
  end

  it "allocate multiple buffers and returns non-null pointers" do
    ptr1 = Primitive.io_thread_buffer_allocate(1)
    ptr2 = Primitive.io_thread_buffer_allocate(1)
    begin
      ptr1.null?.should == false
      ptr2.null?.should == false
      @mem_check.call(ptr1, ptr2)
    ensure
      Primitive.io_thread_buffer_free(ptr2)
      Primitive.io_thread_buffer_free(ptr1)
    end
  end

  it "allocate multiple buffers and returns non-null pointers for large buffer sizes" do
    ptr1 = Primitive.io_thread_buffer_allocate(4096 * 4)
    ptr2 = Primitive.io_thread_buffer_allocate(4096 * 4)
    begin
      ptr1.null?.should == false
      ptr2.null?.should == false
      @mem_check.call(ptr1, ptr2)
    ensure
      Primitive.io_thread_buffer_free(ptr2)
      Primitive.io_thread_buffer_free(ptr1)
    end
  end

  it "allocate multiple buffers and returns non-null pointers" do
    Thread.new do
      ptr1 = Primitive.io_thread_buffer_allocate(1)
      ptr2 = Primitive.io_thread_buffer_allocate(1)
      begin
        ptr1.null?.should == false
        ptr2.null?.should == false
        @mem_check.call(ptr1, ptr2)
      ensure
        Primitive.io_thread_buffer_free(ptr2)
        Primitive.io_thread_buffer_free(ptr1)
      end
    end.join
  end

  it "allocate multiple buffers and returns non-null pointers for large buffer sizes" do
    Thread.new do
      ptr1 = Primitive.io_thread_buffer_allocate(4096 * 4)
      ptr2 = Primitive.io_thread_buffer_allocate(4096 * 4)
      begin
        ptr1.null?.should == false
        ptr2.null?.should == false
        @mem_check.call(ptr1, ptr2)
      ensure
        Primitive.io_thread_buffer_free(ptr2)
        Primitive.io_thread_buffer_free(ptr1)
      end
    end.join
  end

end
