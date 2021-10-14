require "rbs/version"

require "set"
require "json"
require "pathname"
require "pp"
require "ripper"
require "logger"
require "tsort"

require "rbs/errors"
require "rbs/buffer"
require "rbs/location"
require "rbs/namespace"
require "rbs/type_name"
require "rbs/types"
require "rbs/method_type"
require "rbs/ast/declarations"
require "rbs/ast/members"
require "rbs/ast/annotation"
require "rbs/environment"
require "rbs/environment_loader"
require "rbs/builtin_names"
require "rbs/definition"
require "rbs/definition_builder"
require "rbs/definition_builder/ancestor_builder"
require "rbs/definition_builder/method_builder"
require "rbs/variance_calculator"
require "rbs/substitution"
require "rbs/constant"
require "rbs/constant_table"
require "rbs/ast/comment"
require "rbs/writer"
require "rbs/prototype/rbi"
require "rbs/prototype/rb"
require "rbs/prototype/runtime"
require "rbs/type_name_resolver"
require "rbs/environment_walker"
require "rbs/vendorer"
require "rbs/validator"
require "rbs/factory"
require "rbs/repository"

begin
  require "rbs/parser"
rescue LoadError
  STDERR.puts "Missing parser Ruby code? Running `rake parser` may solve the issue"
  raise
end

module RBS
  class <<self
    attr_reader :logger_level
    attr_reader :logger_output

    def logger
      @logger ||= Logger.new(logger_output || STDERR, level: logger_level || "warn", progname: "rbs")
    end

    def logger_output=(val)
      @logger_output = val
      @logger = nil
    end

    def logger_level=(level)
      @logger_level = level
      @logger = nil
    end
  end
end
