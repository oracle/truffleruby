#!/usr/bin/env ruby
## Experimental MRI test tagging script from a test run log
## The input is read from stdin or the first argument

# Usage:
#     tool/parse_mri_errors.rb output.txt
# or
#     jt test mri test/mri/tests/rdoc/test_rdoc_token_stream.rb | tool/parse_mri_errors.rb

REASON = ENV['REASON']

contents = ARGF.read

load_error_output = "0 tests, 0 assertions, 0 failures, 0 errors, 0 skips"

summary_regex = /\d+\stests,\s\d+\sassertions,\s\d+\sfailures,\s\d+\serrors,\s\d+\sskips/
split_errors = contents.split(load_error_output)
if split_errors.size > 1
  puts "split_errors #{split_errors.size}"
  err_files = split_errors.map { |e| e.scan(/filesf \[\"(.*)\"\]/).last[0] }
  patt = err_files.map { |err| err.split("/mri/")[1] }

  all_tests = contents.scan(/filesf \[\"(.*)\"\]/)
  all_tests_patt = all_tests.map { |err| err[0].split("/mri/")[1] }

  non_excluded = all_tests_patt - patt

  puts "# Test index"

  i_hash = Hash[non_excluded.map { |v| [v, true] }]
  e_hash = Hash[patt.map { |v| [v, false] }]

  all_hash = i_hash.merge(e_hash)
  all_hash = Hash[all_hash.sort_by{ |k,v| k }]
  all_hash.each do |k,v|
    if v
      puts k
    else
      puts "# #{k}"
    end
  end

end

require 'fileutils'

repo_root = File.expand_path("../..", __FILE__)
excludes = "#{repo_root}/test/mri/excludes"

t = /((?:\w+::)?\w+)#(.+?)(?:\s*\[(?:[^\]])+\])?:\n(.+)\n/
contents.scan(t) do |class_name, test_method, error|
  file = excludes + "/" + class_name.split("::").join('/') + ".rb"
  prefix = "exclude #{test_method.strip.to_sym.inspect}"
  new_line = "#{prefix}, #{(REASON || error).inspect}\n"

  FileUtils.mkdir_p(File.dirname(file))
  lines = File.exist?(file) ? File.readlines(file) : []
  if i = lines.index { |line| line.start_with?(prefix) }
    lines[i] = new_line
  else
    lines << new_line
  end
  File.write(file, lines.join)
end
