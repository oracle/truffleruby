#!/usr/bin/env ruby
search, replace, pattern = ARGV
Dir.glob(pattern) do |file|
  text = File.read file
  if text.include? search
    puts file
    text.gsub!(search, replace)
  end
  File.write file, text
end
