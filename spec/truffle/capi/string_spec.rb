require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_string")

describe "TruffleRuby RSTRING_PTR" do
  before :each do
    @s = CApiTruffleStringSpecs.new
  end

  it "does not store the String to native memory if not needed" do
    str = "foobar"
    @s.string_ptr(str).should == "f"
    Truffle::CExt.string_pointer_is_native?(str).should == false
  end

  it "stores the String to native memory if stored in malloc'd memory" do
    str = "foobar"
    @s.string_ptr_stored_in_native(str)
    Truffle::CExt.string_pointer_is_native?(str).should == true
  end

  it "stores the String to native memory if the address is returned" do
    str = "foobar"
    @s.string_ptr_return_address(str).should be_kind_of(Integer)
    Truffle::CExt.string_pointer_is_native?(str).should == true
  end
end
