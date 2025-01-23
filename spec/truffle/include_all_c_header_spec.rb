# Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe 'lib/cext/include/internal_all.h' do
  it 'includes each *.h file from lib/cext/include/internal/' do
    filenames = Dir.glob('internal/**/*.h', base: 'lib/cext/include', sort: true)
    content = File.read('lib/cext/include/internal_all.h')

    filenames.should_not be_empty

    missing = filenames.reject { |filename| content.include? "#include <#{filename}>" }
    missing.should be_empty
  end

  it 'includes each *.h file from lib/cext/include/stubs/internal/' do
    filenames = Dir.glob('internal/**/*.h', base: 'lib/cext/include/stubs', sort: true)
    content = File.read('lib/cext/include/internal_all.h')

    filenames.should_not be_empty

    missing = filenames.reject { |filename| content.include? "#include <#{filename}>" }
    missing.should be_empty
  end
end
