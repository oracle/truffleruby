# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

describe "RubyGems" do
  it "is not loaded until needed" do
    ruby_exe('puts $"').should_not include('/rubygems.rb')
    ruby_exe('p autoload? :Gem').should == "\"rubygems\"\n"
  end

  # This spec needs no upgraded gems installed
  it "is not loaded for default gems if there is no upgraded default gem" do
    default_gems = []
    keep_hyphen = %w[open-uri readline-ext resolv-replace]

    Truffle::GemUtil::DEFAULT_GEMS.each_pair do |prefix, names|
      if names == true
        default_gems << prefix
      else
        default_gems.concat(names.map do |name|
          keep_hyphen.include?(name) ? name : name.tr('-', '/')
        end)
      end
    end
    default_gems -= [
      'bundler', # explicitly requires RubyGems
      'dbm', 'gdbm', # not available
      'debug', # not available
      'readline-ext', # readline.so on CRuby, we have no readline C-ext
    ]
    default_gems[default_gems.index('english')] = 'English'
    default_gems[default_gems.index('rinda')] = 'rinda/rinda'

    code = <<-RUBY
    #{default_gems.inspect}.each do |name|
      require name
      unless autoload?(:Gem) == "rubygems"
        puts $LOADED_FEATURES
        abort "\#{name} loaded RubyGems"
      end
    end
    puts 'OK'
    RUBY
    ruby_exe(code, args: "2>&1").should == "OK\n"
    $?.success?.should == true
  end

  it "is loaded when accessing Gem" do
    ruby_exe('Gem; puts $"').should include('/rubygems.rb')
    ruby_exe('Gem; p autoload? :Gem').should == "nil\n"
  end

  it "is loaded by a failing require" do
    code = 'begin; require "lrg-does-not-exist"; rescue LoadError; puts $"; end'
    ruby_exe(code).should include('/rubygems.rb')
    $?.success?.should == true
  end
end

describe "Lazy RubyGems" do
  # See https://github.com/rubygems/rubygems/issues/2772
  it "defines StringIO like RubyGems which requires it eagerly" do
    ruby_exe('puts StringIO').should == "StringIO\n"
  end

  it "works for require 'rubygems/package'" do
    ruby_exe("require 'rubygems/package'; p Gem::Package").should == "Gem::Package\n"
  end

  it "works for require 'rubygems/specification'" do
    ruby_exe("require 'rubygems/specification'; p Gem::Specification").should == "Gem::Specification\n"
  end
end
