# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require 'perfer'

require File.expand_path('clamp', File.dirname(__FILE__))

Perfer.session 'clamp' do |s|
  s.iterate 'clamp_a' do |n|
    n.times do
      clamp_a(10, 40, 90)
    end
  end

  s.iterate 'clamp_b' do |n|
    n.times do
      clamp_b(10, 40, 90)
    end
  end
end
