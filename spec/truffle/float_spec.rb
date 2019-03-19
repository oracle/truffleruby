# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Float#to_s matches MRI" do

  it "for random examples" do
    # 10.times do
    #   bytes = (0...8).map { rand(255) }
    #   string = bytes.pack('C8')
    #   float = string.unpack('D').first
    #   puts "    #{bytes.inspect}.pack('C8').unpack('D').first.to_s.should == #{float.to_s.inspect}"
    # end

    [193, 182, 210, 249, 101, 201, 235, 227].pack('C8').unpack('D').first.to_s.should == "-2.1476558448164614e+173"
    [221, 26, 239, 171, 251, 177, 86, 224].pack('C8').unpack('D').first.to_s.should == "-1.2171740215182016e+156"
    [154, 45, 54, 47, 196, 107, 60, 6].pack('C8').unpack('D').first.to_s.should == "1.2525722967919238e-278"
    [244, 129, 5, 38, 125, 127, 218, 187].pack('C8').unpack('D').first.to_s.should == "-2.244468188651855e-20"
    [38, 118, 202, 94, 175, 231, 208, 223].pack('C8').unpack('D').first.to_s.should == "-3.5415508910848535e+153"
    [24, 143, 78, 101, 219, 177, 42, 27].pack('C8').unpack('D').first.to_s.should == "8.234523887342676e-178"
    [132, 248, 221, 124, 86, 126, 128, 33].pack('C8').unpack('D').first.to_s.should == "2.579794683002462e-147"
    [175, 215, 62, 77, 46, 40, 109, 245].pack('C8').unpack('D').first.to_s.should == "-4.3779268332837355e+257"
    [159, 192, 234, 104, 251, 201, 207, 216].pack('C8').unpack('D').first.to_s.should == "-6.413056398694971e+119"
    [56, 116, 133, 251, 99, 218, 216, 194].pack('C8').unpack('D').first.to_s.should == "-109305037460944.88"
    [108, 87, 169, 189, 212, 8, 93, 198].pack('C8').unpack('D').first.to_s.should == "-9.201399123668237e+30"
    [16, 24, 10, 73, 151, 54, 38, 204].pack('C8').unpack('D').first.to_s.should == "-6.971740228988851e+58"
    [137, 31, 106, 209, 214, 197, 228, 173].pack('C8').unpack('D').first.to_s.should == "-1.305288466844227e-87"
    [46, 207, 217, 116, 118, 213, 171, 207].pack('C8').unpack('D').first.to_s.should == "-6.2948014464533595e+75"
    [42, 125, 191, 144, 195, 19, 161, 37].pack('C8').unpack('D').first.to_s.should == "1.97091810859186e-127"
    [12, 218, 237, 61, 225, 238, 98, 140].pack('C8').unpack('D').first.to_s.should == "-5.288784646981635e-249"
    [5, 139, 63, 217, 195, 113, 126, 97].pack('C8').unpack('D').first.to_s.should == "4.280209597353204e+161"
    [118, 49, 223, 28, 135, 164, 244, 223].pack('C8').unpack('D').first.to_s.should == "-1.7298323616292014e+154"
    [180, 196, 24, 154, 59, 86, 148, 57].pack('C8').unpack('D').first.to_s.should == "2.506709921410835e-31"
    [168, 125, 238, 184, 28, 184, 220, 109].pack('C8').unpack('D').first.to_s.should == "1.6220677710815229e+221"
    [201, 120, 32, 27, 133, 98, 234, 34].pack('C8').unpack('D').first.to_s.should == "1.73095810538336e-140"
    [202, 130, 210, 133, 160, 92, 15, 132].pack('C8').unpack('D').first.to_s.should == "-4.0226800729553587e-289"
    [9, 85, 120, 173, 48, 171, 199, 176].pack('C8').unpack('D').first.to_s.should == "-1.046563748327321e-73"
    [122, 30, 202, 247, 8, 50, 21, 147].pack('C8').unpack('D').first.to_s.should == "-9.606955047506479e-217"
    [195, 204, 86, 69, 72, 20, 54, 180].pack('C8').unpack('D').first.to_s.should == "-3.517423915765567e-57"
    [145, 185, 26, 208, 179, 155, 90, 240].pack('C8').unpack('D').first.to_s.should == "-1.6523892509872832e+233"
    [198, 43, 152, 128, 39, 15, 20, 89].pack('C8').unpack('D').first.to_s.should == "1.2949464434066594e+121"
    [96, 86, 244, 116, 146, 114, 68, 187].pack('C8').unpack('D').first.to_s.should == "-3.382762932592381e-23"
    [173, 36, 106, 90, 3, 154, 82, 56].pack('C8').unpack('D').first.to_s.should == "2.1866091779972295e-37"
    [196, 142, 222, 109, 9, 0, 242, 82].pack('C8').unpack('D').first.to_s.should == "3.666694065832869e+91"
    [220, 197, 220, 52, 163, 116, 89, 211].pack('C8').unpack('D').first.to_s.should == "-3.3186562709755313e+93"
    [180, 140, 180, 233, 195, 115, 109, 216].pack('C8').unpack('D').first.to_s.should == "-9.283808702231933e+117"
    [199, 79, 176, 31, 201, 94, 214, 162].pack('C8').unpack('D').first.to_s.should == "-7.337920460252023e-141"
    [207, 220, 91, 205, 244, 208, 28, 8].pack('C8').unpack('D').first.to_s.should == "1.3636444107667601e-269"
    [102, 150, 64, 150, 37, 26, 68, 59].pack('C8').unpack('D').first.to_s.should == "3.3256194391377893e-23"
    [160, 50, 196, 207, 92, 62, 88, 1].pack('C8').unpack('D').first.to_s.should == "3.535261454306388e-302"
    [238, 223, 239, 119, 18, 121, 80, 211].pack('C8').unpack('D').first.to_s.should == "-2.147581917315252e+93"
    [6, 4, 153, 74, 32, 138, 71, 245].pack('C8').unpack('D').first.to_s.should == "-8.836180350696913e+256"
    [93, 179, 148, 192, 57, 243, 125, 171].pack('C8').unpack('D').first.to_s.should == "-3.4232586644429675e-99"
    [174, 27, 192, 95, 248, 52, 62, 214].pack('C8').unpack('D').first.to_s.should == "-2.7711806994534945e+107"
    [95, 214, 93, 114, 193, 144, 212, 250].pack('C8').unpack('D').first.to_s.should == "-4.77830851596321e+283"
    [20, 120, 208, 87, 24, 220, 246, 215].pack('C8').unpack('D').first.to_s.should == "-5.629499194231452e+115"
    [52, 6, 84, 161, 106, 170, 253, 13].pack('C8').unpack('D').first.to_s.should == "2.780596256030027e-241"
    [232, 157, 0, 12, 66, 70, 189, 85].pack('C8').unpack('D').first.to_s.should == "1.049076558891479e+105"
    [91, 244, 185, 90, 224, 226, 64, 60].pack('C8').unpack('D').first.to_s.should == "1.830809362236487e-18"
    [197, 80, 164, 69, 0, 71, 129, 50].pack('C8').unpack('D').first.to_s.should == "2.050727843259989e-65"
    [212, 172, 15, 25, 205, 33, 242, 46].pack('C8').unpack('D').first.to_s.should == "1.4933711755480538e-82"
    [120, 20, 21, 85, 117, 196, 184, 102].pack('C8').unpack('D').first.to_s.should == "6.735316621341718e+186"
    [217, 184, 12, 141, 56, 72, 146, 207].pack('C8').unpack('D').first.to_s.should == "-2.067308640912632e+75"
    [72, 224, 7, 44, 113, 198, 249, 14].pack('C8').unpack('D').first.to_s.should == "1.5833060097937105e-236"
  end

  it "for the example in GH #1626" do
    (1.0 / 7).to_s.should == "0.14285714285714285"
  end

end
