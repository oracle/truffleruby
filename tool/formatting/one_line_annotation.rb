# Delete all new lines from annotations so they can be formatted
# properly by the eclipse code formatter

root = File.expand_path File.join __dir__, "..", ".."

def braces(line)
  line.scan('(').size - line.scan(')').size
end

Dir.glob(File.join(root, "src", "**", "*.java")) do |file|
  # print file
  # print ' '
  content = File.read file
  new_content = ""

  looking = false
  braces = 0
  content.lines.each do |line|
    if looking
      if braces == 0
        if line =~ /\w+(\[\])? \w+\(/
          new_line = line.gsub(/^( *)(public |protected |private |)/, '\1protected ')
          # p check: line, new_line: new_line unless line =~ /^( *)(public|protected|private)/
          # p edit: line, new_line: new_line
          new_content << new_line
          looking = false
        else
          p skip: line
          new_content << line
        end
      else
        braces += braces(line)
        # p specialization: line, braces: braces
        new_content << line.chomp << " "
      end
    else
      looking = line.match?(/^ *@(Specialization|Fallback|CreateCast|ExportMessage)/)
      new_content << (looking ? (line.chomp + " ") : line)
      if looking
        braces = braces(line)
        # p specialization: line, braces: braces
      else
        # p ignored: line
      end
    end
  end

  if content != new_content
    # puts 'updates'
    puts file
    # puts new_content
    # break
    File.write file, new_content
  else
    # puts
  end

end

