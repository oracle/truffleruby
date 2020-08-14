root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

# Fix links
contents = contents.gsub(/\]\(([^)]+)\)/) {
  link = $1
  if link.start_with?('#')
    # link to same document
  elsif link.start_with?('http://') or link.start_with?('https://')
    # absolute link
  elsif link.start_with?("doc/user/")
    link = link.sub("doc/user/", "")
  else
    if link.end_with?('.png')
      link = "https://raw.githubusercontent.com/oracle/truffleruby/master/#{link}"
    else
      link = "https://github.com/oracle/truffleruby/blob/master/#{link}"
    end
  end
  "](#{link})"
}

# Add top-level title as expected by the website
contents = "# TruffleRuby\n\n#{contents}"

File.write("#{root}/doc/user/README.md", contents)
