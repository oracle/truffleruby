require_relative '../../../spec_helper'
require 'net/http'

describe "Net::HTTPGenericRequest#body_exist?" do
  it "returns true when the response is expected to have a body" do
    request = Net::HTTPGenericRequest.new("POST", true, true, "/some/path")
    request.body_exist?.should be_true

    request = Net::HTTPGenericRequest.new("POST", true, false, "/some/path")
    request.body_exist?.should be_false
  end

  describe "when $VERBOSE is true" do
    it "emits a warning" do
      request = Net::HTTPGenericRequest.new("POST", true, false, "/some/path")
      -> {
        request.body_exist?
      }.should complain(/body_exist\? is obsolete/, verbose: true)
    end
  end
end
