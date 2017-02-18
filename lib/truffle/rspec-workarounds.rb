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
puts "WORKAROUND for ripper_supported? loaded" if $VERBOSE