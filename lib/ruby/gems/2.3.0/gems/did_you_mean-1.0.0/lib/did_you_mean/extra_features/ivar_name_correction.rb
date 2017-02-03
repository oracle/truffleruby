module DidYouMean
  module ExtraFeatures
    module IvarNameCorrectable
      REPLS = {
        "(irb)" => -> { Readline::HISTORY.to_a.last }
      }

      def initialize(no_method_error)
        super

        @location   = no_method_error.backtrace_locations.first
        @ivar_names = no_method_error.frame_binding.receiver.instance_variables
      end

      def candidates
        super.merge(receiver_name.to_s => @ivar_names)
      end

      private

      def receiver_name
        return unless receiver.nil?

        abs_path = @location.absolute_path
        lineno   = @location.lineno

        /@(\w+)*\.#{method_name}/ =~ line(abs_path, lineno).to_s && $1
      end

      def line(abs_path, lineno)
        if REPLS[abs_path]
          REPLS[abs_path].call
        elsif File.exist?(abs_path)
          File.open(abs_path) do |file|
            file.detect { file.lineno == lineno }
          end
        end
      end
    end

    SPELL_CHECKERS['NoMethodError'].prepend(IvarNameCorrectable)
  end
end
