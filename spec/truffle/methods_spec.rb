# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

# Switch to MRI, the version we are compatible with, and run:
# $ jt test spec/truffle/methods_spec.rb -t ruby
# to regenerate the files under methods/.

modules = [
  BasicObject, Kernel, Object,
  Enumerable, Enumerator,
  Numeric, Integer,
  Array,
]
# Hash, Range, String
# Float, Rational, Complex

describe "Public methods on" do
  modules.each do |mod|
    describe "#{mod.name}" do
      file = File.expand_path("../methods/#{mod.name}.txt", __FILE__)

      methods = ruby_exe("puts #{mod}.public_instance_methods(false).sort")
      methods = methods.lines.map { |line| line.chomp.to_sym }

      if RUBY_ENGINE == "ruby"
        contents = methods.map { |meth| "#{meth}\n" }.join
        File.write file, contents
      else
        expected = File.readlines(file).map { |line| line.chomp.to_sym }
        if methods == expected
          it "are the same as on MRI" do
            methods.should == expected
          end
        else
          (methods - expected).each do |extra|
            it "should not include #{extra}" do
              methods.should_not include(extra)
            end
          end
          (expected - methods).each do |missing|
            it "should include #{missing}" do
              methods.should include(missing)
            end
          end
        end
      end
    end
  end
end
