# Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "SignalException" do
  # Differs from MRI
  it "does not self-signal for VTALRM" do
    IO.popen([*ruby_exe, "-e", "raise(SignalException, 'VTALRM')"], err: [:child, :out]) do |out|
      out.read.should include("for SIGVTALRM as it is VM reserved")
    end
    $?.termsig.should == nil
    $?.exitstatus.should == 1
  end
end
