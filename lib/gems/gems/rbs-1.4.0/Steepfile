target :lib do
  signature "sig"
  check "lib"
  ignore "lib/rbs/parser.rb"
  ignore "lib/rbs/prototype", "lib/rbs/test", "lib/rbs/test.rb"

  library "set", "pathname", "json", "logger", "monitor", "tsort"
  signature "stdlib/strscan/0/"
  signature "stdlib/rubygems/0/"
  signature "stdlib/optparse/0/"
end

# target :lib do
#   signature "sig"
#
#   check "lib"                       # Directory name
#   check "Gemfile"                   # File name
#   check "app/models/**/*.rb"        # Glob
#   # ignore "lib/templates/*.rb"
#
#   # library "pathname", "set"       # Standard libraries
#   # library "strong_json"           # Gems
# end

# target :spec do
#   signature "sig", "sig-private"
#
#   check "spec"
#
#   # library "pathname", "set"       # Standard libraries
#   # library "rspec"
# end
