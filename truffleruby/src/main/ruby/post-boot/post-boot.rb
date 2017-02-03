# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

begin
  require 'unicode_normalize'
rescue LoadError
end

if Truffle::Boot.rubygems_enabled?
  begin
    require 'rubygems'
  rescue LoadError
  else
    if Truffle::Boot.did_you_mean_enabled?
      begin
        gem 'did_you_mean'
        require 'did_you_mean'
      rescue Gem::LoadError, LoadError => e
      end
    end
  end
end
