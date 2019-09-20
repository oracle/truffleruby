#!/usr/bin/env ruby

# This tool makes comparing JSON files with CI definitions easier.
# The json output can be retrieved with `jsonnet ci.jsonnet > a.json`
#
# Usage: tool/compare_json_ci_files.rb a.json b.json [job-name-prefix]

a = ARGV[0]
b = ARGV[1]
job_name_prefix = ARGV[2]

require 'json'

a_data = JSON.load(File.read(a))
b_data = JSON.load(File.read(b))

a_build, b_build = [a_data, b_data].map do |data|
  data['builds'].
      select { |job| job_name_prefix.nil? || job['name'].match(/^#{job_name_prefix}/) }.
      sort_by { |job| job['name'] }.
      each { |job| job['environment'].delete('PARTS_INCLUDED_IN_BUILD') }
end

a_part = a.gsub('.json', '-ordered.json')
b_part = b.gsub('.json', '-ordered.json')
File.write a_part, JSON.pretty_generate(a_build)
File.write b_part, JSON.pretty_generate(b_build)

system "idea diff #{b_part} #{a_part}"

