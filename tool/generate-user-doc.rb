root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

# Update links to user docs which are sibling files on the website
contents = contents.gsub(%r{\]\(doc/user/([^)]+)\)}, '](\1)')

# Remove the Documentation section for the website (redundant with the sidebar menu)
contents = contents.sub(/^## Documentation\n.+?\n##/m, '##')

# Add top-level title as expected by the website
contents = "# TruffleRuby\n\n#{contents}"

File.write("#{root}/doc/user/README.md", contents)
