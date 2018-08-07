require_relative '../spec_helper'

describe 'The --encoding command line option sets Encoding.default_external' do
  before :each do
    @test_string = "print Encoding.default_external.name"
  end
  
  it "if given an encoding with an =" do
    ruby_exe(@test_string, options: '--encoding=big5').should == Encoding::Big5.name
  end
  
  it "if given an encoding as a separate argument" do
    ruby_exe(@test_string, options: '--encoding big5').should == Encoding::Big5.name
  end
end
