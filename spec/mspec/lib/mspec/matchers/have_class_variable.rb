require 'mspec/matchers/variable'

class HaveClassVariableMatcher < VariableMatcher
  self.description = 'class variable'

  private def check(object, variable)
    object.class_variable_defined?(variable)
  end
end

module MSpecMatchers
  private def have_class_variable(variable)
    HaveClassVariableMatcher.new(variable)
  end
end
