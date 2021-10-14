unless defined?(RubyVM::InstructionSequence)
  puts "Currently, TypeProf can work on a Ruby implementation that supports RubyVM::InstructionSequence, such as CRuby."
  exit 1
end

module TypeProf end

require_relative "typeprof/version"
require_relative "typeprof/config"
require_relative "typeprof/insns-def"
require_relative "typeprof/utils"
require_relative "typeprof/type"
require_relative "typeprof/container-type"
require_relative "typeprof/method"
require_relative "typeprof/block"
require_relative "typeprof/iseq"
require_relative "typeprof/arguments"
require_relative "typeprof/analyzer"
require_relative "typeprof/import"
require_relative "typeprof/export"
require_relative "typeprof/builtin"
require_relative "typeprof/cli"
