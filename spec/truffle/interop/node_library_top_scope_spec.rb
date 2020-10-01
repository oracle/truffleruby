# truffleruby_primitives: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
#

# rubocop:disable Lint/UselessAssignment

a = "a"

describe "NodeLibrary top scope" do
  it "returns the members" do
    b = "b"
    def get_top_scope
      c = "c"
      Primitive.top_scope
    end
    top_scope = get_top_scope()
    scope_members = Truffle::Interop.members(top_scope)
    ["$stdout", "is_a?"].all? { |m| scope_members.include?(m) }.should == true
    top_scope["$stdout"].should == $stdout
    top_scope["is_a?"].should be_kind_of(Method)
  end
end

# rubocop:enable Lint/UselessAssignment
