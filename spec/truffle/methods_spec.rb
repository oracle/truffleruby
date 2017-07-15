# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

# Switch to MRI 2.3.3 and run:
# $ jt test spec/truffle/methods_spec.rb -t ruby
# to regenerate the files under methods/.

# Only set to true for faster development if this spec is run alone
run_directly = false

modules = [
  BasicObject, Kernel, Object,
  Enumerable, Enumerator,
  Numeric, Integer, Fixnum, Bignum,
  Array,
]
# Hash, Range, String
# Float, Rational, Complex

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
        methods.delete(:yield_self) if mod == Kernel

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
