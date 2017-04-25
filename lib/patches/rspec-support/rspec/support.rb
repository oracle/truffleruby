Truffle::Patching.require_original __FILE__

module RSpec
  module Support
    module RubyFeatures
      module_function
      def ripper_supported?
        false
      end
    end

    def self.define_optimized_require_for_rspec(lib, &require_relative)
      name = "require_rspec_#{lib}"

      (class << self; self; end).__send__(:define_method, name) do |f|
        require "rspec/#{lib}/#{f}"
      end
    end

    define_optimized_require_for_rspec(:support)
    define_optimized_require_for_rspec(:core)
  end
end
