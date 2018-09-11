require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby")

describe "C-API TRUFFLERUBY macro" do
  before :each do
    @s = CApiTruffleRubySpecs.new
  end

  it "is defined" do
    @s.truffleruby.should be_true
  end
end
