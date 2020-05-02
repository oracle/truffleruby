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

describe "Truffle::GemUtil::DEFAULT_GEMS" do
  it "is a Hash listing all default gem paths" do
    default_spec_paths = Gem.instance_variable_get(:@path_to_default_spec_map).keys
    hash = Dir.children("#{Gem.default_dir}/specifications/default").sort.map do |spec|
      spec.rpartition("-").first
    end.select do |name|
      default_spec_paths.include?(name)
    end.to_h { |name| [name, true] }

    Truffle::GemUtil::DEFAULT_GEMS.should == hash
  end
end
