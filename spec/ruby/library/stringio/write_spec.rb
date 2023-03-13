require_relative '../../spec_helper'
require_relative 'fixtures/classes'
require_relative 'shared/write'

describe "StringIO#write when passed [Object]" do
  it_behaves_like :stringio_write, :write
end

describe "StringIO#write when passed [String]" do
  it_behaves_like :stringio_write_string, :write
end

describe "StringIO#write when self is not writable" do
  it_behaves_like :stringio_write_not_writable, :write
end

describe "StringIO#write when in append mode" do
  it_behaves_like :stringio_write_append, :write
end

describe "StringIO#write transcoding" do
  describe "when UTF-16 encoding is set" do
    it "accepts a UTF-8-encoded string and transcodes it" do
      io = StringIO.new.set_encoding(Encoding::UTF_16BE)
      utf8_str = "hello"

      io.write(utf8_str)

      result = io.string
      expected = [
        0, 104, 0, 101, 0, 108, 0, 108, 0, 111, # double-width "hello"
      ]

      io.external_encoding.should == Encoding::UTF_16BE
      result.bytes.should == expected
    end
  end
end
