require_relative '../../spec_helper'

describe "WeakRef#allocate" do
  it "assigns nil as the reference" do
    lambda { WeakRef.allocate.__getobj__ }.should raise_error(WeakRef::RefError)
  end
end
