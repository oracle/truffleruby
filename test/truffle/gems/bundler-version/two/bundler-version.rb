# Copyright (c) 2023, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'bundler/setup'

require 'json'
versions = File.expand_path('../../../../../versions.json', __dir__)
expected = JSON.load(File.read(versions)).dig('gems', 'default', 'bundler')

p Bundler::VERSION == expected
