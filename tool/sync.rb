#!/usr/bin/env ruby

# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# A script to synchronize changes from the Ruby source files
# to the GraalVM build instantaneously.

# Use 'gem install listen' to install this dependency
require 'listen'

require 'fileutils'

FROM = File.expand_path('../..', File.realpath(__FILE__))
TO = "#{FROM}/mxbuild/graalvm/jre/languages/ruby"

# Must be consistent with TRUFFLERUBY_GRAALVM_SUPPORT in suite.py
DIRS_TO_SYNC = %w[
  lib/json
  lib/mri
  lib/patches
  lib/truffle
].map { |path| "#{FROM}/#{path}" }

listener = Listen.to(*DIRS_TO_SYNC) do |modified, added, removed|
  (modified + added).each do |file|
    target = "#{TO}#{file[FROM.size..-1]}"
    FileUtils::Verbose.cp file, target
  end
  removed.each do |file|
    target = "#{TO}#{file[FROM.size..-1]}"
    FileUtils::Verbose.rm target
  end
end

listener.start # not blocking
sleep
