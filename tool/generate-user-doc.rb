root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

contents = contents.gsub(%r{\]\(([^)]+\.(?:md|txt)(?:#.*)?)\)}) {
  link = $1
  if link.start_with? 'http:' or link.start_with? 'https:'
    "](#{link})" # absolute link
  elsif link.start_with? 'doc/user/'
    # Update links to user docs which are sibling files on the website
    "](#{link.sub('doc/user/', '')})"
  else
    # Link to GitHub for .md files outside doc/user
    "](https://github.com/oracle/truffleruby/blob/master/#{link})"
  end
}

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
