root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

# Remove the Documentation section for the website (redundant with the sidebar menu)
contents = contents.sub(/^## Documentation\n.+?\n##/m, '##')

# Remove 'built by Oracle Labs' for the website (redundant)
contents = contents.sub(" built by\n[Oracle Labs](https://labs.oracle.com)", '')

# Add top-level title as expected by the website
contents = "# TruffleRuby\n\n#{contents}"

File.write("#{root}/doc/user/README.md", contents)
