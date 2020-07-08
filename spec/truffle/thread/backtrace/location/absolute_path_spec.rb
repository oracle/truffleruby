# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../../ruby/spec_helper'

describe 'Thread::Backtrace::Location#absolute_path' do

  it 'returns an existing and canonical path for core methods' do
    # Get the caller_locations from a call made into a core method.
    locations = [:non_empty].map { caller_locations }.flatten

    locations.each do |location|
      path = location.absolute_path
      path.should_not.include?('(core)')

      if path.include?('resource:')
        skip
      else
        File.should.exist?(location.absolute_path)
        File.realpath(location.absolute_path).should == location.absolute_path
      end
    end
  end

end
