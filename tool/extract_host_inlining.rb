raise unless ARGV.size == 2
method, file = ARGV

lines = File.readlines(file)


method = "Root[#{method}]" unless method.start_with?('Root[')
method = "            #{method.strip}\n"
blank_line = "            \n"

i = lines.index(method)
raise "not found" unless i
start = i - 1

# i += 1 until lines[i].strip.empty?
i += 1 until lines[i] == blank_line

lines = lines[start...i]

indent = lines.first[/^ +/]
lines = lines.map { |line| line.sub(indent, '') }
lines = lines.map { |line| line.sub(/^ {2}/, '') }
lines = lines.reject { |line|
  line.include?(' com.oracle.truffle.api.CompilerDirectives.transferToInterpreterAndInvalidate()') or
    line.include?(' com.oracle.truffle.api.CompilerDirectives.inInterpreter()') or
    line.include?(' org.truffleruby.language.RubyBaseNode.coreLibrary()')
}

puts lines.join
