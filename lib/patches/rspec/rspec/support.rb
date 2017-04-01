Truffle::Patching.require_original __FILE__

module RSpec
  module Support
    module RubyFeatures
      module_function
      def ripper_supported?
        false
      end
    end
  end
end
