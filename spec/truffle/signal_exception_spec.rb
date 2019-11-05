# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "SignalException" do
  it "can be rescued" do
    ruby_exe(<<-RUBY)
      begin
        raise SignalException, 'SIGKILL'
      rescue SignalException
        exit(0)
      end
      exit(1)
    RUBY

    $?.exitstatus.should == 0
  end

  it "runs after at_exit" do
    output = ruby_exe(<<-RUBY)
      at_exit do
        puts "hello"
        $stdout.flush
      end

      raise SignalException, 'SIGKILL'
    RUBY

    $?.termsig.should == Signal.list.fetch("KILL")
    output.should == "hello\n"
  end

  it "cannot be trapped with Signal.trap" do
    ruby_exe(<<-RUBY)
      Signal.trap("PROF") {}
      raise(SignalException, "PROF")
    RUBY

    $?.termsig.should == Signal.list.fetch("PROF")
  end

  it "does not self-signal for VTALRM" do
    IO.popen(ruby_cmd("raise(SignalException, 'VTALRM')"), err: [:child, :out]) { |out|
      out.read.should include("for SIGVTALRM as it is VM reserved")
    }
    $?.termsig.should be_nil
  end

  it "self-signals for USR1 when running natively" do
    skip unless TruffleRuby.native?
    ruby_exe("raise(SignalException, 'USR1')", options: '--native')
    $?.termsig.should == Signal.list.fetch('USR1')
  end

  it "does not self-signal for USR1 when running on the JVM" do
    IO.popen(ruby_cmd("raise(SignalException, 'USR1')", options: '--jvm'), err: [:child, :out]) { |out|
      out.read.should include("for SIGUSR1 as it is VM reserved")
    }

    $?.termsig.should be_nil
  end
end
