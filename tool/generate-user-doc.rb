root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

# Update links to user docs which are sibling files on the website
contents = contents.gsub(%r{\]\(doc/user/([^)]+)\)}, '](\1)')

# Fix link to logo
contents = contents.gsub(%r{\]\(logo/([^)]+)\)},
  '](https://raw.githubusercontent.com/oracle/truffleruby/master/logo/\1)')

# Remove the Documentation section for the website (redundant with the sidebar menu)
contents = contents.sub(/^## Documentation\n.+?\n##/m, '##')

# Add top-level title as expected by the website
contents = <<HEADER + contents
---
layout: docs-experimental
toc_group: ruby
link_title: Ruby Reference
permalink: /reference-manual/ruby/
---
# TruffleRuby

HEADER

File.write("#{root}/doc/user/README.md", contents)
