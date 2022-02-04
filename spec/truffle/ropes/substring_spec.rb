# Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../ruby/spec_helper'

describe "A substring" do

  describe "of a UTF-8 valid SubtringRope" do
    it "correctly computes the index and consider offset as a byte offset" do
      complex = ("ééé" + "ascii)")[2..-1]
      complex[-1,1].should == ")"
      complex.end_with?(")").should == true

      str = "aétèbcd)"
      complex = (str[0...4] + str[4..-1])[3..-1]
      complex[-1,1].should == ")"
      complex.end_with?(")").should == true
    end
  end

  describe "of a substring of a complex Rope" do
    it "computes the bytes of that Rope to avoid flattening repetitively" do
      side = 512
      big_string = ("a".b * side + "é".b + "z".b * side)
      substring = big_string[1...-1]
      Truffle::Ropes.should.bytes?(big_string)
      Truffle::Ropes.should_not.bytes?(substring)

      subsubstring = substring.byteslice(4, 8)
      Truffle::Ropes.should.bytes?(big_string)
      Truffle::Ropes.should_not.bytes?(subsubstring)

      # done last as it computes the bytes as a side effect
      subsubstring.should == "aaaaaaaa"
    end
  end

end
