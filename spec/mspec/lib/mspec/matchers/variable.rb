class VariableMatcher
  class << self
    attr_accessor :description
  end

  def initialize(variable)
    @variable = variable.to_sym
  end

  def matches?(object)
    @object = object
    check(@object, @variable)
  end

  def failure_message
    ["Expected #{@object} to have #{self.class.description} '#{@variable}'",
     "but it does not"]
  end

  def negative_failure_message
    ["Expected #{@object} NOT to have #{self.class.description} '#{@variable}'",
     "but it does"]
  end
end
