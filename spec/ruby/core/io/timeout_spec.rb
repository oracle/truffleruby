# -*- encoding: utf-8 -*-
require_relative '../../spec_helper'

ruby_version_is "3.2" do
  describe "IO#timeout=" do
    before :each do
      @rpipe, @wpipe = IO.pipe
    end

    after :each do
      @rpipe.close
      @wpipe.close
    end

    it "can be set and read on File instances" do
      fname = tmp("io_timeout_attribute.txt")
      touch(fname)
      file = File.open(fname, "a+")

      file.timeout.should == nil

      file.timeout = 1.23
      file.timeout.should == 1.23
    ensure
      file.close
      rm_r fname
    end

    it "can be set and read on IO instances" do
      @rpipe.timeout.should == nil
      @wpipe.timeout.should == nil

      @rpipe.timeout = 1.23
      @wpipe.timeout = 4.56

      @rpipe.timeout.should == 1.23
      @wpipe.timeout.should == 4.56
    end

    it "raises TypeError when incorrect value is provided for timeout" do
      -> { @rpipe.timeout = -1 }.should raise_error(ArgumentError, 'time interval must not be negative')
      -> { @rpipe.timeout = "1" }.should raise_error(TypeError, 'can\'t convert String into time interval')
    end

    it "accepts objects for interval that have the :divmod method defined" do
      s = "abc"
      s.define_singleton_method(:divmod) { |_| [0, 0.001] }

      @rpipe.timeout = s
      @rpipe.timeout.should == s
    end
  end

  describe "IO#timeout" do
    before :each do
      @rpipe, @wpipe = IO.pipe
      # there is no strict long term standard for pipe limits (2**16 bytes currently). This is an attempt to set a safe
      # enough size to test a full pipe
      @more_than_pipe_limit = 1 << 18
    end

    after :each do
      @rpipe.close
      @wpipe.close
    end

    it "raises TypeError when incorrect value is observed for timeout at the time of use" do
      klass = Class.new do
        attr_accessor(:v)
        def divmod(_); [v, 0]; end
      end

      o = klass.new
      o.v = 1

      @rpipe.timeout = o

      o.v = -1
      -> { @rpipe.read(1) }.should raise_error(ArgumentError, 'time interval must not be negative')
    end

    it "raises IO::TimeoutError when timeout is exceeded for .read" do
      @rpipe.timeout = 0.001
      -> { @rpipe.read.should }.should raise_error(IO::TimeoutError)
    end

    it "raises IO::TimeoutError when timeout is exceeded for .read(n)" do
      @rpipe.timeout = 0.001
      -> { @rpipe.read(3) }.should raise_error(IO::TimeoutError)
    end

    it "raises IO::TimeoutError when timeout is exceeded for .gets" do
      @rpipe.timeout = 0.001
      -> { @rpipe.gets }.should raise_error(IO::TimeoutError)
    end

    it "doesn't affect read_nonblock" do
      @rpipe.timeout = 0.001
      -> { @rpipe.read_nonblock(42) }.should raise_error(IO::EAGAINWaitReadable)
    end

    it "attempts to read first before checking the timeout" do
      @wpipe.write("abc")
      @wpipe.close

      @rpipe.timeout = 0
      @rpipe.read(3).should == "abc"
    end

    it "waits for a read and completes" do
      @rpipe.timeout = 1
      read_result = nil

      t = Thread.new do
        read_result = @rpipe.read(3)
      end

      Thread.pass until t.stop?

      @wpipe.write("abc")
      @wpipe.close

      t.join

      read_result.should == "abc"
    end

    it "raises IO::TimeoutError when timeout is exceeded for .write" do
      @wpipe.timeout = 0.001
      -> { @wpipe.write("x" * @more_than_pipe_limit) }.should raise_error(IO::TimeoutError)
    end

    it "raises IO::TimeoutError when timeout is exceeded for .puts" do
      @wpipe.timeout = 0.001
      -> { @wpipe.puts("x" * @more_than_pipe_limit) }.should raise_error(IO::TimeoutError)
    end

    it "doesn't affect write_nonblock" do
      # #write_nonblock should be before #timeout= because #write_nonblock leaves io in a non-block mode,
      loop {
        break if @wpipe.write_nonblock("a" * 10_000, exception: false) == :wait_writable
      }

      @wpipe.timeout = 0.001
      -> { @wpipe.write_nonblock("abc") }.should raise_error(IO::EAGAINWaitWritable)
    end

    it "waits for a write and completes" do
      # #write_nonblock should be before #timeout= because #write_nonblock leaves io in a non-block mode,
      loop {
        break if @wpipe.write_nonblock("a" * 10_000, exception: false) == :wait_writable
      }

      @wpipe.timeout = 1

      write_result = nil
      blocked = true
      t = Thread.new do
        write_result = @wpipe.write("abc")
        blocked = false
      end

      Thread.pass until t.stop?

      # trigger a buffer release in the pipe
      @rpipe.read(4096) while blocked # number of bytes is platform specific and may very (e.g. 4kb, 64kb)

      t.join

      write_result.should == 3
    end

    it "attempts to write first before checking the timeout" do
      @wpipe.timeout = 0
      @wpipe.write("abc").should == 3
    end

    it "times out with .read when there is no EOF" do
      @wpipe.write("hello")
      @rpipe.timeout = 0.001

      -> { @rpipe.read }.should raise_error(IO::TimeoutError)
    end

    it "returns content with .read when there is EOF" do
      @wpipe.write("hello")
      @wpipe.close

      @rpipe.timeout = 0.001

      @rpipe.read.should == "hello"
    end

    it "times out with .read(N) when there is not enough bytes" do
      @wpipe.write("hello")
      @rpipe.timeout = 0.001

      @rpipe.read(2).should == "he"
      -> { @rpipe.read(5) }.should raise_error(IO::TimeoutError)
    end

    it "returns partial content with .read(N) when there is not enough bytes but there is EOF" do
      @wpipe.write("hello")
      @rpipe.timeout = 0.001

      @rpipe.read(2).should == "he"

      @wpipe.close
      @rpipe.read(5).should == "llo"
    end

    it "blocks after timeout has been nullified" do
      require "timeout"
      @rpipe.timeout = 0.001
      @rpipe.timeout = nil

      -> { Timeout.timeout(0.01) { @rpipe.read } }.should raise_error(Timeout::Error)
    end
  end
end
