require 'mspec/matchers/variable'

class HaveInstanceVariableMatcher < VariableMatcher
  self.description = 'instance variable'

  private def check(object, variable)
    object.instance_variable_defined?(variable)
  end
end

module MSpecMatchers
  private def have_instance_variable(variable)
    HaveInstanceVariableMatcher.new(variable)
  end
end
