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

# Usually the first line in the error message gives us enough context to quickly identify what caused the failure.
# Sometimes, the first line isn't helpful and we need to look further down. This filter helps us discard unhelpful data.
def should_skip_error?(message)
  Regexp.union(
    /\[ruby-\w+:\d+\]/i,   # MRI bug IDs.
    /\[Bug #?\d+\]/i,      # MRI bug IDs.
    /pid \d+ exit \d/i,    # PID and exit status upon failure.
    /^Exception raised:$/i # Heading for exception trace.
  ).match(message)
end

t = /^((?:\w+::)*\w+)#(.+?)(?:\s*\[(?:[^\]])+\])?:\n(.*?)\n$/m
contents.scan(t) do |class_name, test_method, error|
  error_lines = error.split("\n")
  index = 0

  while should_skip_error?(error_lines[index])
    index += 1
  end

  error_display = error_lines[index]

  # Mismatched expectations span two lines. It's much more useful if they're combined into one message.
  if error_display =~ /expected but was/ || error_display =~ /expected:/
    error_display << ' ' + error_lines[index + 1]
  end

  file = excludes + "/" + class_name.split("::").join('/') + ".rb"
  prefix = "exclude #{test_method.strip.to_sym.inspect}"
  new_line = "#{prefix}, #{(REASON || error_display || "needs investigation").inspect}\n"

  FileUtils.mkdir_p(File.dirname(file))
  lines = File.exist?(file) ? File.readlines(file) : []

  # we need the ',' to handle a case when one test name is a substring of another test name
  if i = lines.index { |line| line.start_with?(prefix + ",") }
    puts "already excluded: #{class_name}##{test_method}"
    lines[i] = new_line
  else
    puts "adding exclude: #{class_name}##{test_method}"
    lines << new_line
  end
  File.write(file, lines.join)
end
