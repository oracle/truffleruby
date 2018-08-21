require File.expand_path('../../../spec_helper', __FILE__)
require 'etc'

describe "Etc.passwd" do
  it "returns a Etc::Passwd struct" do
    Etc.passwd.should be_an_instance_of(Etc::Passwd)
  end
end
