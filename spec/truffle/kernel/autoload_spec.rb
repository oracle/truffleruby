# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Kernel#autoload" do
  # https://github.com/oracle/truffleruby/issues/1905
  it "does not cause require to raise an error with a matching path" do
    autoload :FixtureTruffleAutoLoadSpec, "fixtures/autoload_const_issue"

    $LOAD_PATH.unshift(File.expand_path('../', __FILE__ ))

    -> { require_relative "fixtures/autoload_const_issue" }.should_not raise_error
  end
end
