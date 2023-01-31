require_relative '../../spec_helper'
require_relative 'shared/iterable_and_tolerating_size_increasing'

describe "Array#all?" do
  @value_to_return = -> (_) { true }
  it_behaves_like :array_iterable_and_tolerating_size_increasing, :all?
end
