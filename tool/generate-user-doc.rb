root = File.expand_path('../..', __FILE__)

contents = File.read("#{root}/README.md")

# Fix relative links
contents = contents.gsub('(doc/user/', '(')

# Add top-level title as expected by the website
contents = "# For Ruby User\n\n#{contents}"

File.write("#{root}/doc/user/README.md", contents)
