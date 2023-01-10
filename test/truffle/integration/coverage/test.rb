# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'coverage'

Coverage.start

require_relative 'subject.rb'

result = Coverage.result
key = result.keys.find { |k| k.end_with?('subject.rb') }
data = result[key]
expected = [nil, nil, nil, nil, nil, nil, nil, nil, 1, 1, nil, 1, 10, nil, nil, 1, nil, 1, 1, nil, nil, 1, 2, nil, nil, 1, 1, nil, 1]

raise "coverage data:\n#{data}\nnot as expected:\n#{expected}" unless data == expected
