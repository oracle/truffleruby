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

# Only set to true for faster development if this spec is run alone
run_directly = false

modules = [
  BasicObject, Object,
  Enumerable, Enumerator,
  Numeric,
]
# Kernel
# Array, Hash, Range, String
# Fixnum, Bignum, Integer, Float, Rational, Complex

describe "Public methods on" do
  modules.each do |mod|
    it "#{mod.name} are the same as on MRI" do
      if run_directly
        methods = mod.public_instance_methods(false).sort
      else
        methods = ruby_exe("puts #{mod}.public_instance_methods(false).sort")
        methods = methods.lines.map { |line| line.chomp.to_sym }
      end

      file = File.expand_path("../methods/#{mod.name}.txt", __FILE__)

      if RUBY_ENGINE == "ruby"
        contents = methods.map { |meth| "#{meth}\n" }.join
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
