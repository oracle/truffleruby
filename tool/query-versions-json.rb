#!/usr/bin/env ruby

require 'json'

json = JSON.parse(File.read(File.join(File.dirname(__FILE__), '../versions.json')))
json['ruby']['base'] = json['ruby']['version'].gsub(/\.\d+$/, '.0')

ARGV.each do |query|
  object = json
  query.split('.').each do |p|
    object = object[p]
  end
  puts object
end
