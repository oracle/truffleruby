STDERR.puts "🚨🚨 ruby-signature is renamed to rbs. require 'rbs' instead of 'ruby/signature'. 🚨🚨"

require "rbs"

module Ruby
  Signature = RBS
end
