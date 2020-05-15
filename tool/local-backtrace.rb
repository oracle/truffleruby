#!/usr/bin/env ruby
root = File.dirname(__dir__)
contents = STDIN.read
contents = contents.gsub("/b/b/e/main", ".")
contents = contents.gsub("/mxbuild/truffleruby-jvm/jre/languages/ruby/", "/")
STDOUT.puts contents
