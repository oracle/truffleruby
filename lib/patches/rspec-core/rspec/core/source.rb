Truffle::Patching.require_original __FILE__

module RSpec
  module Core
    class Source
      def nodes_by_line_number
        @nodes_by_line_number ||= Hash.new { [] }
      end

      def tokens_by_line_number
        @tokens_by_line_number ||= Hash.new { [] }
      end
    end
  end
end
