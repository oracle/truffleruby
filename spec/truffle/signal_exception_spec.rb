# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "SignalException" do
  it "does not self-signal for VTALRM" do
    IO.popen(ruby_cmd("raise(SignalException, 'VTALRM')"), err: [:child, :out]) do |out|
      out.read.should include("for SIGVTALRM as it is VM reserved")
    end
    $?.termsig.should be_nil
  end

  guard -> { TruffleRuby.native? } do
    it "self-signals for USR1 when running natively" do
      ruby_exe("raise(SignalException, 'USR1')")
      $?.termsig.should == Signal.list.fetch('USR1')
    end
  end

  guard -> { !TruffleRuby.native? } do
    it "does not self-signal for USR1 when running on the JVM" do
      IO.popen(ruby_cmd("raise(SignalException, 'USR1')"), err: [:child, :out]) do |out|
        out.read.should include("for SIGUSR1 as it is VM reserved")
      end

      $?.termsig.should be_nil
    end
  end
end
