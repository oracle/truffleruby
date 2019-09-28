#!/usr/bin/env ruby

# You can call this from .git/hooks/pre-commit with
# exec tool/cleanup-imports.rb

STDERR.puts "Removing unused imports ..."

output = `mx checkstyle -f --primary 2>&1`
status = $?

output.lines.reverse.grep(/: Unused import -/) do |line|
  path, lineno, _ = line.split(':', 3)
  lineno = Integer(lineno)

  lines = File.readlines path
  lines.delete_at(lineno-1)
  File.write path, lines.join
end

unless status.success?
  STDERR.puts output
  exit status.exitstatus
end
