# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

describe "RubyGems" do
  it "is not loaded until needed" do
    ruby_exe('puts $"').should_not include('/rubygems.rb')
    ruby_exe('p autoload? :Gem').should == "\"rubygems\"\n"
  end

  it "is loaded when accessing Gem" do
    ruby_exe('Gem; puts $"').should include('/rubygems.rb')
    ruby_exe('Gem; p autoload? :Gem').should == "nil\n"
  end

  it "is loaded by a failing require" do
    code = 'begin; require "lrg-does-not-exist"; rescue LoadError; puts $"; end'
    ruby_exe(code).should include('/rubygems.rb')
    $?.success?.should == true
  end
end

describe "Lazy RubyGems" do
  # See https://github.com/rubygems/rubygems/issues/2772
  it "defines StringIO like RubyGems which requires it eagerly" do
    ruby_exe('puts StringIO').should == "StringIO\n"
  end
end
