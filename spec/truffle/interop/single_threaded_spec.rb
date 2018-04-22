# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Single-threaded mode" do

  it "should give a helpful error if a thread is created" do
    ruby_exe(
      "Thread.new { }",
      options: "-Xsingle_threaded=true",
      args: '2>&1'
    ).should =~ /threads not allowed in single-threaded mode/
  end
    
  describe "allows native memory finalisers created" do
    
    it "before interop" do
      ruby_exe(
        "Truffle::FFI::Pointer.new(14); puts Polyglot.eval('ruby-single-threaded-test', '14')",
        options: "-Xsingle_threaded=true"
      ).should == "14\n"
    end
    
    it "after interop" do
      ruby_exe(
        "Polyglot.eval('ruby-single-threaded-test', '14'); Truffle::FFI::Pointer.new(14); puts 14",
        options: "-Xsingle_threaded=true"
      ).should == "14\n"
    end
    
  end
  
  describe "allows files to be read" do

    it "before interop" do
      ruby_exe(
        "File.read('README.md'); Polyglot.eval('ruby-single-threaded-test', '14'); puts 14",
        options: "-Xsingle_threaded=true"
      ).should == "14\n"
    end
    
    it "after interop" do
      ruby_exe(
        "Polyglot.eval('ruby-single-threaded-test', '14'); File.read('README.md'); puts 14",
        options: "-Xsingle_threaded=true"
      ).should == "14\n"
    end
    
  end

end

describe "Interop with a single-threaded language in non-single-threaded mode" do
  
  it "should give a helpful error if a thread has already been created" do
    ruby_exe(
      "thread = Thread.new { }; Polyglot.eval('ruby-single-threaded-test', '14')",
      options: "-Xsingle_threaded=false",
      args: '2>&1'
    ).should =~ /Try running Ruby in single-threaded mode by using -Xsingle_threaded or --ruby.single_threaded./
  end
  
  it "should give a helpful error if a thread is created later" do
    ruby_exe(
      "Polyglot.eval('ruby-single-threaded-test', '14'); thread = Thread.new { };",
      options: "-Xsingle_threaded=false",
      args: '2>&1'
    ).should =~ /Are you attempting to create a Ruby thread after you have used a single-threaded language?/
  end

  describe "allows native memory finalisers created" do
    
    it "before interop" do
      ruby_exe(
        "Truffle::FFI::Pointer.new(14); puts Polyglot.eval('ruby-single-threaded-test', '14')",
        options: "-Xsingle_threaded=false"
      ).should == "14\n"
    end
    
    it "after interop" do
      ruby_exe(
        "Polyglot.eval('ruby-single-threaded-test', '14'); Truffle::FFI::Pointer.new(14); puts 14",
        options: "-Xsingle_threaded=false"
      ).should == "14\n"
    end
    
  end
  
  describe "allows files to be read" do

    it "before interop" do
      ruby_exe(
        "File.read('README.md'); Polyglot.eval('ruby-single-threaded-test', '14'); puts 14",
        options: "-Xsingle_threaded=false"
      ).should == "14\n"
    end
    
    it "after interop" do
      ruby_exe(
        "Polyglot.eval('ruby-single-threaded-test', '14'); File.read('README.md'); puts 14",
        options: "-Xsingle_threaded=false"
      ).should == "14\n"
    end
    
  end

end
