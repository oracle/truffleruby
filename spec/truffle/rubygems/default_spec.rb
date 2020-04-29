# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../ruby/spec_helper'
require 'rubygems'

describe "Default gem specs" do
  it "@path_to_default_spec_map should be non-empty" do
    Gem.instance_variable_get(:@path_to_default_spec_map).empty?.should == false
  end
end
