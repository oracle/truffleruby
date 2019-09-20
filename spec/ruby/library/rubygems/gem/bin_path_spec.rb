require_relative '../../../spec_helper'
require 'rubygems'

describe "Gem.bin_path" do
  it "finds executables of default gems, which are the only files shipped for default gems" do
    # For instance, Gem.bin_path("bundler", "bundle") is used by rails new
    Gem::Specification.each_spec([Gem::Specification.default_specifications_dir]) do |spec|
      spec.executables.each do |exe|
        path = Gem.bin_path(spec.name, exe)
        File.exist?(path).should == true
      end
    end
  end
end
