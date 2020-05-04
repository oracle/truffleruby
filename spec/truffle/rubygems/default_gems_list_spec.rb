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
    hash = Dir.children("#{Gem.default_dir}/specifications/default").sort.map do |spec|
      spec.split("-").first # 'io' for gem 'io-console' required as 'io/console'
    end.to_h { |name| [name, true] }

    Truffle::GemUtil::DEFAULT_GEMS.should == hash
  end
end
