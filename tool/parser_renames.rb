contents = STDIN.read

header_regexp = /\A%{.+?%}/m
trailer_regexp = /^%%[^%]+\z/m
original = `git show HEAD:src/main/java/org/truffleruby/parser/parser/RubyParser.y`

original_header = original[header_regexp, 0] or raise
original_trailer = original[trailer_regexp, 0] or raise

contents = contents.lines.map do |line|
  line = line.gsub(/\t/, ' '*8)

  if line.include?('//')
    line, comment = line.split('//', 2)
  end

  # import org.jruby.ast.IfNode;
  # import org.truffleruby.parser.ast.IfParseNode;
  line = line.gsub(/\borg\.jruby\.ast\./, 'org.truffleruby.parser.ast.')
  line = line.gsub(/\b([A-Z]\w+)Node\b/, '\1ParseNode')
  line = line.gsub(/\b(ILiteral|IName)ParseNode\b/, '\1Node')

  line = line.gsub(/\bNode\b/, 'ParseNode')
  line = line.gsub(/\bISourcePosition\b/, 'SourceIndexLength')

  line = line.gsub(/\bsupport.warn(ing)?\(ID[^,]+, /, 'support.warn\1(')

  line = line.gsub(/<KeyValuePair>/, '<ParseNodeTuple>')

  line = line.gsub(/\bByteList\b/, 'Rope')
  line = line.gsub(/\bRubySymbol\b/, 'Rope')

  line = line.gsub(/\b(lexer|LexingCommon)\.([A-Z_]+)\b/, 'RopeConstants.\2')

  line = "#{line}//#{comment}" if comment
  line
end.join

contents = contents.sub(header_regexp, original_header)
contents = contents.sub(trailer_regexp, original_trailer)

puts contents
