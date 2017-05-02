require 'logger'

class PSD
  module Logger
    def self.included(base)
      base.extend(ClassMethods)
    end

    module ClassMethods
      attr_reader :debug
      attr_writer :logger

      def debug=(enabled)
        @debug = enabled
        @logger = nil
      end

      def logger
        return @logger if @logger

        if debug || ENV['PSD_DEBUG']
          @logger = ::Logger.new(STDOUT)
          @logger.formatter = proc do |severity, datetime, progname, msg|
            "#{severity}: #{msg}\n"
          end
        else
          @logger = DisabledLogger.new
        end

        return @logger
      end
    end
  end

  class DisabledLogger
    def method_missing(method, *args, &block)
      # silence
    end
  end
end