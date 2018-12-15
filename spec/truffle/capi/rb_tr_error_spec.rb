require_relative '../../ruby/optional/capi/spec_helper'

load_extension("rb_tr_error")

describe "Unimplemented functions in the C-API" do
  before :each do
    @s = CApiRbTrErrorSpecs.new
  end

  it "raise a useful RuntimeError including the function name" do
    -> {
      @s.not_implemented_function("foo")
    }.should raise_error(RuntimeError, "rb_str_shared_replace not implemented")
  end
end
