# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

benchmark 'core-open-close-file' do
  f = File.open('/dev/null')
  f.close()
end

null = File.open('/dev/null', 'w')

kilobyte = 'x' * 1024

if defined?(Truffle::Ropes.flatten_rope)
  kilobyte = Truffle::Ropes.flatten_rope(kilobyte)
end

benchmark 'core-write-kilobyte' do
  null.write kilobyte
end

gigabyte = 'x' * 1024 * 1024 * 1024

if defined?(Truffle::Ropes.flatten_rope)
  gigabyte = Truffle::Ropes.flatten_rope(gigabyte)
end

benchmark 'core-write-gigabyte' do
  null.write gigabyte
end

benchmark 'core-big-concat-and-write' do
  string = '<html>'
  string += '<ul>'
  
  10_000.times do |n|
    string += '<li>' + n.to_s + '</li>'
  end
  
  string += '</ul>'
  string += '</html>'
  null.write string
end

benchmark 'core-big-join-and-write' do
  strings = ['<html>']
  strings.push '<ul>'
  
  10_000.times do |n|
    strings.push '<li>'
    strings.push n.to_s
    strings.push '</li>'
  end
  
  strings.push '</ul>'
  strings.push '</html>'
  null.write strings.join
end

zero = File.open('/dev/zero')

benchmark 'core-read-kilobyte' do
  zero.read 1024
end

benchmark 'core-read-gigabyte' do
  zero.read 1024 * 1024 * 1024
end
