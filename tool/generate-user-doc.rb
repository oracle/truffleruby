# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

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
