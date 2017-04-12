# Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module LastMatchFixtures

  MD = /(Rob)/.match "Robert"

  def self.foo()
    set_last_match(MD)
  end

  def self.set_last_match(data)
    Truffle.invoke_primitive(:regexp_set_last_match, data)
    data
  end

end

example "LastMatchFixtures.foo", LastMatchFixtures::MD
example "LastMatchFixtures.send(:foo)", LastMatchFixtures::MD
