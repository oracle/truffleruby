# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe Polyglot do
  
  describe ".eval(id, code)" do
    
    it "evals code in Ruby" do
      Polyglot.eval("ruby", "14 + 2").should == 16
    end
    
    it "doesn't work with MIME types" do
      lambda {
        Polyglot.eval("application/x-ruby", "14 + 2")
      }.should raise_error(RuntimeError, /No language for id application\/x-ruby found/)
    end
  
  end

  describe ".eval_file(id, path)" do
    
    it "evals code in Ruby" do
      Polyglot.eval_file("ruby", File.join(File.dirname(__FILE__), "fixtures/eval_file_id.rb"))
      $eval_file_id.should be_true
    end
    
    it "doesn't work with MIME types" do
      lambda {
        Polyglot.eval_file("application/x-ruby", File.join(File.dirname(__FILE__), "fixtures/eval_file_id.rb"))
      }.should raise_error(RuntimeError, /No language for id application\/x-ruby found/)
    end
  
  end

  describe ".eval_file(path)" do
    
    it "evals code in Ruby" do
      Polyglot.eval_file(File.join(File.dirname(__FILE__), "fixtures/eval_file.rb"))
      $eval_file.should be_true
    end
  
  end

  describe ".as_enumerable" do
    
    it "evals code in Ruby" do
      enumerable = lambda { Polyglot.as_enumerable([1, 2, 3]) }
      enumerable.call.min.should == 1
      enumerable.call.max.should == 3
    end
  
  end

end
