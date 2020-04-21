# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::KernelOperations.features_index_add" do
  it "creates correct entries in @@loaded_features_index" do
    current_index = Truffle::KernelOperations.instance_variable_get(:@loaded_features_index)
    current_index_backup = current_index.dup
    current_index.clear

    Truffle::KernelOperations.features_index_add('foo', 0)
    current_index.should == {'foo' => [0]}

    Truffle::KernelOperations.features_index_add('foo', 1)
    current_index.should == {'foo' => [0, 1]}
    current_index.clear

    Truffle::KernelOperations.features_index_add('foo.rb', 0)
    Truffle::KernelOperations.features_index_add('foo.rb', 1)
    current_index.should == {'foo' => [0, 1], 'foo.rb' => [0, 1]}
    current_index.clear

    Truffle::KernelOperations.features_index_add('one/two/foo.rb', 0)
    current_index.should == {'foo' => [0], 'foo.rb' => [0], 'two/foo' => [0], 'two/foo.rb' => [0], 'one/two/foo.rb' => [0], 'one/two/foo' => [0] }
    current_index.clear

    Truffle::KernelOperations.features_index_add('/two/foo.rb', 0)
    current_index.should == {'foo' => [0], 'foo.rb' => [0], 'two/foo' => [0], 'two/foo.rb' => [0], '/two/foo.rb' => [0], '/two/foo' => [0] }
    current_index.clear

    Truffle::KernelOperations.instance_variable_set(:@loaded_features_index, current_index_backup)
  end
end

describe "Truffle::KernelOperations.loaded_feature_path" do
  it "returns path for matching feature" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    name = "/path/ruby/lib/ruby/2.6.0/benchmark.rb"
    feature  = "benchmark"
    path = Truffle::KernelOperations.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature  = "benchmark.rb"
    path = Truffle::KernelOperations.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]
  end

  it "returns nil for missing features" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    path = Truffle::KernelOperations.loaded_feature_path("/path/ruby/lib/ruby/2.6.0/benchmark.rb", "missing", load_path)
    path.should == nil

    long_feature = "/path/ruby/lib/ruby/2.6.0/extra-path/benchmark.rb"
    path = Truffle::KernelOperations.loaded_feature_path("/path/ruby/lib/ruby/2.6.0/benchmark.rb", long_feature, load_path)
    path.should == nil
  end

  it "returns correct paths for non-rb paths" do
    load_path = ["/path/ruby/lib/ruby/2.6.0"]
    name = "/path/ruby/lib/ruby/2.6.0/benchmark.so"

    feature = "benchmark"
    path = Truffle::KernelOperations.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature = "benchmark.so"
    path = Truffle::KernelOperations.loaded_feature_path(name, feature, load_path)
    path.should == load_path[0]

    feature = "benchmark.rb"
    path = Truffle::KernelOperations.loaded_feature_path(name, feature, load_path)
    path.should == nil
  end

end
