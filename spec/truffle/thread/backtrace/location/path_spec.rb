# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../../ruby/spec_helper'

describe 'Thread::Backtrace::Location#path' do

  it 'should be the same path as in the formatted description for core methods' do
    # Get the caller_locations from a call made into a core method.
    locations = [:non_empty].map { caller_locations }.flatten

    locations.each do |location|
      filename = location.to_s[/^(.+):\d+:/, 1]
      path = location.path
      path.should_not.include?('(core)')
      path.should_not.include?('resource:')

      # #path is consistent with #to_s output, like on MRI
      path.should == filename
    end
  end

end
