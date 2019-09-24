# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "SignalException" do
  it "sends a signal to the running process when uncaught" do
    # there is a similar spec in rubyspec, but the JVM catches SIGTERM,
    # making it hard to tell whether the process was killed by signal

    # make sure there is no backtrace for the exception
    -> { ruby_exe("raise SignalException, 'SIGKILL'") }.should output_to_fd("", STDERR)
    $?.termsig.should == Signal.list["KILL"]
  end

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

  it "runs after at_ext" do
    output = ruby_exe(<<-RUBY)
      at_exit do
        puts "hello"
        $stdout.flush
      end

      raise SignalException, 'SIGKILL'
    RUBY

    $?.termsig.should == Signal.list["KILL"]
    output.should == "hello\n"
  end
end
