# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

# Run
# jt test spec/truffle/methods_specs.rb -t ruby
# to regenerate the files under methods/.

modules = [Object]
# BasicObject, Kernel
# Array, Hash, Range, String, Enumerable, Enumerator
# Numeric, Fixnum, Bignum, Integer, Float, Rational, Complex

describe "Public methods on" do
  modules.each do |mod|
    it "#{mod.name} are the same as on MRI" do
      methods = mod.public_instance_methods(false).sort
      file = File.expand_path("../methods/#{mod.name}.txt", __FILE__)

      if RUBY_ENGINE == "ruby"
        contents = methods.join("\n") + "\n"
        File.write file, contents
        1.should == 1
      else
        expected = File.readlines(file).map { |line| line.chomp.to_sym }
        unless methods == expected
          (methods - expected).should == []
          (expected - methods).should == []
        end
        methods.should == expected
      end
    end
  end
end
