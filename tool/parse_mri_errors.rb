#!/usr/bin/env ruby
## Experimental MRI test tagging script from a test run log
## The input is read from stdin or the first argument

# Usage:
#     tool/parse_mri_errors.rb output.txt
# or
#     jt test mri test/mri/tests/rdoc/test_rdoc_token_stream.rb | tool/parse_mri_errors.rb

REASON = ENV['REASON']
SLOW_TEST_THRESHOLD = 30
VERY_SLOW_TEST_THRESHOLD = 60

contents = ARGF.read.scrub
load_error_output = "0 tests, 0 assertions, 0 failures, 0 errors, 0 skips"

require 'etc'
require 'fileutils'

REPO_ROOT = File.expand_path("../..", __FILE__)
EXCLUDES_DIR = "#{REPO_ROOT}/test/mri/excludes"

# Usually the first line in the error message gives us enough context to quickly identify what caused the failure.
# Sometimes, the first line isn't helpful and we need to look further down. This filter helps us discard unhelpful data.
def should_skip_error?(message)
  Regexp.union(
    /\[ruby-\w+:\d+\]/i,   # MRI bug IDs.
    /\[Bug #?\d+\]/i,      # MRI bug IDs.
    /\[bug:\d+\]/i,        # MRI bug IDs.
    /pid \d+ exit \d/i,    # PID and exit status upon failure.
    /^Exception raised:$/i # Heading for exception trace.
  ).match(message)
end

def platform_info
  if RUBY_PLATFORM.include?('darwin')
    cpu_model = `sysctl -n machdep.cpu.brand_string`.strip
  elsif RUBY_PLATFORM.include?('linux')
    cpu_model = `cat /proc/cpuinfo | grep 'model name' | uniq`.split(':').last.strip
  else
    cpu_model = 'unknown'
  end

  "#{cpu_model}: (#{Etc.nprocessors} vCPUs)"
end

def exclude_test!(class_name, test_method, error_display)
  file = EXCLUDES_DIR + "/" + class_name.split("::").join('/') + ".rb"
  prefix = "exclude #{test_method.strip.to_sym.inspect},"
  new_line = "#{prefix} #{(REASON || error_display).inspect}\n"

  FileUtils.mkdir_p(File.dirname(file))
  lines = File.exist?(file) ? File.readlines(file) : []

  # we need the ',' to handle a case when one test name is a substring of another test name
  if i = lines.index { |line| line.start_with?(prefix) }
    puts "already excluded: #{class_name}##{test_method}"
    lines[i] = new_line
  else
    puts "adding exclude: #{class_name}##{test_method}"
    lines << new_line
  end

  File.write(file, lines.sort.join)
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

  # Mismatched exception messages span three lines. It's much more useful if they're combined into one message.
  if error_display =~ /but the message doesn't match/
    error_display << ' ' + error_lines[index + 1] + ' ' + error_lines[index + 2]
  end

  # Assertion errors are more useful with the first line of the backtrace.
  if error_display&.include?('java.lang.AssertionError')
    error_display << ' ' << error_lines[index + 1]
  end

  # Generated Markdown code blocks span multiple lines. It's much more useful if they're combined into one message.
  if error_display =~ /```/
    index += 1

    begin
      until (line = error_lines[index]) =~ /```/
        error_display << line << "\n"
        index += 1
      end

      error_display << line
    rescue
      error_display = "needs investigation; multi-line code block"
    end
  end

  if error_display.to_s.strip.empty?
    error_display = "needs investigation"
  end

  exclude_test!(class_name, test_method, error_display)
end


# Tag slow tests.

test_ruby_version = contents.match(/ruby -v: (.*)/)[1].strip

t = /^\[\s*\d+\/\d+\] ((?:\w+::)*\w+)#(.+?) = (\d+\.\d+) s/
contents.scan(t) do |class_name, test_method, execution_time|
  if execution_time.to_f > SLOW_TEST_THRESHOLD
    prefix =  execution_time.to_f > VERY_SLOW_TEST_THRESHOLD ? "very slow" : "slow"
    message = "#{prefix}: #{execution_time}s on #{test_ruby_version} with #{platform_info}"

    exclude_test!(class_name, test_method, message)
  end
end
