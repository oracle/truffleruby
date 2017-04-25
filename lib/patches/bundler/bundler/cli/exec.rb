Truffle::Patching.require_original __FILE__

module Bundler
  class CLI::Exec
    def ruby_shebang?(file)
      possibilities = [
          "#!/usr/bin/env ruby\n",
          "#!/usr/bin/env jruby\n",
          # TruffleRuby: added item to the array
          "#!/usr/bin/env truffleruby\n",
          "#!#{Gem.ruby}\n",
      ]
      first_line = File.open(file, "rb") {|f| f.read(possibilities.map(&:size).max) }
      possibilities.any? {|shebang| first_line.start_with?(shebang) }
    end
  end
end
