# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::FeatureLoader.features_index_add" do
  it "creates correct entries in @loaded_features_index" do
    current_index = Truffle::FeatureLoader.instance_variable_get(:@loaded_features_index)
    current_index_backup = current_index.dup
    current_index.clear

    Truffle::FeatureLoader.features_index_add('foo', 0)
    current_index[Truffle::FeatureLoader::FeatureEntry.new('foo')].should == [0]

    Truffle::FeatureLoader.features_index_add('foo', 1)
    current_index[Truffle::FeatureLoader::FeatureEntry.new('foo')].should == [0, 1]
    current_index.clear

    Truffle::FeatureLoader.features_index_add('foo.rb', 0)
    Truffle::FeatureLoader.features_index_add('foo.rb', 1)
    current_index[Truffle::FeatureLoader::FeatureEntry.new('foo')].should == [0, 1]
    current_index[Truffle::FeatureLoader::FeatureEntry.new('foo.rb')].should == [0, 1]
    current_index.clear

    Truffle::FeatureLoader.features_index_add('one/two/foo.rb', 0)
    ['foo', 'foo.rb', 'two/foo', 'two/foo.rb', 'one/two/foo.rb', 'one/two/foo'].each do |feature|
      current_index[Truffle::FeatureLoader::FeatureEntry.new(feature)].should == [0]
    end
    current_index.clear

    Truffle::FeatureLoader.features_index_add('/two/foo.rb', 0)
    ['foo', 'foo.rb', 'two/foo', 'two/foo.rb', '/two/foo.rb', '/two/foo'].each do |feature|
      current_index[Truffle::FeatureLoader::FeatureEntry.new(feature)].should == [0]
    end
    current_index.clear
  ensure
    Truffle::FeatureLoader.instance_variable_set(:@loaded_features_index, current_index_backup)
  end
end

describe "Truffle::FeatureLoader.loaded_feature_path" do
  it "returns path for matching feature" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    name = "/path/ruby/lib/ruby/2.6.0/benchmark.rb"
    feature  = "benchmark"
    path = Truffle::FeatureLoader.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature  = "benchmark.rb"
    path = Truffle::FeatureLoader.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]
  end

  it "returns nil for missing features" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    path = Truffle::FeatureLoader.loaded_feature_path("/path/ruby/lib/ruby/2.6.0/benchmark.rb", "missing", load_path)
    path.should == nil

    long_feature = "/path/ruby/lib/ruby/2.6.0/extra-path/benchmark.rb"
    path = Truffle::FeatureLoader.loaded_feature_path("/path/ruby/lib/ruby/2.6.0/benchmark.rb", long_feature, load_path)
    path.should == nil
  end

  it "returns correct paths for non-rb paths" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    name = "/path/ruby/lib/ruby/2.6.0/benchmark.so"

    feature = "benchmark"
    path = Truffle::FeatureLoader.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature = "benchmark.so"
    path = Truffle::FeatureLoader.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature = "benchmark.rb"
    path = Truffle::FeatureLoader.loaded_feature_path(name, feature, load_path)
    path.should == nil
  end

end

describe "Truffle::FeatureLoaderFeatureEntry" do
  it "only a nested path at the end should match" do
    hash = {}
    stored_entry = Truffle::FeatureLoader::FeatureEntry.new("path/to/feature")
    hash[stored_entry] = true
    stored_entry.part_of_index = true

    short_lookup_entry = Truffle::FeatureLoader::FeatureEntry.new("to/feature")
    hash[short_lookup_entry].should be_true

    exact_lookup_entry = Truffle::FeatureLoader::FeatureEntry.new("path/to/feature")
    hash[exact_lookup_entry].should be_true


    longer_lookup_entry = Truffle::FeatureLoader::FeatureEntry.new("long/path/to/feature")
    hash[longer_lookup_entry].should be_nil

    prefix_lookup_entry = Truffle::FeatureLoader::FeatureEntry.new("path/to")
    hash[prefix_lookup_entry].should be_nil
  end

  describe "when stored has an extension" do
    it "matches a lookup with or without extension" do
      hash = {}
      stored_entry = Truffle::FeatureLoader::FeatureEntry.new("path/to/feature.so")
      hash[stored_entry] = true
      stored_entry.part_of_index = true


      lookup_entry_no_ext = Truffle::FeatureLoader::FeatureEntry.new("to/feature")
      hash[lookup_entry_no_ext].should be_true

      lookup_entry_ext = Truffle::FeatureLoader::FeatureEntry.new("to/feature.so")
      hash[lookup_entry_ext].should be_true

      lookup_entry_wrong_ext = Truffle::FeatureLoader::FeatureEntry.new("to/feature.rb")
      hash[lookup_entry_wrong_ext].should be_nil
    end
  end

  it "raises an error when both keys are lookup" do
    hash = {}
    stored_entry = Truffle::FeatureLoader::FeatureEntry.new("path/to/feature")
    hash[stored_entry] = true

    short_lookup_entry = Truffle::FeatureLoader::FeatureEntry.new("to/feature")
    -> { hash[short_lookup_entry] }.should raise_error
  end
end
