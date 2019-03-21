# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Beware, RubyDebugTest use hard-coded line numbers from this file!

def temperature_in_city(name)
  cityArray = name.bytes
  citySum = cityArray.reduce(0, :+)
  weatherTemperature = citySum.modulo(36)
  blt = true
  blf = false
  null = nil
  nm1 = 1
  nm11 = 1.111
  nme = 35e45
  nc = 2 + 3i
  nr = Rational(0.3)
  str = 'A String'
  symbol = :symbolic
  arr = [1, '2', 3.56, blt, nil, str]
  hash = {:a => 1, 'b' => 2}
  nme + nm1
end

temperature_in_city('Panama')
