#!/usr/bin/env ruby

# Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Usage
#   1. `jt ruby tool/compare-rbconfig.rb`
#   2. `jt -u ruby ruby tool/compare-rbconfig.rb`
#   3. Compare ruby.txt and truffleruby.txt in your diff tool.

require 'rbconfig'

File.open("#{RUBY_ENGINE}.txt", "w") do |f|
  f.puts RUBY_VERSION

  prefix = RbConfig::CONFIG['prefix']
  toolchain_path = RbConfig::CONFIG['toolchain_path']

  {
    # 'RbConfig::CONFIG' => RbConfig::CONFIG,
    'RbConfig::MAKEFILE_CONFIG' => RbConfig::MAKEFILE_CONFIG
  }.each do |name, h|
    f.puts
    f.puts name
    h.keys.sort.each { |k|
      value = h[k]

      value = value.gsub(prefix, '$(prefix)')
      value = value.gsub(toolchain_path, '$(toolchain_path)') if toolchain_path

      f.puts "#{k} = #{value}"
    }
  end
end
