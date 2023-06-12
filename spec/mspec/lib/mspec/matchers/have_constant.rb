require 'mspec/matchers/variable'

class HaveConstantMatcher < VariableMatcher
  self.description = 'constant'

  private def check(object, variable)
    # Differs from object.const_defined?(variable, false) for undefined constants
    object.constants(false).include?(variable)
  end
end

module MSpecMatchers
  private def have_constant(variable)
    HaveConstantMatcher.new(variable)
  end
end
