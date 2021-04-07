require_relative '../../spec_helper'
require_relative 'fixtures/classes'

describe "Kernel#format" do
  it "is a private method" do
    Kernel.should have_private_instance_method(:format)
  end
end

describe "Kernel.format" do
  it "is accessible as a module function" do
    Kernel.format("%s", "hello").should == "hello"
  end

  it "formats string with precision" do
    Kernel.format("%.3s", "hello").should == "hel"
    Kernel.format("%-3.3s", "hello").should == "hel"
  end

  describe "on multibyte strings" do
    it "formats string with precision" do
      Kernel.format("%.3s", "hello".encode('UTF-16LE')).should == "hel".encode('UTF-16LE')
    end

    it "preserves encoding" do
      str = format('%s', 'foobar'.encode('UTF-16LE'))
      str.encoding.to_s.should == "UTF-16LE"
    end
  end
end
