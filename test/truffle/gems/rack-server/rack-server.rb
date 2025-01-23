# Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'rack'

class Example
  def call(env)
    return [200, {}, ["Hello Rack!\n"]]
  end
end

Rack::Handler::WEBrick.run(Example.new, Host: '127.0.0.1', Port: 0)
