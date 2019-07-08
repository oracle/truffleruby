# frozen_string_literal: true

# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Defaults for RubyGems when running on TruffleRuby.
# This file is required by RubyGems when RubyGems is loaded.

module Gem
  # The path to the gems shipped with TruffleRuby
  def self.default_dir
    @default_dir ||= "#{Truffle::Boot.ruby_home or raise 'TruffleRuby home not found'}/lib/ruby/gems/#{Truffle::RUBY_BASE_VERSION}"
  end
end
