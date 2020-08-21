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

# Remove the Documentation section for the website (redundant with the sidebar menu)
contents = contents.sub(/^## Documentation\n.+?\n##/m, '##')

# Remove 'built by Oracle Labs' for the website (redundant)
contents = contents.sub(" built by\n[Oracle Labs](https://labs.oracle.com)", '')

# Add top-level title as expected by the website
contents = "# TruffleRuby\n\n#{contents}"

File.write("#{root}/doc/user/README.md", contents)

# Fix links to README for all doc/user files
Dir.glob("#{root}/doc/user/*.md") do |file|
  contents = File.read(file)
  contents = contents.gsub('](../../README.md#', '](../#')
  File.write(file, contents)
end
