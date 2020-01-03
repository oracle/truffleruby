require_relative '../../spec_helper'
require 'English'

describe "alias keyword" do
  it "aliases $ERROR_INFO to $! in English and $ERROR_INFO still returns a backtrace" do
    (1 / 0 rescue $ERROR_INFO).should_not == nil
  end
end
