# Copyright (c) 2022, 2022 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

guard -> { !TruffleRuby.native? } do
  describe "Java.add_to_classpath" do
    before :all do
      jar_dir = File.expand_path(File.dirname(__FILE__)) + '/fixtures/examplejar'
      bin_dir = TruffleRuby.graalvm_home + '/bin'
      Dir.chdir(jar_dir) do
        system("#{bin_dir}/javac org/truffleruby/examplejar/Example.java")
        system("#{bin_dir}/jar cf example.jar org/truffleruby/examplejar/Example.class")
      end
      @jar_file = jar_dir + '/example.jar'
    end

    it "loads a jar file" do
      Java.add_to_classpath(@jar_file).should == true
      example_object = Java.type('org.truffleruby.examplejar.Example').new
      example_object.hello("Spec").should == "Hello Spec"
    end
  end
end
