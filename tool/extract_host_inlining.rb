# Example:
# ruby tool/extract_host_inlining.rb org.truffleruby.language.dispatch.RubyCallNode.execute host-inlining.txt

simplify = ARGV.delete '--simplify'

raise unless ARGV.size == 2
method, file = ARGV

all_lines = File.readlines(file)

method = "Root[#{method}]" unless method.start_with?('Root[')
method = "            #{method.strip}\n"
blank_line = "            \n"

lines = []
while i = all_lines.index(method)
  start = i - 1

  # i += 1 until lines[i].strip.empty?
  i += 1 until all_lines[i] == blank_line

  lines.concat all_lines[start...i]
  lines << "\n"
  all_lines = all_lines[i..-1]
end
raise "not found" if lines.empty?
lines.pop # Remove last \n

indent = lines.first[/^ +/]
lines = lines.map { |line| line.sub(indent, '') }
lines = lines.map { |line| line.sub(/^ {2}/, '') }
lines = lines.reject { |line|
  line.include?(' com.oracle.truffle.api.CompilerDirectives.transferToInterpreterAndInvalidate()') or
  line.include?(' com.oracle.truffle.api.CompilerDirectives.inInterpreter()')
}

if simplify
  lines = lines.reject { |line|
    line.include?(' org.truffleruby.language.RubyBaseNode.coreLibrary()')
  }

  lines = lines.reject { |line|
    line.include?('CUTOFF') and (
      line.include?('reason protected by inInterpreter()') or
      line.include?('reason dominated by transferToInterpreter()') or
      line.include?('reason truffle boundary') or
      line.include?('reason method annotated with @InliningCutoff')
    )
  }

  lines = lines.reject { |line|
    line.include?('DEAD') and line.include?('reason the invoke is dead code')
  }
end

# Reduce line width for unimportant information
lines = lines.map { |line|
  line.sub(/\[inlined\s.+?invoke\s+(true|false),/, '')
      .sub(/,\s*incomplete\s+(true|false),/, ',')
}

puts lines.join
