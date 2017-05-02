#!/usr/bin/env ruby
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require 'json'

result = JSON.parse(File.read(ARGV[0]))

result['queries'].each do |q|
  q.delete_if do |k, v|
    k =~ /^extra\.[\w-]+\.commit\.[\w-]+$/ || k =~ /^truffleruby\.commit\./
  end
end

File.write(ARGV[1], JSON.pretty_generate(result))
